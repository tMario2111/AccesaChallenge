package com.example.accesachallenge.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private Long productId;

    private String name;
    private String category;
    private String brand;
    private Double packageQuantity;
    private String packageUnit;

    @OneToMany(mappedBy = "product")
    private List<Price> prices;

    @OneToMany(mappedBy = "product")
    private List<Discount> discounts;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String productName) {
        this.name = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String productCategory) {
        this.category = productCategory;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Double getPackageQuantity() {
        return packageQuantity;
    }

    public void setPackageQuantity(Double packageQuantity) {
        this.packageQuantity = packageQuantity;
    }

    public String getPackageUnit() {
        return packageUnit;
    }

    public void setPackageUnit(String packageUnit) {
        this.packageUnit = packageUnit;
    }
}
