package com.worknear.api.offer;

import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.offer.dto.OfferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Offers")
@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferRepository offerRepository;

    @Operation(summary = "List active offers")
    @GetMapping
    public ApiResponse<List<OfferResponse>> activeOffers() {
        return ApiResponse.ok(offerRepository.findByActiveTrue().stream().map(OfferResponse::from).toList());
    }
}
