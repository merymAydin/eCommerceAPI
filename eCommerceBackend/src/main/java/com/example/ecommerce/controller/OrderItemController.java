package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.CreateOrderItemRequest;
import com.example.ecommerce.dto.request.UpdateOrderItemsRequest;
import com.example.ecommerce.dto.response.OrderItemResponse;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.service.interfaces.OrderItemService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orderItems")
@AllArgsConstructor
public class OrderItemController {
    private OrderItemService orderItemService;
    @GetMapping("/id")
    public OrderItemResponse getById(@PathVariable Long id){
        return orderItemService.findById(id);
    }
    @PutMapping("/id")
    public OrderItemResponse updateOrderItem(@PathVariable Long id, @Valid @RequestBody UpdateOrderItemsRequest request){
        return orderItemService.updateOrderItem(id, request);
    }
    @PostMapping
    public OrderItemResponse createOrderItem(@Valid @RequestBody CreateOrderItemRequest request){
        return orderItemService.createOrderItem(request);
    }
    @DeleteMapping("/id")
    public void deleteOrderItem(@PathVariable Long id){
        orderItemService.deleteOrderItem(id);
    }
}
