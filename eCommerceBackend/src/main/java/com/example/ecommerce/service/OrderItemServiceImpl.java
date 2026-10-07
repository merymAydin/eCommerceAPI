package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CreateOrderItemRequest;
import com.example.ecommerce.dto.request.UpdateOrderItemsRequest;
import com.example.ecommerce.dto.request.UpdateOrderRequest;
import com.example.ecommerce.dto.response.OrderItemResponse;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.service.interfaces.OrderItemService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private OrderItemRepository orderItemRepository;

    private OrderItemResponse toOrderItemResponse(OrderItem orderItemResponse) {
        return new OrderItemResponse(
                orderItemResponse.getId(),
                orderItemResponse.getProduct().getId(),
                orderItemResponse.getStore().getId(),
                orderItemResponse.getQuantity(),
                orderItemResponse.getUnitPrice()
        );
    }


    @Override
    public OrderItemResponse findById(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id).orElse(null);
        return toOrderItemResponse(orderItem);
    }

    @Override
    public OrderItemResponse createOrderItem(CreateOrderItemRequest req) {
       OrderItem orderItem = new OrderItem();
       orderItem.setQuantity(req.quantity());
       return toOrderItemResponse(orderItemRepository.save(orderItem));
    }

    @Override
    public OrderItemResponse updateOrderItem(Long id,UpdateOrderItemsRequest req) {
        OrderItem orderItem = orderItemRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Order item not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if (!user.getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        orderItem.setQuantity(req.quantity());
        return toOrderItemResponse(orderItemRepository.save(orderItem));
    }

    @Override
    public void deleteOrderItem(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if (!user.getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        orderItemRepository.deleteById(id);
    }
}
