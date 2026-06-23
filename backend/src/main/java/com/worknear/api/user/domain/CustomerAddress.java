package com.worknear.api.user.domain;

import com.worknear.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "customer_addresses")
public class CustomerAddress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String label;

    @Column(nullable = false)
    private String line1;

    private String line2;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress = false;
}
