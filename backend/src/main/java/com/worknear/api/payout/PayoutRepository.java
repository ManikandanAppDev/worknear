package com.worknear.api.payout;

import com.worknear.api.payout.domain.Payout;
import com.worknear.api.payout.domain.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PayoutRepository extends JpaRepository<Payout, UUID> {
    Page<Payout> findByProfessionalIdOrderByCreatedAtDesc(UUID professionalId, Pageable pageable);

    Page<Payout> findByStatusOrderByCreatedAtAsc(PayoutStatus status, Pageable pageable);

    long countByStatus(PayoutStatus status);
}
