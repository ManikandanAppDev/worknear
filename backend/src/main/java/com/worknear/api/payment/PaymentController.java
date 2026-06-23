package com.worknear.api.payment;

import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.payment.dto.ConfirmPaymentRequest;
import com.worknear.api.payment.dto.CreateOrderRequest;
import com.worknear.api.payment.dto.OrderResponse;
import com.worknear.api.payment.dto.PaymentResponse;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payments")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create a payment order (for a booking or wallet top-up)")
    @PostMapping("/orders")
    public ApiResponse<OrderResponse> createOrder(@CurrentUser UserPrincipal user,
                                                 @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(paymentService.createOrder(user.id(), request));
    }

    @Operation(summary = "Confirm a payment after gateway checkout (captures funds)")
    @PostMapping("/confirm")
    public ApiResponse<PaymentResponse> confirm(@CurrentUser UserPrincipal user,
                                               @Valid @RequestBody ConfirmPaymentRequest request) {
        return ApiResponse.ok(paymentService.confirm(user.id(), request));
    }
}
