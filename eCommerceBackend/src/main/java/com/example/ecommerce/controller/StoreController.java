package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.CreateStoreRequest;
import com.example.ecommerce.dto.request.UpdateStoreRequest;
import com.example.ecommerce.dto.response.StoreResponse;
import com.example.ecommerce.service.interfaces.StoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/stores")
public class StoreController {
    private StoreService storeService;
    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/{id}")
    public StoreResponse getStoreById(@PathVariable Long id){
        return storeService.findById(id);
    }

    @GetMapping("/name")
    public StoreResponse getStoreByName( @RequestParam String name){
        return storeService.findStoreByName(name);
    }

    @PostMapping
    public StoreResponse createStore(@Valid @RequestBody CreateStoreRequest createStoreRequest){
        return storeService.createStore(createStoreRequest);
    }

    @PutMapping("/{id}")
    public StoreResponse updateStore(@PathVariable Long id,@Valid @RequestBody UpdateStoreRequest updateStoreRequest){
        return storeService.updateStore(id,updateStoreRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteStore(@PathVariable Long id){
        storeService.deleteStore(id);
    }
}
