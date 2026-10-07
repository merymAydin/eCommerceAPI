package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.CreatePaymentRequest;
import com.example.ecommerce.dto.request.UpdatePaymentRequest;
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
    @PutMapping("/{id}")
    public PaymentResponse updatePayment(@PathVariable Long id, @Valid @RequestBody UpdatePaymentRequest req) {
        return paymentService.updatePayment(id,req);
    }
    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable Long id) {
         paymentService.deletePayment(id);
    }
}
