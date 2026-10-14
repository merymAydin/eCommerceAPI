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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

         Optional<StripeObject> stripeObject =
                event.getDataObjectDeserializer().getObject();

        if (stripeObject.isEmpty()) {
            log.error("Could not deserialize Stripe event: {}", event.getId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Could not deserialize event");
        }

        if (!(stripeObject.get() instanceof PaymentIntent paymentIntent)) {
            log.error("Unexpected Stripe object for event: {}", event.getId());
            return ResponseEntity.badRequest().body("Invalid event object");
        }


        if (eventType.equals("payment_intent.succeeded")) {

            paymentService.handlePaymentSuccess(paymentIntent.getId());
            log.info("Payment succeeded. PaymentIntent ID: {}", paymentIntent.getId());

        } else if (eventType.equals("payment_intent.payment_failed")) {

            paymentService.handlePaymentFailure(paymentIntent.getId());
            log.warn("Payment failed. PaymentIntent ID: {}", paymentIntent.getId());
        }

        return ResponseEntity.ok("Webhook processed");
    }
}