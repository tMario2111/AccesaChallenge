package com.example.accesachallenge.dto;

import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiscountDTO(
        String storeName,
        String productId,
        String name,
        String brand,
        String category,
        Double packageQuantity,
        String packageUnit,
        BigDecimal price,
        String currency,
        BigDecimal discountPercentage,
        LocalDate endDate) {
}
