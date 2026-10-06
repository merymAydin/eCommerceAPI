package com.example.ecommerce.dto.request;

import com.example.ecommerce.entity.PaymentStatus;

import java.math.BigDecimal;

public record UpdatePaymentRequest(
        Long orderId,
        String provider,
        String providerPaymentId,
        BigDecimal amount,
        PaymentStatus status
) {
}
