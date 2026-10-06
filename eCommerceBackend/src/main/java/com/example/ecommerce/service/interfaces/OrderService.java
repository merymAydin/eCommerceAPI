package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.CreateOrderRequest;
import com.example.ecommerce.dto.request.OrderUpdateRequest;

public interface OrderService {
    CreateOrderRequest createOrder(CreateOrderRequest orderCreateRequest);
    OrderUpdateRequest updateOrder(Long id, CreateOrderRequest orderCreateRequest);
}
