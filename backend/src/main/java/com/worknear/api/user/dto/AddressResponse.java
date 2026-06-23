package com.worknear.api.user.dto;

import com.worknear.api.user.domain.CustomerAddress;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String label,
        String line1,
        String line2,
        String city,
        String state,
        String pincode,
        Double latitude,
        Double longitude,
        boolean isDefault
) {
    public static AddressResponse from(CustomerAddress a) {
        return new AddressResponse(a.getId(), a.getLabel(), a.getLine1(), a.getLine2(),
                a.getCity(), a.getState(), a.getPincode(), a.getLatitude(), a.getLongitude(), a.isDefaultAddress());
    }
}
