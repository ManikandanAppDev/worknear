package com.worknear.api.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock gateway active while {@code worknear.payment.mock=true}. No external calls.
 */
@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public GatewayOrder createOrder(BigDecimal amount, String currency, String receipt) {
        String orderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        log.info("[MOCK PAYMENT] created order {} for {} {}", orderId, amount, currency);
        return new GatewayOrder(orderId, amount, currency);
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        return true;
    }

    @Override
    public String publicKey() {
        return "mock_key_id";
    }
}
