package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.CreateStoreRequest;
import com.example.ecommerce.dto.request.UpdateStoreRequest;
import com.example.ecommerce.dto.response.StoreResponse;

public interface StoreService {
    StoreResponse findById(Long id);
    StoreResponse findStoreByName(String storeName);
    StoreResponse createStore(CreateStoreRequest storeRequest);
    StoreResponse updateStore(Long id, UpdateStoreRequest storeRequest);
    void deleteStore(Long id);
}
