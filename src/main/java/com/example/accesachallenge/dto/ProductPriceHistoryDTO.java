package com.example.accesachallenge.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductPriceHistoryDTO(
        LocalDate date,
        String storeName,
        BigDecimal price) {
}
