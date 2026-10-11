package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CreatePaymentRequest;
import com.example.ecommerce.dto.response.CheckoutSessionResponse;
import com.example.ecommerce.dto.response.PaymentResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.service.interfaces.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpSession;
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
    private final HttpSession httpSession;
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
                               BigDecimal amount){
        payment.setOrder(order);
        payment.setProvider(provider);
        payment.setProviderPaymentId(providerPaymentId);
        payment.setAmount(amount);

    }

    private BigDecimal calculateOrderTotal(Order order){
        return order.getOrderItems()
                .stream()
                .map(i->i.getUnitPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
        p.setStatus(PaymentStatus.PENDING);
        toSet(p,o,request.provider(),request.providerPaymentId(),request.amount());
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

    @Override
    public CheckoutSessionResponse createCheckoutSession(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(()->new ResourceNotFoundException("Order not found"));
        Authentication  authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if(!user.getId().equals(order.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        SessionCreateParams.Builder paramsBuilder =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(
                                "http://localhost:3000/payment-success?session_id={CHECKOUT_SESSION_ID}"
                        )
                        .setCancelUrl("http://localhost:3000/create-order");

        for (OrderItem item : order.getOrderItems()) {
            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(Long.valueOf(item.getQuantity()))
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("try")
                                            .setUnitAmount(
                                                    item.getUnitPrice()
                                                            .movePointRight(2)
                                                            .longValueExact()
                                            )
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(item.getProduct().getName())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }
        try {
            Session session = Session.create(paramsBuilder.build());
            Payment payment = new Payment();
            payment.setProvider("STRIPE");
            payment.setOrder(order);
            payment.setProviderPaymentId(session.getId());
            payment.setAmount(calculateOrderTotal(order));
            payment.setCurrency("try");
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);
            return new CheckoutSessionResponse(session.getUrl());
        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment provider error",
                    e
            );
        }
    }
}
