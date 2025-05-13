package com.example.accesachallenge.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "discounts")
public class Discount {
    @EmbeddedId
    private DiscountId id;

    @MapsId("productId")
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @MapsId("storeId")
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    private BigDecimal discountPercentage;

    public DiscountId getId() {
        return id;
    }

    public void setId(DiscountId id) {
        this.id = id;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
