package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.CreatePaymentRequest;
import com.example.ecommerce.dto.request.UpdatePaymentRequest;
import com.example.ecommerce.dto.request.VerifyPaymentRequest;
import com.example.ecommerce.dto.response.CheckoutSessionResponse;
import com.example.ecommerce.dto.response.PaymentIntentResponse;
import com.example.ecommerce.dto.response.PaymentResponse;
import com.example.ecommerce.dto.response.VerifyPaymentResponse;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.service.interfaces.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable Long id) {
        return paymentService.findById(id);
    }
    @PostMapping
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest createPaymentRequest) {
        return paymentService.createPayment(createPaymentRequest);
    }

    /**
     * Authenticated fallback used by the payment success page.
     * Only {@code orderId} is accepted; all payment facts are read from Stripe.
     */
    @PostMapping("/verify")
    public VerifyPaymentResponse verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        return paymentService.verifyPayment(request.orderId());
    }
    @PostMapping("/checkout-session/{orderId}")
    public CheckoutSessionResponse createCheckoutSession(
            @PathVariable Long orderId
    ) {
        return paymentService.createCheckoutSession(orderId);
    }

    @PostMapping("/payment-intent/{orderId}/saved-card/{paymentMethodId}")
    public ResponseEntity<PaymentIntentResponse> createSavedCardPaymentIntent(
            @PathVariable Long orderId,
            @PathVariable String paymentMethodId
    ) {
        return ResponseEntity.ok(
                paymentService.createSavedCardPaymentIntent(orderId, paymentMethodId)
        );
    }

    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
    }
    @PostMapping("/payment-intent/{orderId}")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                paymentService.createPaymentIntent(orderId)
        );
    }
}
