package com.example.accesachallenge.repository;

import com.example.accesachallenge.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findProductByName(String name);
    Optional<Product> findProductByProductId(Long id);
}
