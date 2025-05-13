package com.example.accesachallenge.repository;

import com.example.accesachallenge.model.Price;
import com.example.accesachallenge.model.PriceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<Price, PriceId> {
}
