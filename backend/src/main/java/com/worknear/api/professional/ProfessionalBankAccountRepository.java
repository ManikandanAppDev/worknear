package com.worknear.api.professional;

import com.worknear.api.professional.domain.ProfessionalBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfessionalBankAccountRepository extends JpaRepository<ProfessionalBankAccount, UUID> {
    Optional<ProfessionalBankAccount> findByProfessionalId(UUID professionalId);
}
