package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.CreateOrderRequest;
import com.example.ecommerce.dto.request.UpdateOrderRequest;
import com.example.ecommerce.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest orderCreateRequest);
    OrderResponse updateOrder(Long id, UpdateOrderRequest updateOrderRequest);
    OrderResponse findById(Long id);
    List<OrderResponse> findAllOrdersByUserId();
    void deleteOrder(Long id);
}
