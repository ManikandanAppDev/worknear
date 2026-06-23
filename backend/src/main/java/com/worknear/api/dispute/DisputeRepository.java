package com.worknear.api.dispute;

import com.worknear.api.dispute.domain.Dispute;
import com.worknear.api.dispute.domain.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    Page<Dispute> findByStatusOrderByCreatedAtAsc(DisputeStatus status, Pageable pageable);

    Page<Dispute> findByRaisedByOrderByCreatedAtDesc(UUID raisedBy, Pageable pageable);
}
