package com.worknear.api.professional;

import com.worknear.api.professional.domain.ProfessionalAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProfessionalAvailabilityRepository extends JpaRepository<ProfessionalAvailability, UUID> {
    List<ProfessionalAvailability> findByProfessionalIdOrderByDayOfWeekAsc(UUID professionalId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ProfessionalAvailability a where a.professionalId = :professionalId")
    void deleteByProfessionalId(@Param("professionalId") UUID professionalId);
}
