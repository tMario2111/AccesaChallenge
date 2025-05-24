package com.example.accesachallenge.controller;

import com.example.accesachallenge.dto.*;
import com.example.accesachallenge.model.Price;
import com.example.accesachallenge.model.PriceAlert;
import com.example.accesachallenge.model.PriceAlertId;
import com.example.accesachallenge.model.PriceId;
import com.example.accesachallenge.repository.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    private final PriceAlertRepository priceAlertRepository;

    private BigDecimal applyDiscount(BigDecimal price, BigDecimal discount) {
        BigDecimal multiplier = BigDecimal.ONE.subtract(
                discount.divide(BigDecimal.valueOf(100),
                        2, RoundingMode.HALF_UP)
        );
        return price.multiply(multiplier);
    }

    private final String currentDate = "2025-05-01";

    public ShoppingController(StoreRepository storeRepository,
                              ProductRepository productRepository,
                              PriceRepository priceRepository,
                              DiscountRepository discountRepository,
                              PriceAlertRepository priceAlertRepository) {
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.discountRepository = discountRepository;
        this.priceAlertRepository = priceAlertRepository;
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

    @GetMapping(value = "/best-discounts")
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
    // If there are discounts added in the last 24h, these are returned
    @GetMapping(value = "/new-discounts")
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

    @Transactional
    @PostMapping(value = "/product-price-history",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductPriceHistoryDTO>> productPriceHistory(
            @RequestBody ProductIdRequestDTO productIdDTO) {

        List<ProductPriceHistoryDTO> response = new ArrayList<>();

        var productIdAsLong = Long.parseLong(productIdDTO.productId().substring(1));

        var earliestDate = priceRepository.findEarliestDateOfProductId(productIdAsLong);
        var latestDate = priceRepository.findLatestDateOfProductId(productIdAsLong);

        earliestDate.datesUntil(latestDate.plusDays(1)).forEach((date) -> {
            var result = priceRepository.findCheapestPriceWithDiscounts(productIdAsLong, date);
            if (!result.isEmpty()) {
                var store = storeRepository.findById((Long) result.get(0)[3]);
                if (store.isEmpty()) {
                    return;
                }
                response.add(new ProductPriceHistoryDTO(date, store.get().getStoreName(),
                        (BigDecimal) result.get(0)[2]));
            }
        });

        return ResponseEntity.ok(response);
    }

    public record DatePrice(LocalDate date, BigDecimal price) {
    }

    @PostMapping(value = "/filtered-product-price-history",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> filteredProductPriceHistory(
            @RequestBody FilteredProductPriceHistoryDTO dto) {
        List<DatePrice> response = new ArrayList<>();
        if (dto.brand() == null && dto.category() == null &&
                dto.storeName() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "At least one filter (store, brand, or category) is required!"));
        }

        if (dto.brand() != null && dto.category() != null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cannot use both brand and category filters!"));
        }

        var store = storeRepository.findByStoreName(dto.storeName());
        if (store.isEmpty() && dto.storeName() != null) {
            return ResponseEntity.badRequest().body(response);
        }
        var storeId = (dto.storeName() == null) ? null : store.get().getStoreId();

        var result = priceRepository.findFilteredPriceHistory(storeId, dto.brand(), dto.category());
        for (var row : result) {
            response.add(new DatePrice(((java.sql.Date) row[0]).toLocalDate(), (BigDecimal) row[1]));
        }

        return ResponseEntity.ok(response);
    }

    public record ProductPricePerUnit(String productId, String productName, Double quantity, String unit,
                                      BigDecimal price) {
    }

    @PostMapping(value = "/value-per-unit",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> valuePerUnit(@RequestBody List<String> productIds) {
        List<ProductPricePerUnit> response = new ArrayList<>();
        for (var id : productIds) {
            var product = productRepository.findProductByProductId(Long.parseLong(id.substring(1)));
            if (product.isEmpty()) {
                continue;
            }

            var result = priceRepository.findCheapestPriceWithDiscounts(Long.parseLong(id.substring(1)),
                    LocalDate.parse(currentDate));
            if (result.isEmpty()) {
                continue;
            }

            var price = (BigDecimal) result.get(0)[2];
            var packageUnit = product.get().getPackageUnit();
            BigDecimal packageQuantity = BigDecimal.valueOf(product.get().getPackageQuantity());

            BigDecimal normalizationFactor = switch (packageUnit) {
                case "g", "ml" -> new BigDecimal("1000");
                default -> BigDecimal.ONE;
            };

            BigDecimal pricePerUnit = price.divide(packageQuantity, 4, RoundingMode.HALF_UP);
            BigDecimal normalizedPrice = pricePerUnit.multiply(normalizationFactor);

            String newUnit = switch (packageUnit) {
                case "g", "kg" -> "kg";
                case "ml", "l" -> "l";
                default -> packageUnit;
            };
            response.add(new ProductPricePerUnit(id, product.get().getName(), 1.0, newUnit, normalizedPrice));
        }

        return ResponseEntity.ok(response);
    }

    public record ProductUserIdDTO(Long userId, String productId) {
    }

    @PostMapping(value = "/add-price-alert",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addPriceAlert(@RequestBody ProductUserIdDTO dto) {
        var productId = Long.parseLong(dto.productId.substring(1));
        var product = productRepository.findProductByProductId(productId);
        if (product.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var result = priceRepository.findCheapestPriceWithDiscounts(productId, LocalDate.parse(currentDate));
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var priceAlertId = new PriceAlertId(dto.userId(), productId);

        var newPriceAlert = new PriceAlert();
        newPriceAlert.setId(priceAlertId);
        newPriceAlert.setProduct(product.get());
        newPriceAlert.setCurrentPrice((BigDecimal) result.get(0)[2]);
        priceAlertRepository.save(newPriceAlert);

        return ResponseEntity.ok().build();
    }

    public record UserIdDTO(Long userId) {
    }

    @PostMapping(value = "/fetch-price-alerts",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> fetchPriceAlerts(@RequestBody UserIdDTO dto) {
        record ProductStorePrice(String productId, String storeName, BigDecimal price) {
        }

        var response = new ArrayList<ProductStorePrice>();

        var alerts = priceAlertRepository.findById_UserId(dto.userId);
        for (var alert : alerts) {
            // Simulate another date
            var result = priceRepository.findCheapestPriceWithDiscounts(alert.getId().getProductId(),
                    LocalDate.parse("2025-05-08"));
            if (result.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var newPrice = (BigDecimal) result.get(0)[2];
            if (newPrice.compareTo(alert.getCurrentPrice()) >= 0)
                continue;

            var store = storeRepository.findById((Long) result.get(0)[3]);
            if (store.isEmpty()) {
                continue;
            }

            response.add(new ProductStorePrice("P" + alert.getId().getProductId(), store.get().getStoreName(),
                    (BigDecimal) result.get(0)[2]));
            priceAlertRepository.delete(alert);
        }

        return ResponseEntity.ok(response);
    }
}
