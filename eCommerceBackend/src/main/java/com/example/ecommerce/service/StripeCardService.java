package com.example.ecommerce.service;

import com.example.ecommerce.dto.response.SavedCardResponse;
import com.example.ecommerce.dto.response.SetupIntentResponse;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodListParams;
import com.stripe.param.SetupIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StripeCardService {

    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof User authenticatedUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private String getOrCreateStripeCustomer(User user) throws StripeException {
        if (user.getStripeCustomerId() != null &&
                !user.getStripeCustomerId().isBlank()) {
            return user.getStripeCustomerId();
        }

        CustomerCreateParams customerParams = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getUserName())
                .build();

        Customer customer = Customer.create(customerParams);

        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);

        return customer.getId();
    }

    public SetupIntentResponse createSetupIntent() {
        try {
            User user = getAuthenticatedUser();
            String customerId = getOrCreateStripeCustomer(user);

            SetupIntentCreateParams params =
                    SetupIntentCreateParams.builder()
                            .setCustomer(customerId)
                            .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION)
                            .build();

            SetupIntent setupIntent = SetupIntent.create(params);

            return new SetupIntentResponse(setupIntent.getClientSecret());

        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not initialize card setup",
                    e
            );
        }
    }

    public List<SavedCardResponse> getSavedCards() {
        try {
            User user = getAuthenticatedUser();

            if (user.getStripeCustomerId() == null ||
                    user.getStripeCustomerId().isBlank()) {
                return List.of();
            }

            PaymentMethodListParams params =
                    PaymentMethodListParams.builder()
                            .setCustomer(user.getStripeCustomerId())
                            .setType(PaymentMethodListParams.Type.CARD)
                            .build();

            return PaymentMethod.list(params)
                    .getData()
                    .stream()
                    .map(paymentMethod -> {
                        var card = paymentMethod.getCard();

                        return new SavedCardResponse(
                                paymentMethod.getId(),
                                card.getBrand(),
                                card.getLast4(),
                                card.getExpMonth(),
                                card.getExpYear()
                        );
                    })
                    .toList();

        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not retrieve saved cards",
                    e
            );
        }
    }


    public String getCustomerIdForPaymentMethod(String paymentMethodId) {
        try {
            User user = getAuthenticatedUser();

            if (user.getStripeCustomerId() == null ||
                    user.getStripeCustomerId().isBlank()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found");
            }

            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

            if (!"card".equals(paymentMethod.getType()) ||
                    !user.getStripeCustomerId().equals(paymentMethod.getCustomer())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found");
            }

            return user.getStripeCustomerId();
        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not verify saved card",
                    e
            );
        }
    }

    public void deleteCard(String paymentMethodId) {
        try {
            User user = getAuthenticatedUser();

            if (user.getStripeCustomerId() == null ||
                    user.getStripeCustomerId().isBlank()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found");
            }

            PaymentMethodListParams params =
                    PaymentMethodListParams.builder()
                            .setCustomer(user.getStripeCustomerId())
                            .setType(PaymentMethodListParams.Type.CARD)
                            .build();

            boolean belongsToUser = PaymentMethod.list(params)
                    .getData()
                    .stream()
                    .anyMatch(card -> card.getId().equals(paymentMethodId));

            if (!belongsToUser) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found");
            }

            PaymentMethod.retrieve(paymentMethodId).detach();

        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not delete card",
                    e
            );
        }
    }
}