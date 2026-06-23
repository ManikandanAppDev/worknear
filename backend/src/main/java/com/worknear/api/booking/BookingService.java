package com.worknear.api.booking;

import com.worknear.api.booking.domain.Booking;
import com.worknear.api.booking.domain.BookingPhoto;
import com.worknear.api.booking.domain.BookingPhotoType;
import com.worknear.api.booking.domain.BookingStatus;
import com.worknear.api.booking.domain.BookingStatusHistory;
import com.worknear.api.booking.dto.BookingResponse;
import com.worknear.api.booking.dto.CreateBookingRequest;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final SecureRandom RANDOM = new SecureRandom();

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
        if (!req.slotEnd().isAfter(req.slotStart())) {
            throw new BadRequestException("Slot end time must be after start time");
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

    @Transactional
    public BookingResponse cancel(UUID userId, UUID bookingId, String reason) {
        Booking booking = require(bookingId);
        boolean isCustomer = booking.getCustomerId().equals(userId);
        boolean isPro = userId.equals(booking.getProfessionalId());
        if (!isCustomer && !isPro && !isAdmin(userId)) {
            throw new ForbiddenException("You cannot cancel this booking");
        }
        transition(booking, BookingStatus.CANCELLED, userId, reason);
        booking.setCancellationReason(reason);
        booking.setCancelledBy(isCustomer ? "CUSTOMER" : isPro ? "PROFESSIONAL" : "ADMIN");
        UUID notify = isCustomer ? booking.getProfessionalId() : booking.getCustomerId();
        if (notify != null) {
            notificationService.notifyUser(notify, "BOOKING_CANCELLED",
                    "Booking cancelled", "Booking " + booking.getCode() + " was cancelled");
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
