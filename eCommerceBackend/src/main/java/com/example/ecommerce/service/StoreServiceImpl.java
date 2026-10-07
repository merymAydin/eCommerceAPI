package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CreateStoreRequest;
import com.example.ecommerce.dto.request.UpdateStoreRequest;
import com.example.ecommerce.dto.response.StoreResponse;
import com.example.ecommerce.entity.Store;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.StoreRepository;
import com.example.ecommerce.service.interfaces.StoreService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;

    private StoreResponse toResponse(Store store) {
        return new StoreResponse(
                store.getUser().getId(),
                store.getStoreName(),
                store.getPhone(),
                store.getTaxNo(),
                store.getBankAccount()
        );
    }

    private void toSet(Store store,String storeName,String phone,String taxNo,String bankAccount) {
        store.setStoreName(storeName);
        store.setPhone(phone);
        store.setTaxNo(taxNo);
        store.setBankAccount(bankAccount);
    }

    @Override
    public StoreResponse findById(Long id) {
        Store store = storeRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Store not found"));
        return toResponse(store);
    }

    @Override
    public StoreResponse findStoreByName(String storeName) {
        Store store = storeRepository.findByStoreName(storeName);
        return toResponse(store);
    }

    @Override
    public StoreResponse createStore(CreateStoreRequest storeRequest) {
        Store store = new Store();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        store.setUser(user);
        toSet(store,storeRequest.storeName(), storeRequest.phone(), storeRequest.taxNo(),storeRequest.bankAccount());
        Store savedStore = storeRepository.save(store);
        return toResponse(savedStore);
    }

    @Override
    public StoreResponse updateStore(Long id, UpdateStoreRequest storeRequest) {
        Store store = storeRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Store not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if(!user.getId().equals(store.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        toSet(store,storeRequest.storeName(), storeRequest.phone(), storeRequest.taxNo(),storeRequest.bankAccount());
        Store updatedStore = storeRepository.save(store);
        return toResponse(updatedStore);
    }

    @Override
    public void deleteStore(Long id) {
        Store store = storeRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Store not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        if(!user.getId().equals(store.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        storeRepository.deleteById(id);

    }
}
