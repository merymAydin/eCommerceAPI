package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.CreateOrderItemRequest;
import com.example.ecommerce.dto.request.UpdateOrderItemsRequest;
import com.example.ecommerce.dto.response.OrderItemResponse;

public interface OrderItemService {
    OrderItemResponse findById(Long id);
    OrderItemResponse createOrderItem(CreateOrderItemRequest req);
    OrderItemResponse updateOrderItem(Long id,UpdateOrderItemsRequest req);
    void deleteOrderItem(Long id);
}
