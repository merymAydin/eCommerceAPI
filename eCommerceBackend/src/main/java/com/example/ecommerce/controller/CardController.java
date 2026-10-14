package com.example.ecommerce.controller;

import com.example.ecommerce.dto.response.SavedCardResponse;
import com.example.ecommerce.dto.response.SetupIntentResponse;
import com.example.ecommerce.service.StripeCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/card")
@RequiredArgsConstructor
public class CardController {

    private final StripeCardService stripeCardService;

    @PostMapping("/setup-intent")
    public SetupIntentResponse createSetupIntent() {
        return stripeCardService.createSetupIntent();
    }

    @GetMapping
    public List<SavedCardResponse> getSavedCards() {
        return stripeCardService.getSavedCards();
    }

    @DeleteMapping("/{paymentMethodId}")
    public void deleteCard(@PathVariable String paymentMethodId) {
        stripeCardService.deleteCard(paymentMethodId);
    }
}