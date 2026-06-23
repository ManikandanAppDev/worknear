package com.worknear.api.professional.domain;

import com.worknear.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "professional_documents")
public class ProfessionalDocument extends BaseEntity {

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "original_name")
    private String originalName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "review_note")
    private String reviewNote;
}
