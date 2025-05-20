package com.example.accesachallenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FilteredProductPriceHistoryDTO(
        String storeName,
        String brand,
        String category
) {

}
