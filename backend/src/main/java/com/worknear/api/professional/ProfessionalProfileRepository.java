package com.worknear.api.professional;

import com.worknear.api.professional.domain.ProfessionalProfile;
import com.worknear.api.professional.domain.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, UUID> {
    Optional<ProfessionalProfile> findByUserId(UUID userId);

    Page<ProfessionalProfile> findByVerificationStatus(VerificationStatus status, Pageable pageable);

    long countByVerificationStatus(VerificationStatus status);
}
