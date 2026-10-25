
package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CreatePaymentRequest;
import com.example.ecommerce.dto.response.CheckoutSessionResponse;
import com.example.ecommerce.dto.response.PaymentIntentResponse;
import com.example.ecommerce.dto.response.PaymentResponse;
import com.example.ecommerce.dto.response.VerifyPaymentResponse;
import com.example.ecommerce.dto.webhook.StripeWebhookEventData;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final StripeCardService stripeCardService;
    private final StripeWebhookEventRepository stripeWebhookEventRepository;
    private final PaymentConfirmationService paymentConfirmationService;
    private final StripePaymentGateway stripePaymentGateway;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl = "http://localhost:3000";

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

    private User getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return user;
    }

    private Order getAuthorizedOrder(Long orderId) {
        Order order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        User user = getAuthenticatedUser();

        if (order.getUser() == null || !user.getId().equals(order.getUser().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Order does not belong to the authenticated user"
            );
        }

        return order;
    }

    private String frontendUrl(String path) {
        String baseUrl = frontendBaseUrl != null ? frontendBaseUrl : "http://localhost:3000";
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + path;
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
                                frontendUrl("/payment-success?session_id={CHECKOUT_SESSION_ID}")
                        )
                        .setCancelUrl(frontendUrl("/create-order"))
                        .setClientReferenceId(orderId.toString())
                        .putMetadata("orderId", orderId.toString())
                        .setPaymentIntentData(
                                SessionCreateParams.PaymentIntentData.builder()
                                        .putMetadata("orderId", orderId.toString())
                                        .build()
                        );

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
    public void handleStripeWebhook(StripeWebhookEventData event) {
        if (stripeWebhookEventRepository.existsByStripeEventId(event.eventId())) {
            log.info("Stripe event already processed. eventId={}", event.eventId());
            return;
        }

        switch (event.eventType()) {
            case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
            case "payment_intent.payment_failed", "payment_intent.canceled" ->
                    handlePaymentIntentFailed(event);
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" ->
                    handleCheckoutSessionSucceeded(event);
            case "checkout.session.expired", "checkout.session.async_payment_failed" ->
                    handleCheckoutSessionFailed(event);
            default -> log.info(
                    "Ignoring unsupported Stripe event. eventId={}, type={}",
                    event.eventId(),
                    event.eventType()
            );
        }

        // The marker is written in the same transaction as the business changes.
        // If processing fails, both are rolled back and Stripe can retry safely.
        int marked = stripeWebhookEventRepository.markProcessed(
                event.eventType(),
                event.eventId(),
                LocalDateTime.now()
        );

        if (marked == 0) {
            log.info(
                    "Stripe event was already marked by another transaction. eventId={}",
                    event.eventId()
            );
        }
    }

    private void handlePaymentIntentSucceeded(StripeWebhookEventData event) {
        if (!"succeeded".equalsIgnoreCase(event.stripeStatus())) {
            log.warn(
                    "payment_intent.succeeded event has unexpected status. eventId={}, status={}",
                    event.eventId(),
                    event.stripeStatus()
            );
            return;
        }

        Optional<Payment> paymentOptional =
                findPaymentForSuccessfulEvent(event.objectId(), event.orderId());

        if (paymentOptional.isEmpty()) {
            log.warn(
                    "No payment found for payment_intent.succeeded. eventId={}, paymentIntentId={}, orderId={}",
                    event.eventId(),
                    event.objectId(),
                    event.orderId()
            );
            return;
        }

        Payment payment = paymentOptional.get();

        PaymentConfirmationResult result = paymentConfirmationService.confirmVerifiedPayment(
                payment.getOrder().getId(),
                payment.getId(),
                event.objectId(),
                event.amount(),
                event.currency(),
                event.orderId() != null ? event.orderId().toString() : null
        );

        logConfirmationResult(event, result);
    }

    private void handlePaymentIntentFailed(StripeWebhookEventData event) {
        Optional<Payment> paymentOptional = findByProviderPaymentId(event.objectId());

        if (paymentOptional.isEmpty()) {
            log.warn(
                    "No payment found for failed/canceled payment intent. eventId={}, paymentIntentId={}",
                    event.eventId(),
                    event.objectId()
            );
            return;
        }

        paymentConfirmationService.markPaymentFailed(paymentOptional.get().getId());
    }

    private void handleCheckoutSessionSucceeded(StripeWebhookEventData event) {
        if (!"paid".equalsIgnoreCase(event.stripeStatus())) {
            log.info(
                    "Checkout session is not paid yet. eventId={}, sessionId={}, paymentStatus={}",
                    event.eventId(),
                    event.objectId(),
                    event.stripeStatus()
            );
            return;
        }

        Optional<Payment> paymentOptional =
                findPaymentForSuccessfulEvent(event.objectId(), event.orderId());

        if (paymentOptional.isEmpty()) {
            log.warn(
                    "No payment found for checkout session. eventId={}, sessionId={}, orderId={}",
                    event.eventId(),
                    event.objectId(),
                    event.orderId()
            );
            return;
        }

        Payment payment = paymentOptional.get();

        PaymentConfirmationResult result = paymentConfirmationService.confirmVerifiedPayment(
                payment.getOrder().getId(),
                payment.getId(),
                event.paymentIntentId(),
                event.amount(),
                event.currency(),
                event.orderId() != null ? event.orderId().toString() : null
        );

        logConfirmationResult(event, result);
    }

    private void handleCheckoutSessionFailed(StripeWebhookEventData event) {
        Optional<Payment> paymentOptional = findByProviderPaymentId(event.objectId());

        if (paymentOptional.isEmpty()) {
            log.warn(
                    "No payment found for failed/expired checkout session. eventId={}, sessionId={}",
                    event.eventId(),
                    event.objectId()
            );
            return;
        }

        paymentConfirmationService.markPaymentFailed(paymentOptional.get().getId());
    }

    private void logConfirmationResult(
            StripeWebhookEventData event,
            PaymentConfirmationResult result
    ) {
        if (result.isSuccessful()) {
            log.info(
                    "Stripe payment confirmed. eventId={}, orderId={}, paymentIntentId={}, status={}",
                    event.eventId(),
                    result.orderId(),
                    result.paymentIntentId(),
                    result.status()
            );
            return;
        }

        log.error(
                "Stripe payment confirmation rejected. eventId={}, orderId={}, status={}",
                event.eventId(),
                result.orderId(),
                result.status()
        );
    }

    private Optional<Payment> findPaymentForSuccessfulEvent(
            String providerPaymentId,
            Long orderId
    ) {
        Optional<Payment> direct = findByProviderPaymentId(providerPaymentId);

        if (direct.isPresent()) {
            return direct;
        }

        if (orderId == null) {
            return Optional.empty();
        }

        Optional<Payment> pendingPayment = paymentRepository
                .findFirstByOrder_IdAndStatusOrderByIdDesc(orderId, PaymentStatus.PENDING);

        if (pendingPayment.isPresent()
                && isSamePaymentAttempt(pendingPayment.get(), providerPaymentId)) {
            return pendingPayment;
        }

        Optional<Payment> latestPayment =
                paymentRepository.findFirstByOrder_IdOrderByIdDesc(orderId);

        if (latestPayment.isPresent()
                && isSamePaymentAttempt(latestPayment.get(), providerPaymentId)) {
            return latestPayment;
        }

        return Optional.empty();
    }

    private Optional<Payment> findByProviderPaymentId(String providerPaymentId) {
        try {
            return paymentRepository.findByProviderPaymentId(providerPaymentId);
        } catch (IncorrectResultSizeDataAccessException e) {
            log.error("Multiple payments found for providerPaymentId={}", providerPaymentId, e);
            return Optional.empty();
        }
    }

    private boolean isSamePaymentAttempt(Payment payment, String providerPaymentId) {
        String storedProviderPaymentId = payment.getProviderPaymentId();

        if (storedProviderPaymentId == null
                || storedProviderPaymentId.equals(providerPaymentId)) {
            return true;
        }

        // Checkout sessions are stored by session id first. The PaymentIntent id
        // arrives later, so a session can legitimately resolve to the same payment.
        return storedProviderPaymentId.startsWith("cs_");
    }

    @Override
    public VerifyPaymentResponse verifyPayment(Long orderId) {
        User user = getAuthenticatedUser();

        Order order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getUser() == null || !user.getId().equals(order.getUser().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Order does not belong to the authenticated user"
            );
        }

        Payment payment = paymentRepository
                .findFirstByOrder_IdAndStatusOrderByIdDesc(orderId, PaymentStatus.SUCCEEDED)
                .orElseGet(() -> paymentRepository
                        .findFirstByOrder_IdOrderByIdDesc(orderId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Payment has not been initialized for this order"
                        )));

        String providerPaymentId = payment.getProviderPaymentId();
        if (providerPaymentId == null || providerPaymentId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment has no Stripe reference"
            );
        }

        StripeVerificationSnapshot snapshot;
        try {
            snapshot = retrieveAndVerifyStripePayment(orderId, providerPaymentId);
        } catch (StripeException e) {
            log.error(
                    "Stripe verification failed. orderId={}, providerPaymentId={}",
                    orderId,
                    providerPaymentId,
                    e
            );
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not verify payment with Stripe",
                    e
            );
        }

        if (!snapshot.paid()) {
            log.info(
                    "Stripe payment is not completed. orderId={}, stripeStatus={}",
                    orderId,
                    snapshot.stripeStatus()
            );

            return new VerifyPaymentResponse(
                    orderId,
                    order.getStatus(),
                    payment.getStatus(),
                    snapshot.paymentIntentId(),
                    false,
                    "Payment is not completed"
            );
        }

        PaymentConfirmationResult result = paymentConfirmationService.confirmVerifiedPayment(
                orderId,
                payment.getId(),
                snapshot.paymentIntentId(),
                snapshot.amount(),
                snapshot.currency(),
                snapshot.stripeOrderId()
        );

        return mapVerificationResult(result);
    }

    private VerifyPaymentResponse mapVerificationResult(PaymentConfirmationResult result) {
        return switch (result.status()) {
            case CONFIRMED, ALREADY_CONFIRMED -> new VerifyPaymentResponse(
                    result.orderId(),
                    result.orderStatus(),
                    result.paymentStatus(),
                    result.paymentIntentId(),
                    true,
                    "Payment verified"
            );
            case ORDER_CANCELLED -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment succeeded but order is cancelled; manual review is required"
            );
            case AMOUNT_MISMATCH -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment amount does not match order total"
            );
            case CURRENCY_MISMATCH -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment currency does not match order currency"
            );
            case METADATA_MISMATCH -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment metadata does not match order"
            );
            case PROVIDER_ID_MISMATCH -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment provider reference does not match"
            );
            case PAYMENT_REFUNDED -> throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment was refunded"
            );
            case PAYMENT_NOT_FOUND -> throw new ResourceNotFoundException("Payment not found");
            case ORDER_NOT_FOUND -> throw new ResourceNotFoundException("Order not found");
        };
    }

    private StripeVerificationSnapshot retrieveAndVerifyStripePayment(
            Long orderId,
            String providerPaymentId
    ) throws StripeException {
        if (providerPaymentId.startsWith("cs_")) {
            Session session = stripePaymentGateway.retrieveCheckoutSession(providerPaymentId);
            String sessionOrderId = extractStripeOrderId(
                    session.getMetadata(),
                    session.getClientReferenceId()
            );
            validateStripeOrderId(orderId, sessionOrderId);

            String paymentIntentId = session.getPaymentIntent();

            if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
                return new StripeVerificationSnapshot(
                        false,
                        paymentIntentId,
                        session.getAmountTotal(),
                        session.getCurrency(),
                        sessionOrderId,
                        session.getPaymentStatus()
                );
            }

            if (paymentIntentId == null || paymentIntentId.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Paid checkout session has no payment intent"
                );
            }

            PaymentIntent paymentIntent =
                    stripePaymentGateway.retrievePaymentIntent(paymentIntentId);
            String paymentIntentOrderId = extractStripeOrderId(
                    paymentIntent.getMetadata(),
                    null
            );

            // Older sessions may not have copied metadata onto the PaymentIntent.
            // The session itself was already validated, so fall back to it.
            if (paymentIntentOrderId == null) {
                paymentIntentOrderId = sessionOrderId;
            }
            validateStripeOrderId(orderId, paymentIntentOrderId);

            return new StripeVerificationSnapshot(
                    "succeeded".equalsIgnoreCase(paymentIntent.getStatus()),
                    paymentIntent.getId(),
                    paymentIntent.getAmount(),
                    paymentIntent.getCurrency(),
                    paymentIntentOrderId,
                    paymentIntent.getStatus()
            );
        }

        if (providerPaymentId.startsWith("pi_")) {
            PaymentIntent paymentIntent =
                    stripePaymentGateway.retrievePaymentIntent(providerPaymentId);
            String paymentIntentOrderId = extractStripeOrderId(
                    paymentIntent.getMetadata(),
                    null
            );
            validateStripeOrderId(orderId, paymentIntentOrderId);

            return new StripeVerificationSnapshot(
                    "succeeded".equalsIgnoreCase(paymentIntent.getStatus()),
                    paymentIntent.getId(),
                    paymentIntent.getAmount(),
                    paymentIntent.getCurrency(),
                    paymentIntentOrderId,
                    paymentIntent.getStatus()
            );
        }

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Unsupported Stripe payment reference"
        );
    }

    private String extractStripeOrderId(
            Map<String, String> metadata,
            String clientReferenceId
    ) {
        if (metadata != null) {
            String orderId = metadata.get("orderId");
            if (orderId != null && !orderId.isBlank()) {
                return orderId;
            }
        }
        return clientReferenceId;
    }

    private void validateStripeOrderId(Long orderId, String stripeOrderId) {
        if (stripeOrderId == null || !stripeOrderId.equals(orderId.toString())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stripe payment metadata does not match order"
            );
        }
    }

    private record StripeVerificationSnapshot(
            boolean paid,
            String paymentIntentId,
            Long amount,
            String currency,
            String stripeOrderId,
            String stripeStatus
    ) {
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
