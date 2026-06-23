package com.worknear.api.review;

import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.review.dto.CreateReviewRequest;
import com.worknear.api.review.dto.ReviewResponse;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Reviews")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Leave a review for a completed booking (customer)")
    @PostMapping
    public ApiResponse<ReviewResponse> create(@CurrentUser UserPrincipal user,
                                             @Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.ok(reviewService.create(user.id(), request));
    }

    @Operation(summary = "List reviews for a professional")
    @GetMapping("/professional/{professionalUserId}")
    public ApiResponse<PageResponse<ReviewResponse>> forProfessional(@PathVariable UUID professionalUserId,
                                                                     Pageable pageable) {
        return ApiResponse.ok(reviewService.forProfessional(professionalUserId, pageable));
    }
}
