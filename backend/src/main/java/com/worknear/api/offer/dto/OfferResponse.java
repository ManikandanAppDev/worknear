package com.worknear.api.offer.dto;

import com.worknear.api.offer.domain.DiscountType;
import com.worknear.api.offer.domain.Offer;

import java.math.BigDecimal;
import java.util.UUID;

public record OfferResponse(
        UUID id,
        String code,
        String title,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscount,
        BigDecimal minOrder
) {
    public static OfferResponse from(Offer o) {
        return new OfferResponse(o.getId(), o.getCode(), o.getTitle(), o.getDescription(),
                o.getDiscountType(), o.getDiscountValue(), o.getMaxDiscount(), o.getMinOrder());
    }
}
