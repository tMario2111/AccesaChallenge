package com.example.accesachallenge.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "prices")
public class Price {
    @EmbeddedId
    private PriceId id;

    @MapsId("productId")
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @MapsId("storeId")
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    private BigDecimal price;
    private String currency;

    public Price() {
    }

    public Price(PriceId id, BigDecimal price, String currency) {
        this.id = id;
        this.price = price;
        this.currency = currency;
    }

    public PriceId getId() {
        return id;
    }

    public void setId(PriceId id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
