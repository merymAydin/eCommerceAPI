package com.example.ecommerce.repository;

import com.example.ecommerce.entity.StripeWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, Long> {

    boolean existsByStripeEventId(String stripeEventId);

    /**
     * Marks an event as processed.
     *
     * <p>The explicit id comes from the same Hibernate sequence that owns the
     * {@code webhooks.id} column. This keeps the native insert compatible with the
     * existing ID strategy instead of relying on a non-existent column default.</p>
     */
    @Modifying
    @Query(value = """
    INSERT INTO public.webhooks (id, event_type, stripe_event_id, processed_at)
    VALUES (nextval('public.webhooks_seq'), :eventType, :stripeEventId, :processedAt)
    ON CONFLICT (stripe_event_id) DO NOTHING
    """, nativeQuery = true)
    int markProcessed(
            @Param("eventType") String eventType,
            @Param("stripeEventId") String stripeEventId,
            @Param("processedAt") LocalDateTime processedAt
    );
}
