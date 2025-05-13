package com.example.accesachallenge.repository;

import com.example.accesachallenge.model.Discount;
import com.example.accesachallenge.model.DiscountId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, DiscountId> {
}
