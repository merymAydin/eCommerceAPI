package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    Optional<Payment> findFirstByOrder_IdAndStatusOrderByIdDesc(
            Long orderId,
            PaymentStatus status
    );

    Optional<Payment> findFirstByOrder_IdOrderByIdDesc(Long orderId);

    /**
     * Locks the payment row for the duration of the confirmation transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") Long id);
}
