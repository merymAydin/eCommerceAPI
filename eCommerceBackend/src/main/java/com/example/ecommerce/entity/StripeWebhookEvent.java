package com.example.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(schema = "public", name = "webhooks")
public class StripeWebhookEvent extends BaseEntity{

    private String eventType;

    @Column(nullable = false, unique = true)
    private String stripeEventId;

    private LocalDateTime processedAt;

}
