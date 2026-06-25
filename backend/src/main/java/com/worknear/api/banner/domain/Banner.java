package com.worknear.api.banner.domain;

import com.worknear.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * An onboarding / promotional banner slide rendered in the app's onboarding carousel.
 * Fully admin-managed (create/update/reorder/deactivate) so marketing content can change
 * without an app release.
 */
@Getter
@Setter
@Entity
@Table(name = "banners")
public class Banner extends BaseEntity {

    private String title;

    @Column(columnDefinition = "text")
    private String subtitle;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "cta_label")
    private String ctaLabel;

    /** CUSTOMER, PROFESSIONAL or ALL — controls which onboarding flow shows the slide. */
    @Column(nullable = false)
    private String audience = "ALL";

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false)
    private boolean active = true;
}
