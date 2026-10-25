package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);

    /**
     * Loads an order together with its owner and items. Used by payment
     * creation/verification so lazily loaded order data is available outside
     * of the persistence context.
     */
    @EntityGraph(attributePaths = {"user", "orderItems"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findDetailedById(@Param("id") Long id);

    /**
     * Acquires a row lock so concurrent webhook and /verify calls cannot apply
     * conflicting order state transitions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);
}
