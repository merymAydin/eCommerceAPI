package com.example.ecommerce.controller;

import com.example.ecommerce.dto.webhook.StripeWebhookEventData;
import com.example.ecommerce.security.config.StripeConfig;
import com.example.ecommerce.service.interfaces.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;


@Slf4j
@RestController
@RequestMapping("/payments")
@AllArgsConstructor
public class PaymentWebhookController {

    private static final Set<String> PAYMENT_INTENT_EVENTS = Set.of(
            "payment_intent.succeeded",
            "payment_intent.payment_failed",
            "payment_intent.canceled"
    );

    private static final Set<String> CHECKOUT_SESSION_EVENTS = Set.of(
            "checkout.session.completed",
            "checkout.session.async_payment_succeeded",
            "checkout.session.async_payment_failed",
            "checkout.session.expired"
    );

    private final StripeConfig stripeConfig;
    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handlePaymentWebhook(
            @RequestHeader("Stripe-Signature") String signature,
            @RequestBody String payload) {

        Event event;

        try {
            event = Webhook.constructEvent(
                    payload,
                    signature,
                    stripeConfig.getWebhookSecret()
            );
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature");
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        String eventType = event.getType();

        log.info("Stripe webhook received. Event ID: {}, Type: {}",
                event.getId(), eventType);

        if (!PAYMENT_INTENT_EVENTS.contains(eventType)
                && !CHECKOUT_SESSION_EVENTS.contains(eventType)) {

            log.info("Ignoring unsupported Stripe event: {}", eventType);
            return ResponseEntity.ok("Event ignored");
        }

        StripeObject stripeObject;

        try {
            stripeObject = event.getDataObjectDeserializer()
                    .getObject()
                    .orElseGet(() -> {
                        try {
                            return event.getDataObjectDeserializer().deserializeUnsafe();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            log.error("Stripe event deserialization failed. Event ID: {}", event.getId(), e);
            return ResponseEntity.internalServerError()
                    .body("Event deserialization failed");
        }

        StripeWebhookEventData eventData;

        if (PAYMENT_INTENT_EVENTS.contains(eventType)) {
            if (!(stripeObject instanceof PaymentIntent paymentIntent)) {
                log.error("Unexpected Stripe object for event: {}", event.getId());
                return ResponseEntity.badRequest().body("Invalid event object");
            }

            eventData = new StripeWebhookEventData(
                    event.getId(),
                    eventType,
                    paymentIntent.getId(),
                    extractOrderId(paymentIntent.getMetadata(), null),
                    null,
                    paymentIntent.getAmount(),
                    paymentIntent.getCurrency(),
                    paymentIntent.getStatus()
            );
        } else {
            if (!(stripeObject instanceof Session session)) {
                log.error("Unexpected Stripe object for event: {}", event.getId());
                return ResponseEntity.badRequest().body("Invalid event object");
            }

            eventData = new StripeWebhookEventData(
                    event.getId(),
                    eventType,
                    session.getId(),
                    extractOrderId(session.getMetadata(), session.getClientReferenceId()),
                    session.getPaymentIntent(),
                    session.getAmountTotal(),
                    session.getCurrency(),
                    session.getPaymentStatus()
            );
        }

        try {
            paymentService.handleStripeWebhook(eventData);
        } catch (Exception e) {
            log.error(
                    "Stripe webhook processing failed. Event ID: {}, Type: {}",
                    event.getId(),
                    eventType,
                    e
            );
            return ResponseEntity.internalServerError()
                    .body("Webhook processing failed");
        }

        return ResponseEntity.ok("Webhook processed");
    }

    private Long extractOrderId(Map<String, String> metadata, String clientReferenceId) {
        String value = metadata != null ? metadata.get("orderId") : null;

        if (value == null || value.isBlank()) {
            value = clientReferenceId;
        }

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid orderId in Stripe event: {}", value);
            return null;
        }
    }
}
