package com.example.ecommerce.security.config;


import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@Data
public class StripeConfig {
    @Value("${stripe.secret}")
    private String secret;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secret;
        log.info("Stripe secret is initialized");
        if (webhookSecret != null && webhookSecret.startsWith("whsec_")){
            log.info("Webhook secret found");
        }else  {
            log.warn("Webhook secret not found");
        }
    }


}
