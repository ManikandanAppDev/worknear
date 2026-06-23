package com.worknear.api.payment;

import com.worknear.api.booking.BookingRepository;
import com.worknear.api.booking.domain.Booking;
import com.worknear.api.common.exception.BadRequestException;
import com.worknear.api.common.exception.ForbiddenException;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.config.WorkNearProperties;
import com.worknear.api.payment.domain.Payment;
import com.worknear.api.payment.domain.PaymentPurpose;
import com.worknear.api.payment.domain.PaymentStatus;
import com.worknear.api.payment.dto.ConfirmPaymentRequest;
import com.worknear.api.payment.dto.CreateOrderRequest;
import com.worknear.api.payment.dto.OrderResponse;
import com.worknear.api.payment.dto.PaymentResponse;
import com.worknear.api.payment.gateway.PaymentGateway;
import com.worknear.api.wallet.WalletService;
import com.worknear.api.wallet.domain.TransactionReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentGateway gateway;
    private final WalletService walletService;
    private final WorkNearProperties properties;

    @Transactional
    public OrderResponse createOrder(UUID customerId, CreateOrderRequest req) {
        BigDecimal amount = resolveAmount(customerId, req);
        if (amount.signum() <= 0) {
            throw new BadRequestException("Amount must be positive");
        }

        PaymentGateway.GatewayOrder order = gateway.createOrder(amount, "INR", "rcpt_" + UUID.randomUUID());

        Payment payment = new Payment();
        payment.setCustomerId(customerId);
        payment.setBookingId(req.purpose() == PaymentPurpose.BOOKING ? req.bookingId() : null);
        payment.setAmount(amount);
        payment.setCurrency(order.currency());
        payment.setMethod(req.method());
        payment.setPurpose(req.purpose());
        payment.setGateway("MOCK");
        payment.setGatewayOrderId(order.orderId());
        payment.setStatus(PaymentStatus.CREATED);
        payment = paymentRepository.save(payment);

        return new OrderResponse(payment.getId(), order.orderId(), gateway.publicKey(),
                amount, order.currency(), payment.getStatus(), properties.payment().mock());
    }

    @Transactional
    public PaymentResponse confirm(UUID customerId, ConfirmPaymentRequest req) {
        Payment payment = paymentRepository.findByGatewayOrderId(req.gatewayOrderId())
                .orElseThrow(() -> NotFoundException.of("Payment", req.gatewayOrderId()));
        if (!payment.getCustomerId().equals(customerId)) {
            throw new ForbiddenException("This payment does not belong to you");
        }
        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            return PaymentResponse.from(payment);
        }
        if (!gateway.verifySignature(req.gatewayOrderId(), req.gatewayPaymentId(), req.signature())) {
            payment.setStatus(PaymentStatus.FAILED);
            throw new BadRequestException("Payment signature verification failed");
        }

        payment.setGatewayPaymentId(req.gatewayPaymentId() != null ? req.gatewayPaymentId()
                : "pay_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14));
        payment.setStatus(PaymentStatus.CAPTURED);

        if (payment.getPurpose() == PaymentPurpose.WALLET_TOPUP) {
            walletService.credit(customerId, payment.getAmount(), TransactionReason.TOPUP,
                    payment.getId(), "Wallet top-up");
        }
        return PaymentResponse.from(payment);
    }

    private BigDecimal resolveAmount(UUID customerId, CreateOrderRequest req) {
        if (req.purpose() == PaymentPurpose.BOOKING) {
            if (req.bookingId() == null) {
                throw new BadRequestException("bookingId is required for a booking payment");
            }
            Booking booking = bookingRepository.findById(req.bookingId())
                    .orElseThrow(() -> NotFoundException.of("Booking", req.bookingId()));
            if (!booking.getCustomerId().equals(customerId)) {
                throw new ForbiddenException("This booking does not belong to you");
            }
            return booking.getAmount();
        }
        if (req.amount() == null) {
            throw new BadRequestException("amount is required for a wallet top-up");
        }
        return req.amount();
    }
}
