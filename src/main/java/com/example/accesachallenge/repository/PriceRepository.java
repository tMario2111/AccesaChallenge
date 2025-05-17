package com.example.accesachallenge.repository;

import com.example.accesachallenge.model.Price;
import com.example.accesachallenge.model.PriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, PriceId> {
    @Query(value = """
            WITH DISCOUNTED_PRICES AS (
                 SELECT\s
                     P1.PRODUCT_ID,
                     P1.DATE,
                     (1 - COALESCE(D1.DISCOUNT_PERCENTAGE, 0)/100.0) * P1.PRICE AS PRICE,
                     P1.STORE_ID,
                     P1.CURRENCY,
                     D1.DISCOUNT_PERCENTAGE
                 FROM PRICES P1
                 LEFT JOIN DISCOUNTS D1\s
                     ON P1.PRODUCT_ID = D1.PRODUCT_ID\s
                     AND P1.STORE_ID = D1.STORE_ID\s
                     AND P1.DATE BETWEEN D1.START_DATE AND D1.END_DATE
                 WHERE\s
                     P1.DATE = :targetDate
             )
             SELECT * FROM DISCOUNTED_PRICES P WHERE P.PRODUCT_ID = :productId AND P.PRICE =
                 (SELECT MIN(P1.PRICE) FROM DISCOUNTED_PRICES P1 WHERE P1.PRODUCT_ID = :productId) 
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findCheapestPriceWithDiscounts(
            @Param("productId") Long productId,
            @Param("targetDate") LocalDate targetDate
    );

    Optional<Price> findPriceById(PriceId id);
}
