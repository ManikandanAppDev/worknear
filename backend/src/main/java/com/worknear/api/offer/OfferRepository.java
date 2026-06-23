package com.worknear.api.offer;

import com.worknear.api.offer.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    List<Offer> findByActiveTrue();

    Optional<Offer> findByCodeIgnoreCase(String code);
}
