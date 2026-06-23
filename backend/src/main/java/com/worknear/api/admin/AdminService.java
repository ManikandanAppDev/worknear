package com.worknear.api.admin;

import com.worknear.api.admin.dto.DashboardResponse;
import com.worknear.api.admin.dto.DisputeResponse;
import com.worknear.api.admin.dto.ResolveDisputeRequest;
import com.worknear.api.admin.dto.VerificationDecisionRequest;
import com.worknear.api.admin.dto.VerificationQueueItem;
import com.worknear.api.booking.BookingMapper;
import com.worknear.api.booking.BookingRepository;
import com.worknear.api.booking.domain.BookingStatus;
import com.worknear.api.booking.dto.BookingResponse;
import com.worknear.api.catalog.ServiceCategoryRepository;
import com.worknear.api.catalog.domain.ServiceCategory;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.dispute.DisputeRepository;
import com.worknear.api.dispute.domain.Dispute;
import com.worknear.api.dispute.domain.DisputeStatus;
import com.worknear.api.notification.NotificationService;
import com.worknear.api.payout.PayoutRepository;
import com.worknear.api.payout.domain.PayoutStatus;
import com.worknear.api.professional.ProfessionalProfileRepository;
import com.worknear.api.professional.ProfessionalServiceRepository;
import com.worknear.api.professional.domain.ProfessionalProfile;
import com.worknear.api.professional.domain.VerificationStatus;
import com.worknear.api.user.UserRepository;
import com.worknear.api.user.domain.User;
import com.worknear.api.wallet.WalletService;
import com.worknear.api.wallet.domain.TransactionReason;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProfessionalProfileRepository profileRepository;
    private final ProfessionalServiceRepository proServiceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final DisputeRepository disputeRepository;
    private final PayoutRepository payoutRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        Instant monthAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        return new DashboardResponse(
                bookingRepository.sumCommissionSince(monthAgo),
                bookingRepository.sumCompletedAmountSince(monthAgo),
                bookingRepository.count(),
                bookingRepository.countByCreatedAtAfter(weekAgo),
                profileRepository.countByVerificationStatus(VerificationStatus.APPROVED),
                profileRepository.countByVerificationStatus(VerificationStatus.PENDING),
                disputeRepository.findByStatusOrderByCreatedAtAsc(DisputeStatus.OPEN, Pageable.unpaged()).getTotalElements(),
                payoutRepository.countByStatus(PayoutStatus.REQUESTED)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<VerificationQueueItem> verificationQueue(VerificationStatus status, Pageable pageable) {
        VerificationStatus filter = status == null ? VerificationStatus.PENDING : status;
        Page<ProfessionalProfile> page = profileRepository.findByVerificationStatus(filter, pageable);

        Map<UUID, ServiceCategory> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(ServiceCategory::getId, Function.identity()));

        return PageResponse.from(page, profile -> {
            User user = userRepository.findById(profile.getUserId()).orElse(null);
            List<String> services = proServiceRepository.findByProfessionalId(profile.getId()).stream()
                    .map(s -> categories.containsKey(s.getCategoryId()) ? categories.get(s.getCategoryId()).getName() : null)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            return new VerificationQueueItem(
                    profile.getId(), profile.getUserId(),
                    user != null ? user.getFullName() : null,
                    user != null ? user.getPhone() : null,
                    services, services.size(), profile.getVerificationStatus());
        });
    }

    @Transactional
    public VerificationQueueItem decideVerification(UUID profileId, VerificationDecisionRequest req) {
        ProfessionalProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> NotFoundException.of("Professional profile", profileId));

        VerificationStatus newStatus = switch (req.decision()) {
            case APPROVE -> VerificationStatus.APPROVED;
            case REJECT -> VerificationStatus.REJECTED;
            case MORE_INFO -> VerificationStatus.MORE_INFO;
        };
        profile.setVerificationStatus(newStatus);

        notificationService.notifyUser(profile.getUserId(), "VERIFICATION_" + newStatus.name(),
                "Verification " + newStatus.name().toLowerCase(),
                req.note() != null ? req.note() : "Your verification status is now " + newStatus.name());

        User user = userRepository.findById(profile.getUserId()).orElse(null);
        return new VerificationQueueItem(profile.getId(), profile.getUserId(),
                user != null ? user.getFullName() : null, user != null ? user.getPhone() : null,
                List.of(), 0, newStatus);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> bookings(BookingStatus status, Pageable pageable) {
        Page<com.worknear.api.booking.domain.Booking> page = status == null
                ? bookingRepository.findAll(pageable)
                : bookingRepository.findByStatus(status, pageable);
        return PageResponse.from(page, bookingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<DisputeResponse> disputes(DisputeStatus status, Pageable pageable) {
        DisputeStatus filter = status == null ? DisputeStatus.OPEN : status;
        return PageResponse.from(
                disputeRepository.findByStatusOrderByCreatedAtAsc(filter, pageable),
                DisputeResponse::from);
    }

    @Transactional
    public DisputeResponse resolveDispute(UUID disputeId, UUID adminId, ResolveDisputeRequest req) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> NotFoundException.of("Dispute", disputeId));
        dispute.setStatus(req.resolution() == ResolveDisputeRequest.Resolution.RESOLVED
                ? DisputeStatus.RESOLVED : DisputeStatus.REJECTED);
        dispute.setResolutionNote(req.note());
        dispute.setResolvedBy(adminId);

        if (req.resolution() == ResolveDisputeRequest.Resolution.RESOLVED
                && req.refundAmount() != null && req.refundAmount().signum() > 0) {
            bookingRepository.findById(dispute.getBookingId()).ifPresent(b ->
                    walletService.credit(b.getCustomerId(), req.refundAmount(), TransactionReason.REFUND,
                            b.getId(), "Dispute refund for booking " + b.getCode()));
        }
        return DisputeResponse.from(dispute);
    }

    @Transactional
    public void setUserBlocked(UUID userId, boolean blocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("User", userId));
        user.setStatus(blocked ? com.worknear.api.user.domain.UserStatus.BLOCKED
                : com.worknear.api.user.domain.UserStatus.ACTIVE);
    }
}
