package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.CreatePaymentRequest;
import com.example.ecommerce.dto.response.CheckoutSessionResponse;
import com.example.ecommerce.dto.response.PaymentIntentResponse;
import com.example.ecommerce.dto.response.PaymentResponse;
import com.example.ecommerce.dto.response.VerifyPaymentResponse;
import com.example.ecommerce.dto.webhook.StripeWebhookEventData;


public interface PaymentService {
    PaymentResponse findById(Long id);
    PaymentResponse createPayment(CreatePaymentRequest request);
    void deletePayment(Long id);
    CheckoutSessionResponse createCheckoutSession(Long orderId);
    PaymentIntentResponse createPaymentIntent(Long orderId);
    PaymentIntentResponse createSavedCardPaymentIntent(Long orderId, String paymentMethodId);
    VerifyPaymentResponse verifyPayment(Long orderId);
    void handleStripeWebhook(StripeWebhookEventData event);
}
