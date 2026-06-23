package com.worknear.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 40) String label,
        @NotBlank @Size(max = 200) String line1,
        @Size(max = 200) String line2,
        @Size(max = 80) String city,
        @Size(max = 80) String state,
        @Size(max = 12) String pincode,
        Double latitude,
        Double longitude,
        boolean makeDefault
) {}
