package com.example.accesachallenge.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class PriceId implements Serializable {
    private Long productId;
    private Long storeId;
    private LocalDate date;

    public PriceId() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var priceId = (PriceId) o;
        return Objects.equals(productId, priceId.productId) &&
                Objects.equals(storeId, priceId.storeId) &&
                Objects.equals(date, priceId.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, storeId, date);
    }
}
