package com.example.accesachallenge.controller;

import com.example.accesachallenge.dto.DiscountDTO;
import com.example.accesachallenge.dto.ProductDTO;
import com.example.accesachallenge.dto.UpcomingDiscountDTO;
import com.example.accesachallenge.model.Price;
import com.example.accesachallenge.model.PriceId;
import com.example.accesachallenge.repository.DiscountRepository;
import com.example.accesachallenge.repository.PriceRepository;
import com.example.accesachallenge.repository.ProductRepository;
import com.example.accesachallenge.repository.StoreRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ShoppingController {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final DiscountRepository discountRepository;

    private BigDecimal applyDiscount(BigDecimal price, BigDecimal discount) {
        BigDecimal multiplier = BigDecimal.ONE.subtract(
                discount.divide(BigDecimal.valueOf(100),
                        2, RoundingMode.HALF_UP)
        );
        return price.multiply(multiplier);
    }

    // TODO: Don't hardcode date
    private final String currentDate = "2025-05-01";

    public ShoppingController(StoreRepository storeRepository,
                              ProductRepository productRepository,
                              PriceRepository priceRepository,
                              DiscountRepository discountRepository) {
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.discountRepository = discountRepository;
    }

    @PostMapping(value = "/optimize-shopping-list",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<ProductDTO>>> optimizeShoppingList(@RequestBody List<String> productIds) {
        Map<String, List<ProductDTO>> response = new HashMap<>();
        for (var productId : productIds) {
            var product = productRepository.findById(Long.parseLong(productId.substring(1)));
            if (product.isEmpty()) {
                continue;
            }

            var result = priceRepository.findCheapestPriceWithDiscounts(Long.parseLong(productId.substring(1)),
                    LocalDate.parse(currentDate));
            if (result.isEmpty()) {
                continue;
            }
            var store = storeRepository.findById((Long) result.get(0)[3]);

            if (store.isEmpty()) {
                continue;
            }

            var productDTO = new ProductDTO(productId, product.get().getName(),
                    product.get().getBrand(), product.get().getCategory(),
                    product.get().getPackageQuantity(),
                    product.get().getPackageUnit(),
                    (BigDecimal) result.get(0)[2], (String) result.get(0)[4], (BigDecimal) result.get(0)[5]);

            if (!response.containsKey(store.get().getStoreName())) {
                response.put(store.get().getStoreName(), new ArrayList<>());
            }
            response.get(store.get().getStoreName()).add(productDTO);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/best-discounts",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DiscountDTO>> bestDiscounts() {
        List<DiscountDTO> response = new ArrayList<>();
        var result = discountRepository.findBestDiscounts(LocalDate.parse(currentDate));

        for (var row : result) {
            var product = productRepository.findProductByProductId((Long) row[3]);
            if (product.isEmpty()) {
                continue;
            }
            var store = storeRepository.findById((Long) row[4]);
            if (store.isEmpty()) {
                continue;
            }
            var price = priceRepository.findPriceById(new PriceId(product.get().getProductId(), store.get().getStoreId(),
                    LocalDate.parse(currentDate)));
            if (price.isEmpty()) {
                continue;
            }
            BigDecimal discountPercentage = (BigDecimal) row[2];
            BigDecimal discountedPrice = applyDiscount(price.get().getPrice(), discountPercentage);
            response.add(new DiscountDTO(
                    store.get().getStoreName(),
                    "P" + product.get().getProductId(),
                    product.get().getName(),
                    product.get().getBrand(),
                    product.get().getCategory(),
                    product.get().getPackageQuantity(),
                    product.get().getPackageUnit(),
                    discountedPrice,
                    price.get().getCurrency(),
                    discountPercentage,
                    ((java.sql.Date) row[0]).toLocalDate()
            ));
        }

        return ResponseEntity.ok(response);
    }

    // Returns discounts with the latest start date
    @PostMapping(value = "/new-discounts",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UpcomingDiscountDTO>> newDiscounts() {
        List<UpcomingDiscountDTO> response = new ArrayList<>();
        var result = discountRepository.findLatestDiscounts();
        for (var discount : result) {
            final var store = discount.getStore();
            final var product = discount.getProduct();
            response.add(new UpcomingDiscountDTO(
                    store.getStoreName(),
                    "P" + product.getProductId(),
                    product.getName(),
                    product.getBrand(),
                    product.getCategory(),
                    product.getPackageQuantity(),
                    product.getPackageUnit(),
                    discount.getDiscountPercentage(),
                    discount.getId().getStartDate(),
                    discount.getId().getEndDate()
            ));
        }

        return ResponseEntity.ok(response);
    }
}
