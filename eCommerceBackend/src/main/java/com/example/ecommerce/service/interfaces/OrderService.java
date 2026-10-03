package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.OrderCreateRequest;
import com.example.ecommerce.dto.request.OrderUpdateRequest;

public interface OrderService {
    OrderCreateRequest createOrder(OrderCreateRequest orderCreateRequest);
    OrderUpdateRequest updateOrder(Long id,OrderCreateRequest orderCreateRequest);
}
