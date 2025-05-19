package com.example.accesachallenge.repository;

import com.example.accesachallenge.model.Price;
import com.example.accesachallenge.model.PriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, PriceId> {
    @Query(value = """
            SELECT * FROM (
                SELECT
                    P1.PRODUCT_ID,
                    P1.DATE,
                    (1 - (COALESCE(D1.DISCOUNT_PERCENTAGE, 0)/100.0)) * P1.PRICE AS PRICE,
                    P1.STORE_ID,
                    P1.CURRENCY,
                    D1.DISCOUNT_PERCENTAGE
                FROM PRICES P1
                LEFT JOIN DISCOUNTS D1
                    ON P1.PRODUCT_ID = D1.PRODUCT_ID
                    AND P1.STORE_ID = D1.STORE_ID
                    AND P1.DATE BETWEEN D1.START_DATE AND D1.END_DATE
                WHERE P1.DATE = :targetDate
            ) P
            WHERE P.PRODUCT_ID = :productId
                AND P.PRICE = (
                    SELECT MIN(
                        (1 - (COALESCE(D2.DISCOUNT_PERCENTAGE, 0)/100.0)) * P2.PRICE
                    )
                    FROM PRICES P2
                    LEFT JOIN DISCOUNTS D2
                        ON P2.PRODUCT_ID = D2.PRODUCT_ID
                        AND P2.STORE_ID = D2.STORE_ID
                        AND P2.DATE BETWEEN D2.START_DATE AND D2.END_DATE
                    WHERE P2.PRODUCT_ID = :productId AND P2.DATE = :targetDate
                )
            ORDER BY P.STORE_ID
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findCheapestPriceWithDiscounts(
            @Param("productId") Long productId,
            @Param("targetDate") LocalDate targetDate
    );

    Optional<Price> findPriceById(PriceId id);

    @Query(value = """
            SELECT P.DATE FROM PRICES P WHERE P.PRODUCT_ID = :productId ORDER BY P.DATE LIMIT 1
            """, nativeQuery = true)
    LocalDate findEarliestDateOfProductId(@Param("productId") Long productId);

    @Query(value = """
            SELECT P.DATE FROM PRICES P WHERE P.PRODUCT_ID = :productId ORDER BY P.DATE DESC LIMIT 1
            """, nativeQuery = true)
    LocalDate findLatestDateOfProductId(@Param("productId") Long productId);
}
