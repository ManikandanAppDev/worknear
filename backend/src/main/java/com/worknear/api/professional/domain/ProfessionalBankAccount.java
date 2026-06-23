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
@Table(name = "professional_bank_accounts")
public class ProfessionalBankAccount extends BaseEntity {

    @Column(name = "professional_id", nullable = false, unique = true)
    private UUID professionalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankMethod method;

    @Column(name = "upi_id")
    private String upiId;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_number")
    private String accountNumber;

    private String ifsc;
}
