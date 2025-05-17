package com.example.accesachallenge.controller;

import com.example.accesachallenge.dto.ProductDTO;
import com.example.accesachallenge.repository.PriceRepository;
import com.example.accesachallenge.repository.ProductRepository;
import com.example.accesachallenge.repository.StoreRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShoppingController {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;

    public ShoppingController(StoreRepository storeRepository,
                              ProductRepository productRepository, PriceRepository priceRepository) {
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
    }

    @PostMapping(value = "/optimize-shopping-list",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<ProductDTO>>> optimizeShoppingList(@RequestBody List<String> productIds) {
        Map<String, List<ProductDTO>> response = new HashMap<>();
        for (var productId : productIds) {
            var product = productRepository.findById(Long.parseLong(productId.substring(1)));
            if (product.isEmpty()) {
                continue;
            }
            // TODO: Don't hardcode date
            var result = priceRepository.findCheapestPriceWithDiscounts(Long.parseLong(productId.substring(1)),
                    LocalDate.parse("2025-05-01"));
            if (result.isEmpty()) {
                continue;
            }
            var store = storeRepository.findById((Long) result.get(0)[3]);

            if (store.isEmpty()) {
                continue;
            }

            var productDTO = new ProductDTO(productId, product.get().getName(),
                    product.get().getBrand(), product.get().getCategory(),
                    product.get().getPackageQuantity(),
                    product.get().getPackageUnit(),
                    (BigDecimal) result.get(0)[2], (String) result.get(0)[4], (BigDecimal) result.get(0)[5]);

            if (!response.containsKey(store.get().getStoreName())) {
                response.put(store.get().getStoreName(), new ArrayList<>());
            }
            response.get(store.get().getStoreName()).add(productDTO);
        }
        return ResponseEntity.ok(response);
    }
}
