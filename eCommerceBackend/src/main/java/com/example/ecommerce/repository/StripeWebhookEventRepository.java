package com.example.ecommerce.repository;

import com.example.ecommerce.entity.StripeWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, Long> {
    @Modifying
    @Query(value = """
    INSERT INTO public.webhooks (event_type, stripe_event_id, processed_at)
    VALUES (:eventType, :stripeEventId, :processedAt)
    ON CONFLICT (stripe_event_id) DO NOTHING
    """, nativeQuery = true)
    int addEventId(
            @Param("eventType") String eventType,
            @Param("stripeEventId") String stripeEventId,
            @Param("processedAt") LocalDateTime processedAt
    );
}
