package com.worknear.api.booking;

import com.worknear.api.booking.domain.Booking;
import com.worknear.api.booking.domain.BookingPhoto;
import com.worknear.api.booking.domain.BookingPhotoType;
import com.worknear.api.booking.domain.BookingStatus;
import com.worknear.api.booking.domain.BookingStatusHistory;
import com.worknear.api.booking.dto.BookingResponse;
import com.worknear.api.booking.dto.CancelBookingRequest;
import com.worknear.api.booking.dto.CancelPreviewResponse;
import com.worknear.api.booking.dto.CreateBookingRequest;
import com.worknear.api.booking.dto.RescheduleBookingRequest;
import com.worknear.api.catalog.ServiceCategoryRepository;
import com.worknear.api.chat.domain.ChatThread;
import com.worknear.api.chat.ChatThreadRepository;
import com.worknear.api.common.exception.BadRequestException;
import com.worknear.api.common.exception.ForbiddenException;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.config.WorkNearProperties;
import com.worknear.api.notification.NotificationService;
import com.worknear.api.professional.ProfessionalProfileRepository;
import com.worknear.api.professional.ProfessionalServiceRepository;
import com.worknear.api.professional.domain.ProfessionalProfile;
import com.worknear.api.storage.StorageService;
import com.worknear.api.user.CustomerAddressRepository;
import com.worknear.api.user.UserRepository;
import com.worknear.api.user.domain.Role;
import com.worknear.api.user.domain.User;
import com.worknear.api.wallet.WalletService;
import com.worknear.api.wallet.domain.TransactionReason;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static final int MAX_RESCHEDULES = 2;
    private static final int FREE_LATE_CANCELS_PER_MONTH = 3;
    private static final int EARLY_CANCEL_HOURS = 24;
    private static final BigDecimal LATE_CANCEL_FEE = new BigDecimal("50.00");
    private static final Duration COMPLETION_OTP_TTL = Duration.ofHours(2);

    /** Authoritative timezone for all booking date/time validation (ignores the device clock). */
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int MAX_BOOKING_DAYS_AHEAD = 7;

    /** Statuses that count as an "active" booking for duplicate-prevention. */
    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(
            BookingStatus.PENDING, BookingStatus.CONFIRMED,
            BookingStatus.ON_THE_WAY, BookingStatus.ARRIVED,
            BookingStatus.IN_PROGRESS, BookingStatus.COMPLETED_PENDING_OTP);

    private final BookingRepository bookingRepository;
    private final BookingPhotoRepository photoRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final ProfessionalServiceRepository professionalServiceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final CustomerAddressRepository addressRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final StorageService storageService;
    private final BookingMapper mapper;
    private final WorkNearProperties properties;

    @Transactional
    public BookingResponse create(UUID customerId, CreateBookingRequest req) {
        User pro = userRepository.findById(req.professionalId())
                .orElseThrow(() -> NotFoundException.of("Professional", req.professionalId()));
        if (pro.getRole() != Role.PROFESSIONAL) {
            throw new BadRequestException("Target user is not a professional");
        }
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(req.professionalId())
                .orElseThrow(() -> NotFoundException.of("Professional profile", req.professionalId()));
        if (!categoryRepository.existsById(req.categoryId())) {
            throw NotFoundException.of("Category", req.categoryId());
        }
        validateSchedule(req.scheduledDate(), req.slotStart(), req.slotEnd());

        if (bookingRepository.existsByCustomerIdAndProfessionalIdAndStatusIn(
                customerId, req.professionalId(), ACTIVE_STATUSES)) {
            throw new BadRequestException("DUPLICATE_ACTIVE_BOOKING",
                    "You already have an active booking with this professional. "
                            + "Please complete or cancel it before booking again.");
        }

        BigDecimal amount = professionalServiceRepository
                .findByProfessionalIdAndCategoryId(profile.getId(), req.categoryId())
                .orElseThrow(() -> new BadRequestException("This professional does not offer the selected service"))
                .getBasePrice();

        BigDecimal commission = amount
                .multiply(BigDecimal.valueOf(properties.payment().platformCommissionPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Booking booking = new Booking();
        booking.setCode(generateCode());
        booking.setCustomerId(customerId);
        booking.setProfessionalId(req.professionalId());
        booking.setCategoryId(req.categoryId());
        booking.setScheduledDate(req.scheduledDate());
        booking.setSlotStart(req.slotStart());
        booking.setSlotEnd(req.slotEnd());
        applyAddress(booking, customerId, req);
        booking.setProblemDescription(req.problemDescription());
        booking.setStatus(BookingStatus.PENDING);
        booking.setAmount(amount);
        booking.setCommission(commission);
        booking.setProEarning(amount.subtract(commission));
        booking.setLockedAmount(amount);
        booking.setPaymentMethod(req.paymentMethod());
        walletService.debit(customerId, amount, TransactionReason.BOOKING_PAYMENT,
                null, "Locked amount for booking " + booking.getCode());
        booking = bookingRepository.save(booking);

        recordHistory(booking, BookingStatus.PENDING, "Booking created", customerId);
        notificationService.notifyUser(req.professionalId(), "BOOKING_REQUEST",
                "New job request", "You have a new booking request " + booking.getCode());

        return mapper.toResponse(booking, customerId);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(UUID userId, UUID bookingId) {
        Booking booking = require(bookingId);
        assertParticipant(userId, booking);
        return mapper.toResponse(booking, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> listForCustomer(UUID customerId, String tab, Pageable pageable) {
        List<BookingStatus> statuses = customerTab(tab);
        var page = statuses == null
                ? bookingRepository.findByCustomerId(customerId, pageable)
                : bookingRepository.findByCustomerIdAndStatusIn(customerId, statuses, pageable);
        return PageResponse.from(page, booking -> mapper.toResponse(booking, customerId));
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> listForProfessional(UUID proId, String tab, Pageable pageable) {
        List<BookingStatus> statuses = proTab(tab);
        var page = statuses == null
                ? bookingRepository.findByProfessionalId(proId, pageable)
                : bookingRepository.findByProfessionalIdAndStatusIn(proId, statuses, pageable);
        return PageResponse.from(page, booking -> mapper.toResponse(booking, proId));
    }

    @Transactional
    public BookingResponse accept(UUID proId, UUID bookingId) {
        Booking booking = require(bookingId);
        assertAssignedPro(proId, booking);
        transition(booking, BookingStatus.CONFIRMED, proId, "Accepted by professional");
        booking.setConfirmedAt(Instant.now());
        ensureChatThread(booking);
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_CONFIRMED",
                "Booking confirmed", "Your booking " + booking.getCode() + " was accepted");
        return mapper.toResponse(booking, proId);
    }

    @Transactional
    public BookingResponse reject(UUID proId, UUID bookingId, String note) {
        Booking booking = require(bookingId);
        assertAssignedPro(proId, booking);
        transition(booking, BookingStatus.REJECTED, proId, note);
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_REJECTED",
                "Booking declined", "Your booking " + booking.getCode() + " was declined");
        return mapper.toResponse(booking, proId);
    }

    @Transactional
    public BookingResponse updateStatus(UUID proId, UUID bookingId, BookingStatus target, String note) {
        Booking booking = require(bookingId);
        assertAssignedPro(proId, booking);
        if (target == BookingStatus.REJECTED || target == BookingStatus.CONFIRMED || target == BookingStatus.CANCELLED) {
            throw new BadRequestException("Use the dedicated accept/reject/cancel endpoints for this transition");
        }
        if (target == BookingStatus.ON_THE_WAY) {
            return markOnTheWay(proId, bookingId);
        }
        if (target == BookingStatus.ARRIVED) {
            return markArrived(proId, bookingId);
        }
        if (target == BookingStatus.IN_PROGRESS) {
            return startWork(proId, bookingId);
        }
        if (target == BookingStatus.COMPLETED_PENDING_OTP) {
            return markWorkCompleted(proId, bookingId);
        }
        throw new BadRequestException("Use the dedicated status endpoints for this transition");
    }

    @Transactional
    public BookingResponse markOnTheWay(UUID proId, UUID bookingId) {
        Booking booking = require(bookingId);
        assertAssignedPro(proId, booking);
        transition(booking, BookingStatus.ON_THE_WAY, proId, "Professional is on the way");
        booking.setOnTheWayAt(Instant.now());
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_UPDATE",
                "Professional on the way", "Your professional is on the way for booking " + booking.getCode());
        return mapper.toResponse(booking, proId);
    }

    @Transactional
    public BookingResponse markArrived(UUID proId, UUID bookingId) {
        Booking booking = require(bookingId);
        assertAssignedPro(proId, booking);
        transition(booking, BookingStatus.ARRIVED, proId, "Professional arrived");
        booking.setArrivedAt(Instant.now());
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_UPDATE",
                "Professional arrived", "Your professional has arrived for booking " + booking.getCode());
        return mapper.toResponse(booking, proId);
    }

    @Transactional
    public BookingResponse startWork(UUID proId, UUID bookingId) {
        Booking booking = require(bookingId);
        assertAssignedPro(proId, booking);
        transition(booking, BookingStatus.IN_PROGRESS, proId, "Work started");
        booking.setWorkStartedAt(Instant.now());
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_UPDATE",
                "Work started", "Work has started for booking " + booking.getCode());
        return mapper.toResponse(booking, proId);
    }

    @Transactional
    public BookingResponse markWorkCompleted(UUID proId, UUID bookingId) {
        Booking booking = require(bookingId);
        assertAssignedPro(proId, booking);
        String otp = generateCompletionOtp();
        booking.setCompletionOtpCode(otp);
        booking.setCompletionOtpHash(hashOtp(otp));
        booking.setCompletionOtpExpiresAt(Instant.now().plus(COMPLETION_OTP_TTL));
        transition(booking, BookingStatus.COMPLETED_PENDING_OTP, proId, "Work completed; waiting for customer OTP");
        booking.setWorkCompletedAt(Instant.now());
        bookingRepository.save(booking);
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_COMPLETION_OTP",
                "Confirm completion", "Share the OTP only after the work is fully completed.");
        return mapper.toResponse(booking, proId);
    }

    @Transactional
    public BookingResponse verifyCompletionOtp(UUID proId, UUID bookingId, String otp) {
        Booking booking = require(bookingId);
        assertAssignedPro(proId, booking);
        if (booking.getStatus() != BookingStatus.COMPLETED_PENDING_OTP) {
            throw new BadRequestException("OTP_NOT_EXPECTED", "This booking is not waiting for completion OTP.");
        }
        if (otp == null || otp.isBlank() || !hashOtp(otp.trim()).equals(booking.getCompletionOtpHash())) {
            throw new BadRequestException("INVALID_OTP", "Invalid completion OTP.");
        }
        if (booking.getCompletionOtpExpiresAt() != null && Instant.now().isAfter(booking.getCompletionOtpExpiresAt())) {
            throw new BadRequestException("OTP_EXPIRED", "Completion OTP expired. Please mark work completed again.");
        }
        transition(booking, BookingStatus.COMPLETED, proId, "Completion OTP verified");
        completeBooking(booking);
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_COMPLETED",
                "Booking completed", "Booking " + booking.getCode() + " is completed.");
        return mapper.toResponse(booking, proId);
    }

    @Transactional(readOnly = true)
    public CancelPreviewResponse cancelPreview(UUID userId, UUID bookingId) {
        Booking booking = require(bookingId);
        if (!booking.getCustomerId().equals(userId) && !isAdmin(userId)) {
            throw new ForbiddenException("Only the customer can preview cancellation for this booking");
        }
        if (!booking.getStatus().canTransitionTo(BookingStatus.CANCELLED)) {
            return new CancelPreviewResponse(false, false, BigDecimal.ZERO, null, false, 0,
                    0, FREE_LATE_CANCELS_PER_MONTH, FREE_LATE_CANCELS_PER_MONTH,
                    walletService.getWallet(userId).balance(), true,
                    "This booking can no longer be cancelled.");
        }
        CancelQuote quote = computeCancelQuote(booking);
        BigDecimal balance = walletService.getWallet(userId).balance();
        BigDecimal locked = booking.getLockedAmount() == null ? BigDecimal.ZERO : booking.getLockedAmount();
        boolean sufficient = !quote.feeApplies() || locked.compareTo(quote.feeAmount()) >= 0;
        return new CancelPreviewResponse(
                true,
                quote.feeApplies(),
                quote.feeAmount(),
                quote.feeReason(),
                quote.lateCancel(),
                quote.hoursUntilSlot(),
                quote.lateCancelsUsedThisMonth(),
                FREE_LATE_CANCELS_PER_MONTH,
                quote.lateCancelsRemaining(),
                balance,
                sufficient,
                quote.message());
    }

    @Transactional
    public BookingResponse cancel(UUID userId, UUID bookingId, CancelBookingRequest req) {
        Booking booking = require(bookingId);
        if (!booking.getCustomerId().equals(userId) && !isAdmin(userId)) {
            throw new ForbiddenException("Only the customer can cancel this booking");
        }

        CancelPreviewResponse preview = cancelPreview(userId, bookingId);
        if (!preview.canCancel()) {
            throw new BadRequestException("CANNOT_CANCEL", preview.message());
        }
        if (preview.feeApplies()) {
            if (!preview.sufficientWalletBalance()) {
                throw new BadRequestException("INSUFFICIENT_BALANCE",
                        "Insufficient wallet balance. Please add ₹"
                                + preview.feeAmount().stripTrailingZeros().toPlainString()
                                + " to your wallet.");
            }
            booking.setCancellationFee(preview.feeAmount());
        }
        refundLockedAmount(booking, preview.feeAmount());
        booking.setLateCancel(preview.lateCancel());
        booking.setCancellationReason(req.reasonCode().name());
        booking.setCancellationComment(req.comment());

        transition(booking, BookingStatus.CANCELLED, userId, "Cancelled by customer");
        booking.setCancelledBy("CUSTOMER");
        booking.setCancelledAt(Instant.now());

        if (booking.getProfessionalId() != null) {
            notificationService.notifyUser(booking.getProfessionalId(), "BOOKING_CANCELLED",
                    "Booking cancelled", "Booking " + booking.getCode() + " was cancelled");
        }
        return mapper.toResponse(booking, userId);
    }

    @Transactional
    public BookingResponse cancelByProfessional(UUID userId, UUID bookingId, String note) {
        Booking booking = require(bookingId);
        boolean isPro = userId.equals(booking.getProfessionalId());
        if (!isPro && !isAdmin(userId)) {
            throw new ForbiddenException("You cannot cancel this booking");
        }
        if (!booking.getStatus().canTransitionTo(BookingStatus.CANCELLED)) {
            throw new BadRequestException("CANNOT_CANCEL", "This booking can no longer be cancelled.");
        }
        transition(booking, BookingStatus.CANCELLED, userId, note);
        booking.setCancellationReason(note);
        booking.setCancelledBy(isPro ? "PROFESSIONAL" : "ADMIN");
        booking.setCancelledAt(Instant.now());
        refundLockedAmount(booking, BigDecimal.ZERO);
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_CANCELLED",
                "Booking cancelled", "Booking " + booking.getCode() + " was cancelled");
        return mapper.toResponse(booking, userId);
    }

    @Transactional
    public BookingResponse reschedule(UUID userId, UUID bookingId, RescheduleBookingRequest req) {
        Booking booking = require(bookingId);
        if (!booking.getCustomerId().equals(userId) && !isAdmin(userId)) {
            throw new ForbiddenException("Only the customer can reschedule this booking");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("CANNOT_RESCHEDULE",
                    "This booking can no longer be rescheduled.");
        }
        if (booking.getRescheduleCount() >= MAX_RESCHEDULES) {
            throw new BadRequestException("RESCHEDULE_LIMIT",
                    "You can reschedule this booking at most " + MAX_RESCHEDULES + " times.");
        }
        validateSchedule(req.scheduledDate(), req.slotStart(), req.slotEnd());

        booking.setScheduledDate(req.scheduledDate());
        booking.setSlotStart(req.slotStart());
        booking.setSlotEnd(req.slotEnd());
        booking.setRescheduleCount(booking.getRescheduleCount() + 1);
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            booking.setStatus(BookingStatus.PENDING);
            booking.setConfirmedAt(null);
        }
        recordHistory(booking, booking.getStatus(),
                "Rescheduled to " + req.scheduledDate() + " " + req.slotStart() + "-" + req.slotEnd(), userId);

        if (booking.getProfessionalId() != null) {
            notificationService.notifyUser(booking.getProfessionalId(), "BOOKING_RESCHEDULED",
                    "Booking rescheduled",
                    "Booking " + booking.getCode() + " was moved to " + req.scheduledDate());
        }
        return mapper.toResponse(booking, userId);
    }

    @Transactional
    public BookingResponse uploadPhoto(UUID userId, UUID bookingId, BookingPhotoType type, MultipartFile file) {
        Booking booking = require(bookingId);
        assertParticipant(userId, booking);
        StorageService.StoredFile stored = storageService.store(file, "bookings/" + bookingId);
        BookingPhoto photo = new BookingPhoto();
        photo.setBookingId(bookingId);
        photo.setType(type);
        photo.setFileUrl(stored.url());
        photo.setUploadedBy(userId);
        photoRepository.save(photo);
        return mapper.toResponse(booking, userId);
    }

    // ---- internals ----

    private void validateSchedule(LocalDate date, LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new BadRequestException("Slot end time must be after start time");
        }
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate maxDate = today.plusDays(MAX_BOOKING_DAYS_AHEAD);
        if (date.isBefore(today)) {
            throw new BadRequestException("INVALID_DATE", "Booking date cannot be in the past.");
        }
        if (date.isAfter(maxDate)) {
            throw new BadRequestException("INVALID_DATE",
                    "You can only book up to " + MAX_BOOKING_DAYS_AHEAD + " days in advance.");
        }
        if (date.isEqual(today) && !start.isAfter(LocalTime.now(BUSINESS_ZONE))) {
            throw new BadRequestException("INVALID_TIME", "Please pick a time later than the current time.");
        }
    }

    private CancelQuote computeCancelQuote(Booking booking) {
        double hoursUntil = hoursUntilSlot(booking);
        boolean late = hoursUntil < EARLY_CANCEL_HOURS;
        int used = countLateCancelsThisMonth(booking.getCustomerId());
        int remaining = Math.max(0, FREE_LATE_CANCELS_PER_MONTH - used);

        if (!late) {
            return new CancelQuote(false, BigDecimal.ZERO, null, false, hoursUntil, used, remaining,
                    "Free cancellation — your slot is more than 24 hours away.");
        }
        if (used < FREE_LATE_CANCELS_PER_MONTH) {
            return new CancelQuote(false, BigDecimal.ZERO, null, true, hoursUntil, used, remaining,
                    "Late cancellation — " + (used + 1) + " of " + FREE_LATE_CANCELS_PER_MONTH
                            + " free late cancellations used this month.");
        }
        return new CancelQuote(true, LATE_CANCEL_FEE, "LATE_CANCEL_QUOTA_EXCEEDED", true, hoursUntil, used, 0,
                "Less than 24 hours to your slot and you've used your free late cancellations. "
                        + "A ₹50 fee applies.");
    }

    private double hoursUntilSlot(Booking booking) {
        ZonedDateTime slotStart = ZonedDateTime.of(
                booking.getScheduledDate(), booking.getSlotStart(), BUSINESS_ZONE);
        long seconds = Duration.between(Instant.now(), slotStart.toInstant()).getSeconds();
        return seconds / 3600.0;
    }

    private int countLateCancelsThisMonth(UUID customerId) {
        ZonedDateTime monthStart = LocalDate.now(BUSINESS_ZONE)
                .withDayOfMonth(1)
                .atStartOfDay(BUSINESS_ZONE);
        return (int) bookingRepository.countLateCustomerCancelsSince(customerId, monthStart.toInstant());
    }

    private record CancelQuote(
            boolean feeApplies,
            BigDecimal feeAmount,
            String feeReason,
            boolean lateCancel,
            double hoursUntilSlot,
            int lateCancelsUsedThisMonth,
            int lateCancelsRemaining,
            String message
    ) {}

    private void completeBooking(Booking booking) {
        if (booking.getPaymentReleasedAt() != null) {
            return;
        }
        booking.setCompletedAt(Instant.now());
        booking.setPaymentReleasedAt(Instant.now());
        booking.setCompletionOtpCode(null);
        booking.setCompletionOtpHash(null);
        booking.setCompletionOtpExpiresAt(null);
        if (booking.getProfessionalId() != null) {
            walletService.credit(booking.getProfessionalId(), booking.getProEarning(),
                    TransactionReason.EARNING, booking.getId(),
                    "Earning for booking " + booking.getCode());
            professionalProfileRepository.findByUserId(booking.getProfessionalId()).ifPresent(p -> {
                p.setJobsCompleted(p.getJobsCompleted() + 1);
            });
        }
        booking.setLockedAmount(BigDecimal.ZERO);
    }

    private void refundLockedAmount(Booking booking, BigDecimal feeAmount) {
        BigDecimal locked = booking.getLockedAmount() == null ? BigDecimal.ZERO : booking.getLockedAmount();
        if (locked.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal fee = feeAmount == null ? BigDecimal.ZERO : feeAmount;
        BigDecimal refund = locked.subtract(fee);
        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            walletService.credit(booking.getCustomerId(), refund, TransactionReason.REFUND,
                    booking.getId(), "Refund for cancelled booking " + booking.getCode());
        }
        booking.setLockedAmount(BigDecimal.ZERO);
    }

    private void transition(Booking booking, BookingStatus target, UUID actor, String note) {
        if (!booking.getStatus().canTransitionTo(target)) {
            throw new BadRequestException("ILLEGAL_TRANSITION",
                    "Cannot move booking from " + booking.getStatus() + " to " + target);
        }
        booking.setStatus(target);
        recordHistory(booking, target, note, actor);
    }

    private void ensureChatThread(Booking booking) {
        if (booking.getProfessionalId() == null) {
            return;
        }
        if (chatThreadRepository.findByBookingId(booking.getId()).isEmpty()) {
            ChatThread thread = new ChatThread();
            thread.setBookingId(booking.getId());
            thread.setCustomerId(booking.getCustomerId());
            thread.setProfessionalId(booking.getProfessionalId());
            chatThreadRepository.save(thread);
        }
    }

    private void applyAddress(Booking booking, UUID customerId, CreateBookingRequest req) {
        if (req.addressId() != null) {
            var addr = addressRepository.findByIdAndUserId(req.addressId(), customerId)
                    .orElseThrow(() -> NotFoundException.of("Address", req.addressId()));
            booking.setAddressLine(join(addr.getLine1(), addr.getLine2()));
            booking.setCity(addr.getCity());
            booking.setLatitude(addr.getLatitude());
            booking.setLongitude(addr.getLongitude());
        } else {
            if (req.addressLine() == null || req.addressLine().isBlank()) {
                throw new BadRequestException("An address (addressId or addressLine) is required");
            }
            booking.setAddressLine(req.addressLine());
            booking.setCity(req.city());
            booking.setLatitude(req.latitude());
            booking.setLongitude(req.longitude());
        }
    }

    private void recordHistory(Booking booking, BookingStatus status, String note, UUID actor) {
        BookingStatusHistory h = new BookingStatusHistory();
        h.setBookingId(booking.getId());
        h.setStatus(status);
        h.setNote(note);
        h.setChangedBy(actor);
        historyRepository.save(h);
    }

    private Booking require(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> NotFoundException.of("Booking", bookingId));
    }

    private void assertParticipant(UUID userId, Booking booking) {
        if (booking.getCustomerId().equals(userId)
                || userId.equals(booking.getProfessionalId())
                || isAdmin(userId)) {
            return;
        }
        throw new ForbiddenException("You do not have access to this booking");
    }

    private void assertAssignedPro(UUID proId, Booking booking) {
        if (!proId.equals(booking.getProfessionalId())) {
            throw new ForbiddenException("This booking is not assigned to you");
        }
    }

    private boolean isAdmin(UUID userId) {
        return userRepository.findById(userId).map(u -> u.getRole() == Role.ADMIN).orElse(false);
    }

    private String join(String a, String b) {
        return b == null || b.isBlank() ? a : a + ", " + b;
    }

    private String generateCode() {
        String code;
        do {
            code = "BK" + (10_000_000 + RANDOM.nextInt(90_000_000));
        } while (bookingRepository.findByCode(code).isPresent());
        return code;
    }

    private String generateCompletionOtp() {
        return String.valueOf(100_000 + RANDOM.nextInt(900_000));
    }

    private String hashOtp(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private List<BookingStatus> customerTab(String tab) {
        if (tab == null) return null;
        return switch (tab.toUpperCase()) {
            case "UPCOMING" -> List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED,
                    BookingStatus.ON_THE_WAY, BookingStatus.ARRIVED,
                    BookingStatus.IN_PROGRESS, BookingStatus.COMPLETED_PENDING_OTP);
            case "COMPLETED" -> List.of(BookingStatus.COMPLETED);
            case "CANCELLED" -> List.of(BookingStatus.CANCELLED, BookingStatus.REJECTED);
            default -> null;
        };
    }

    private List<BookingStatus> proTab(String tab) {
        if (tab == null) return null;
        return switch (tab.toUpperCase()) {
            case "REQUESTS" -> List.of(BookingStatus.PENDING);
            case "ACTIVE" -> List.of(BookingStatus.CONFIRMED, BookingStatus.ON_THE_WAY,
                    BookingStatus.ARRIVED, BookingStatus.IN_PROGRESS, BookingStatus.COMPLETED_PENDING_OTP);
            case "COMPLETED" -> List.of(BookingStatus.COMPLETED);
            default -> null;
        };
    }
}
