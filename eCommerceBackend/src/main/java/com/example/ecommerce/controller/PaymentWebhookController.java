package com.example.ecommerce.controller;

import com.example.ecommerce.security.config.StripeConfig;
import com.example.ecommerce.service.interfaces.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequestMapping("/payments")
@AllArgsConstructor
public class PaymentWebhookController {

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

         if (!eventType.equals("payment_intent.succeeded")
                && !eventType.equals("payment_intent.payment_failed")) {

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
            log.error("Stripe event deserialization failed", e);
            return ResponseEntity.internalServerError()
                    .body("Event deserialization failed");
        }

        if (!(stripeObject instanceof PaymentIntent paymentIntent)) {
            log.error("Unexpected Stripe object for event: {}", event.getId());
            return ResponseEntity.badRequest().body("Invalid event object");
        }


        paymentService.handleStripeWebhook(
                event.getId(),
                eventType,
                paymentIntent.getId()
        );

        return ResponseEntity.ok("Webhook processed");
    }
}