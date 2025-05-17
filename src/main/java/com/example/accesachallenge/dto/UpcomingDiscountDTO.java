package com.example.accesachallenge.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpcomingDiscountDTO(
        String storeName,
        String productId,
        String name,
        String brand,
        String category,
        Double packageQuantity,
        String packageUnit,
        BigDecimal discountPercentage,
        LocalDate startDate,
        LocalDate endDate) {
}
