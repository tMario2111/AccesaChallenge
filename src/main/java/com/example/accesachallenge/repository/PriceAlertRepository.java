package com.example.accesachallenge.repository;

import com.example.accesachallenge.model.PriceAlert;
import com.example.accesachallenge.model.PriceAlertId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, PriceAlertId> {
    List<PriceAlert> findById_UserId(Long userId);
}
