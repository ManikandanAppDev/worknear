package com.worknear.api.professional;

import com.worknear.api.professional.domain.ProfessionalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalServiceRepository extends JpaRepository<ProfessionalService, UUID> {
    List<ProfessionalService> findByProfessionalId(UUID professionalId);

    Optional<ProfessionalService> findByProfessionalIdAndCategoryId(UUID professionalId, UUID categoryId);

    List<ProfessionalService> findByCategoryIdAndActiveTrue(UUID categoryId);

    List<ProfessionalService> findByProfessionalIdIn(List<UUID> professionalIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ProfessionalService s where s.professionalId = :professionalId")
    void deleteByProfessionalId(@Param("professionalId") UUID professionalId);
}
