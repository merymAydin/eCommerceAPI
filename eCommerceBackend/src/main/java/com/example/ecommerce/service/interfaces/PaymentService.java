package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.CreatePaymentRequest;
import com.example.ecommerce.dto.response.CheckoutSessionResponse;
import com.example.ecommerce.dto.response.PaymentIntentResponse;
import com.example.ecommerce.dto.response.PaymentResponse;
import com.example.ecommerce.entity.StripeWebhookEvent;


public interface PaymentService {
    PaymentResponse findById(Long id);
    PaymentResponse createPayment(CreatePaymentRequest request);
    void deletePayment(Long id);
    CheckoutSessionResponse createCheckoutSession(Long orderId);
    PaymentIntentResponse createPaymentIntent(Long orderId);
    PaymentIntentResponse createSavedCardPaymentIntent(Long orderId, String paymentMethodId);
    void handleStripeWebhook(String stripeEventId, String eventType, String providerPaymentId);
}
