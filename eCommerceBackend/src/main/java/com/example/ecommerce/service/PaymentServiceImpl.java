package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CreatePaymentRequest;
import com.example.ecommerce.dto.request.UpdatePaymentRequest;
import com.example.ecommerce.dto.response.PaymentResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.PaymentStatus;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.service.interfaces.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orderRepository;
    private PaymentRepository paymentRepository;

    private PaymentResponse toPaymentResponse(Payment payment){
        return new PaymentResponse(
                payment.getOrder().getId(),
                payment.getProvider(),
                payment.getProviderPaymentId(),
                payment.getAmount(),
                payment.getStatus()
        );
    }

    private void toSet(        Payment payment,
                               Order order,
                               String provider,
                               String providerPaymentId,
                               BigDecimal amount,
                               PaymentStatus status){
        payment.setOrder(order);
        payment.setProvider(provider);
        payment.setProviderPaymentId(providerPaymentId);
        payment.setAmount(amount);
        payment.setStatus(status);

    }

    @Override
    public PaymentResponse findById(Long id) {
        Payment  payment = paymentRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Payment not Found"));
        return toPaymentResponse(payment);
    }


    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Payment p = new Payment();
        Order o = orderRepository.findById(request.orderId()).orElseThrow(()->new ResourceNotFoundException("Order not found"));
        toSet(p,o,request.provider(),request.providerPaymentId(),request.amount(),request.status());
        return toPaymentResponse(paymentRepository.save(p));
    }


    @Override
    public PaymentResponse updatePayment(Long id, UpdatePaymentRequest request) {
        Payment p = paymentRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Payment not Found"));
        Order o = orderRepository.findById(request.orderId()).orElseThrow(()->new ResourceNotFoundException("Order not found"));

        toSet(p,o,request.provider(),request.providerPaymentId(),request.amount(),request.status());
        return toPaymentResponse(paymentRepository.save(p));
    }

    @Override
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Payment not Found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if(!user.getId().equals(payment.getOrder().getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        paymentRepository.deleteById(id);
    }
}
