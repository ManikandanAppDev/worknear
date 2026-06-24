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
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    /** Authoritative timezone for all booking date/time validation (ignores the device clock). */
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int MAX_BOOKING_DAYS_AHEAD = 7;

    /** Statuses that count as an "active" booking for duplicate-prevention. */
    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(
            BookingStatus.PENDING, BookingStatus.CONFIRMED,
            BookingStatus.ON_THE_WAY, BookingStatus.IN_PROGRESS);

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
        booking.setPaymentMethod(req.paymentMethod());
        booking = bookingRepository.save(booking);

        recordHistory(booking, BookingStatus.PENDING, "Booking created", customerId);
        notificationService.notifyUser(req.professionalId(), "BOOKING_REQUEST",
                "New job request", "You have a new booking request " + booking.getCode());

        return mapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(UUID userId, UUID bookingId) {
        Booking booking = require(bookingId);
        assertParticipant(userId, booking);
        return mapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> listForCustomer(UUID customerId, String tab, Pageable pageable) {
        List<BookingStatus> statuses = customerTab(tab);
        var page = statuses == null
                ? bookingRepository.findByCustomerId(customerId, pageable)
                : bookingRepository.findByCustomerIdAndStatusIn(customerId, statuses, pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> listForProfessional(UUID proId, String tab, Pageable pageable) {
        List<BookingStatus> statuses = proTab(tab);
        var page = statuses == null
                ? bookingRepository.findByProfessionalId(proId, pageable)
                : bookingRepository.findByProfessionalIdAndStatusIn(proId, statuses, pageable);
        return PageResponse.from(page, mapper::toResponse);
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
        return mapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse reject(UUID proId, UUID bookingId, String note) {
        Booking booking = require(bookingId);
        assertAssignedPro(proId, booking);
        transition(booking, BookingStatus.REJECTED, proId, note);
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_REJECTED",
                "Booking declined", "Your booking " + booking.getCode() + " was declined");
        return mapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse updateStatus(UUID proId, UUID bookingId, BookingStatus target, String note) {
        Booking booking = require(bookingId);
        assertAssignedPro(proId, booking);
        if (target == BookingStatus.REJECTED || target == BookingStatus.CONFIRMED || target == BookingStatus.CANCELLED) {
            throw new BadRequestException("Use the dedicated accept/reject/cancel endpoints for this transition");
        }
        transition(booking, target, proId, note);
        if (target == BookingStatus.COMPLETED) {
            completeBooking(booking);
        }
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_UPDATE",
                "Booking update", "Your booking " + booking.getCode() + " is now " + target.name());
        return mapper.toResponse(booking);
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
        boolean sufficient = !quote.feeApplies() || balance.compareTo(quote.feeAmount()) >= 0;
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
            walletService.debit(userId, preview.feeAmount(), TransactionReason.CANCELLATION_FEE,
                    booking.getId(), "Cancellation fee for booking " + booking.getCode());
            booking.setCancellationFee(preview.feeAmount());
        }
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
        return mapper.toResponse(booking);
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
        notificationService.notifyUser(booking.getCustomerId(), "BOOKING_CANCELLED",
                "Booking cancelled", "Booking " + booking.getCode() + " was cancelled");
        return mapper.toResponse(booking);
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
        return mapper.toResponse(booking);
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
        return mapper.toResponse(booking);
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
        booking.setCompletedAt(Instant.now());
        if (booking.getProfessionalId() != null) {
            walletService.credit(booking.getProfessionalId(), booking.getProEarning(),
                    TransactionReason.EARNING, booking.getId(),
                    "Earning for booking " + booking.getCode());
            professionalProfileRepository.findByUserId(booking.getProfessionalId()).ifPresent(p -> {
                p.setJobsCompleted(p.getJobsCompleted() + 1);
            });
        }
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

    private List<BookingStatus> customerTab(String tab) {
        if (tab == null) return null;
        return switch (tab.toUpperCase()) {
            case "UPCOMING" -> List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED,
                    BookingStatus.ON_THE_WAY, BookingStatus.IN_PROGRESS);
            case "COMPLETED" -> List.of(BookingStatus.COMPLETED);
            case "CANCELLED" -> List.of(BookingStatus.CANCELLED, BookingStatus.REJECTED);
            default -> null;
        };
    }

    private List<BookingStatus> proTab(String tab) {
        if (tab == null) return null;
        return switch (tab.toUpperCase()) {
            case "REQUESTS" -> List.of(BookingStatus.PENDING);
            case "ACTIVE" -> List.of(BookingStatus.CONFIRMED, BookingStatus.ON_THE_WAY, BookingStatus.IN_PROGRESS);
            case "COMPLETED" -> List.of(BookingStatus.COMPLETED);
            default -> null;
        };
    }
}
