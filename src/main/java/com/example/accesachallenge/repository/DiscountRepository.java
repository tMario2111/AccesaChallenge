package com.example.accesachallenge.repository;

import com.example.accesachallenge.model.Discount;
import com.example.accesachallenge.model.DiscountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface DiscountRepository extends JpaRepository<Discount, DiscountId> {
    // Gets the best 5 discounts available on the given date
    // Also check whether the discount also has a corresponding product price on the given date
    @Query(value = """
             SELECT * FROM DISCOUNTS D WHERE :date BETWEEN D.START_DATE AND D.END_DATE
                 AND (SELECT COUNT(*) FROM PRICES P WHERE P.STORE_ID = D.STORE_ID AND\s
                 P.PRODUCT_ID = D.PRODUCT_ID AND P.DATE = :date) > 0
                 ORDER BY D.DISCOUNT_PERCENTAGE DESC LIMIT 5;
            \s""", nativeQuery = true)
    List<Object[]> findBestDiscounts(@Param("date") LocalDate date);
}
