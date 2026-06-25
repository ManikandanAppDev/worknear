package com.worknear.api.catalog.domain;

import com.worknear.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "service_categories")
public class ServiceCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String icon;
    private String color;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    /** Platform reference/starting price for the category, managed from the admin dashboard. */
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice = BigDecimal.ZERO;
}
