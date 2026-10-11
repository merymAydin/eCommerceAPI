package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.CreatePaymentRequest;
import com.example.ecommerce.dto.request.UpdatePaymentRequest;
import com.example.ecommerce.dto.response.CheckoutSessionResponse;
import com.example.ecommerce.dto.response.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

public interface PaymentService {
    PaymentResponse findById(Long id);
    PaymentResponse createPayment(CreatePaymentRequest request);
    void deletePayment(Long id);
    CheckoutSessionResponse createCheckoutSession(Long orderId);
}
