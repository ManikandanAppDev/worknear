package com.worknear.api.professional;

import com.worknear.api.professional.domain.ProfessionalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProfessionalDocumentRepository extends JpaRepository<ProfessionalDocument, UUID> {
    List<ProfessionalDocument> findByProfessionalId(UUID professionalId);
}
