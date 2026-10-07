package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CreateOrderRequest;
import com.example.ecommerce.dto.request.UpdateOrderRequest;
import com.example.ecommerce.dto.response.OrderItemResponse;
import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.interfaces.OrderService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;


@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private OrderResponse toOrderResponse(Order order) {
        return new OrderResponse(
                order.getUser().getUserName(),
                order.getUser().getEmail(),
                order.getOrderItems()
                        .stream()
                        .map(item -> new OrderItemResponse(
                                item.getOrder().getId(),
                                item.getProduct().getId(),
                                item.getStore().getId(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .toList(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingDistrict(),
                order.getShippingPostalCode()
        );
    }

    @Transactional
    @Override
    public OrderResponse createOrder(CreateOrderRequest orderCreateRequest) {
        Order order = new Order();
        User user = userRepository.findByEmail(orderCreateRequest.email());
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderItems(
                orderCreateRequest
                        .orderItems()
                        .stream()
                        .map(item -> {
                            Product p = productRepository
                                    .findById(item.productId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                            OrderItem orderItem = new OrderItem();
                            orderItem.setOrder(order);
                            orderItem.setProduct(p);
                            orderItem.setStore(p.getStore());
                            orderItem.setQuantity(item.quantity());
                            orderItem.setUnitPrice(p.getPrice());

                            return orderItem;
                        })
                        .toList());
        order.setStatus(OrderStatus.PENDING);
        Order savedOrder = orderRepository.save(order);
        return toOrderResponse(savedOrder);
    }

    @Transactional
    @Override
    public OrderResponse updateOrder(Long id, UpdateOrderRequest updateOrderRequest) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if (!user.getId().equals(order.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        order.setOrderItems(updateOrderRequest.orderItems().stream().map(
                item->{
                    Product p = productRepository.findById(item.productId()).orElseThrow(() -> new RuntimeException("Product not found"));

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(p);
                    orderItem.setStore(p.getStore());
                    orderItem.setQuantity(item.quantity());
                    orderItem.setUnitPrice(p.getPrice());

                    return orderItem;
                }
        ).toList());
        order.setStatus(OrderStatus.valueOf(updateOrderRequest.status()));
        order.setShippingAddress(updateOrderRequest.shippingAddress());
        order.setShippingCity(updateOrderRequest.shippingCity());
        order.setShippingDistrict(updateOrderRequest.shippingDistrict());
        order.setShippingPostalCode(updateOrderRequest.shippingPostalCode());

        orderRepository.save(order);

        return toOrderResponse(order);
    }

    @Override
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if(!user.getId().equals(order.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> findAllOrdersByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticated = (User) authentication.getPrincipal();

        return orderRepository
                .findAllByUserId(authenticated.getId())
                .stream()
                .map(this::toOrderResponse).toList();
    }

    @Override
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if (!user.getId().equals(order.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        orderRepository.delete(order);
    }
}
