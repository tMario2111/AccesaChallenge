package com.example.accesachallenge.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "price_alerts")
public class PriceAlert {
    @EmbeddedId
    private PriceAlertId id;

    private BigDecimal currentPrice;

    @MapsId("productId")
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public PriceAlert() {
    }

    public PriceAlert(PriceAlertId id, BigDecimal currentPrice) {
        this.id = id;
        this.currentPrice = currentPrice;
    }

    public PriceAlertId getId() {
        return id;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setId(PriceAlertId id) {
        this.id = id;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
