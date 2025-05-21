package com.example.accesachallenge.model;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class PriceAlertId {
    private long userId;
    private long productId;

    public PriceAlertId() {
    }

    public PriceAlertId(long userId, long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public long getUserId() {
        return userId;
    }

    public long getProductId() {
        return productId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PriceAlertId that = (PriceAlertId) o;
        return userId == that.userId && productId == that.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, productId);
    }
}
