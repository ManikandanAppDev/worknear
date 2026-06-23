package com.worknear.api.payment.gateway;

import java.math.BigDecimal;

/**
 * Abstraction over a payment gateway (e.g. Razorpay). The mock implementation
 * fabricates order/payment ids so the flow can be exercised without real keys.
 */
public interface PaymentGateway {

    GatewayOrder createOrder(BigDecimal amount, String currency, String receipt);

    /** Verifies a webhook/callback signature. Mock always returns true. */
    boolean verifySignature(String orderId, String paymentId, String signature);

    /** Client-facing key id for the SDK (mock returns a placeholder). */
    String publicKey();

    record GatewayOrder(String orderId, BigDecimal amount, String currency) {}
}
