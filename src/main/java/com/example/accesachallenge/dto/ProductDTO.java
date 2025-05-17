package com.example.accesachallenge.dto;

import jakarta.annotation.Nullable;

import java.math.BigDecimal;

public record ProductDTO(
        String productId,
        String name,
        String brand,
        String category,
        Double packageQuantity,
        String packageUnit,
        BigDecimal price,
        String currency,
        @Nullable BigDecimal discountPercentage
) {
}
