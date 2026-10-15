
package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CreatePaymentRequest;
import com.example.ecommerce.dto.response.CheckoutSessionResponse;
import com.example.ecommerce.dto.response.PaymentIntentResponse;
import com.example.ecommerce.dto.response.PaymentResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.repository.StripeWebhookEventRepository;
import com.example.ecommerce.service.interfaces.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final StripeCardService stripeCardService;
    private final StripeWebhookEventRepository stripeWebhookEventRepository;

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getOrder().getId(),
                payment.getProvider(),
                payment.getProviderPaymentId(),
                payment.getAmount(),
                payment.getStatus()
        );
    }

    private void toSet(
            Payment payment,
            Order order,
            String provider,
            String providerPaymentId,
            BigDecimal amount
    ) {
        payment.setOrder(order);
        payment.setProvider(provider);
        payment.setProviderPaymentId(providerPaymentId);
        payment.setAmount(amount);
    }

    private BigDecimal calculateOrderTotal(Order order) {
        return order.getOrderItems()
                .stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order getAuthorizedOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        if (!user.getId().equals(order.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return order;
    }

    @Override
    public PaymentResponse findById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not Found"));

        return toPaymentResponse(payment);
    }

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment();

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        payment.setStatus(PaymentStatus.PENDING);

        toSet(
                payment,
                order,
                request.provider(),
                request.providerPaymentId(),
                request.amount()
        );

        return toPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not Found"));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User user = (User) authentication.getPrincipal();

        if (!user.getId().equals(payment.getOrder().getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        paymentRepository.deleteById(id);
    }


    @Override
    public CheckoutSessionResponse createCheckoutSession(Long orderId) {
        Order order = getAuthorizedOrder(orderId);

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

    @Override
    public PaymentIntentResponse createSavedCardPaymentIntent(
            Long orderId,
            String paymentMethodId
    ) {
        Order order = getAuthorizedOrder(orderId);


        String customerId =
                stripeCardService.getCustomerIdForPaymentMethod(paymentMethodId);

        BigDecimal orderTotal = calculateOrderTotal(order);
        long amountInKurus = orderTotal.movePointRight(2).longValueExact();

        if (amountInKurus <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order amount must be greater than zero"
            );
        }

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInKurus)
                        .setCurrency("try")
                        .setCustomer(customerId)
                        .setPaymentMethod(paymentMethodId)
                        .putMetadata("orderId", orderId.toString())
                        .build();

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            Payment payment = new Payment();
            payment.setProvider("STRIPE");
            payment.setOrder(order);
            payment.setProviderPaymentId(paymentIntent.getId());
            payment.setAmount(orderTotal);
            payment.setCurrency("try");
            payment.setStatus(PaymentStatus.PENDING);

            paymentRepository.save(payment);

            return new PaymentIntentResponse(paymentIntent.getClientSecret());

        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment provider error",
                    e
            );
        }
    }


    @Transactional

    @Override
    public void handleStripeWebhook(String stripeEventId, String eventType, String providerPaymentId) {
        int inserted = stripeWebhookEventRepository.addEventId(eventType,stripeEventId,LocalDateTime.now());
        if (inserted == 0) {
            return;
        }

        Payment payment = paymentRepository.findByProviderPaymentId(providerPaymentId).orElseThrow(()->new ResourceNotFoundException("Payment not found"));

        if (eventType.equals("payment_intent.succeeded")) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.getOrder().setStatus(OrderStatus.CONFIRMED);
        } else if (
                eventType.equals("payment_intent.payment_failed")
                        && payment.getStatus() != PaymentStatus.SUCCEEDED
        ) {
            payment.setStatus(PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);


    }

    @Override
    public PaymentIntentResponse createPaymentIntent(Long orderId) {


        Order order = getAuthorizedOrder(orderId);

        BigDecimal orderTotal = calculateOrderTotal(order);


        long amountInKurus = orderTotal
                .movePointRight(2)
                .longValueExact();

        if (amountInKurus <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order amount must be greater than zero"
            );
        }


        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInKurus)
                        .setCurrency("try")
                        .putMetadata("orderId", orderId.toString())
                        .build();

        try {

            PaymentIntent paymentIntent = PaymentIntent.create(params);


            Payment payment = new Payment();
            payment.setProvider("STRIPE");
            payment.setOrder(order);
            payment.setProviderPaymentId(paymentIntent.getId());
            payment.setAmount(orderTotal);
            payment.setCurrency("try");
            payment.setStatus(PaymentStatus.PENDING);

            paymentRepository.save(payment);


            return new PaymentIntentResponse(
                    paymentIntent.getClientSecret()
            );

        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment provider error",
                    e
            );
        }
    }
}
