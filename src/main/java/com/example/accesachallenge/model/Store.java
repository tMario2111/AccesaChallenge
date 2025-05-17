package com.example.accesachallenge.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "stores")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long storeId;

    private String storeName;

    @OneToMany(mappedBy = "store")
    private List<Price> prices;

    @OneToMany(mappedBy = "store")
    private List<Discount> discounts;

    public Store() {
    }

    public long getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public String toString() {
        return "Store{" +
                "storeId=" + storeId +
                ", storeName='" + storeName + '\'' +
                ", prices=" + prices +
                ", discounts=" + discounts +
                '}';
    }
}
