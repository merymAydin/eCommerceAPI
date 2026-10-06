package com.example.ecommerce.dto.response;



import com.example.ecommerce.entity.PaymentStatus;


import java.math.BigDecimal;

public record PaymentResponse(
          Long orderId,
          String provider,
         String providerPaymentId,
        BigDecimal amount,
        PaymentStatus status
) {
}


