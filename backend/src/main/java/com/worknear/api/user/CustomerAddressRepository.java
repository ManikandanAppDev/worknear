package com.worknear.api.user;

import com.worknear.api.user.domain.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {
    List<CustomerAddress> findByUserIdOrderByDefaultAddressDescCreatedAtDesc(UUID userId);

    Optional<CustomerAddress> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("update CustomerAddress a set a.defaultAddress = false where a.userId = :userId")
    void clearDefaultForUser(@Param("userId") UUID userId);
}
