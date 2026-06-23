package com.worknear.api.professional;

import com.worknear.api.professional.domain.ProfessionalSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProfessionalSpecializationRepository extends JpaRepository<ProfessionalSpecialization, UUID> {
    List<ProfessionalSpecialization> findByProfessionalId(UUID professionalId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ProfessionalSpecialization s where s.professionalId = :professionalId")
    void deleteByProfessionalId(@Param("professionalId") UUID professionalId);
}
