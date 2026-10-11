package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.CreatePaymentRequest;
import com.example.ecommerce.dto.request.UpdatePaymentRequest;
import com.example.ecommerce.dto.response.CheckoutSessionResponse;
import com.example.ecommerce.dto.response.PaymentResponse;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.service.interfaces.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
    @PostMapping("/checkout-session/{orderId}")
    public CheckoutSessionResponse createCheckoutSession(
            @PathVariable Long orderId
    ) {
        return paymentService.createCheckoutSession(orderId);
    }

    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable Long id) {
         paymentService.deletePayment(id);
    }
}
