package com.example.accesachallenge;

import com.example.accesachallenge.model.*;
import com.example.accesachallenge.repository.DiscountRepository;
import com.example.accesachallenge.repository.PriceRepository;
import com.example.accesachallenge.repository.ProductRepository;
import com.example.accesachallenge.repository.StoreRepository;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.accesachallenge.repository")
public class AccesaChallengeApplication implements CommandLineRunner {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final PriceRepository priceRepository;
    private final DiscountRepository discountRepository;

    public AccesaChallengeApplication(StoreRepository storeRepository,
                                      ProductRepository productRepository,
                                      PriceRepository priceRepository,
                                      DiscountRepository discountRepository) {

        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.discountRepository = discountRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(AccesaChallengeApplication.class, args);
    }

    private void addPrice(Store store, LocalDate date, String filePath) {
        try (var reader = new CSVReaderBuilder(new FileReader(filePath))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {

            // Read header
            reader.readNext();

            String[] record;
            while ((record = reader.readNext()) != null) {
                var productId = Long.parseLong(record[0].substring(1));

                String[] finalRecord = record;
                var product = productRepository.findById(productId).orElseGet(() -> {
                    var newProduct = new Product();

                    newProduct.setProductId(productId);
                    newProduct.setName(finalRecord[1]);
                    newProduct.setCategory(finalRecord[2]);
                    newProduct.setBrand(finalRecord[3]);
                    newProduct.setPackageQuantity(Double.parseDouble(finalRecord[4]));
                    newProduct.setPackageUnit(finalRecord[5]);

                    return productRepository.save(newProduct);
                });

                var priceId = new PriceId();
                priceId.setProductId(product.getProductId());
                priceId.setStoreId(store.getStoreId());
                priceId.setDate(date);

                var newPrice = new Price();
                newPrice.setId(priceId);
                newPrice.setStore(store);
                newPrice.setProduct(product);
                newPrice.setPrice(new BigDecimal(record[6]));
                newPrice.setCurrency(record[7]);

                priceRepository.save(newPrice);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void addDiscount(Store store, LocalDate date, String filePath) {
        try (var reader = new CSVReaderBuilder(new FileReader(filePath))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            // Read header
            reader.readNext();

            String[] record;
            while ((record = reader.readNext()) != null) {
                var productId = Long.parseLong(record[0].substring(1));

                String[] finalRecord = record;
                var product = productRepository.findById(productId).orElseGet(() -> {
                    var newProduct = new Product();

                    newProduct.setProductId(productId);
                    newProduct.setName(finalRecord[1]);
                    newProduct.setBrand(finalRecord[2]);
                    newProduct.setPackageQuantity(Double.parseDouble(finalRecord[3]));
                    newProduct.setPackageUnit(finalRecord[4]);
                    newProduct.setCategory(finalRecord[5]);

                    return productRepository.save(newProduct);
                });

                var discountId = new DiscountId();
                discountId.setProductId(product.getProductId());
                discountId.setStoreId(store.getStoreId());
                discountId.setStartDate(LocalDate.parse(record[6]));
                discountId.setEndDate(LocalDate.parse(record[7]));

                var newDiscount = new Discount();
                newDiscount.setId(discountId);
                newDiscount.setStore(store);
                newDiscount.setProduct(product);
                newDiscount.setDiscountPercentage(new BigDecimal(record[8]));
                discountRepository.save(newDiscount);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run(String... args) throws Exception {

        // Clear the database and load the data from the .csv files
        priceRepository.deleteAll();
        discountRepository.deleteAll();
        productRepository.deleteAll();
        storeRepository.deleteAll();

        var directory = new File("data");
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Directory does not exist or is not a directory");
            return;
        }

        var files = directory.listFiles();
        if (files == null) {
            System.out.println("No files found in data directory");
            return;
        }

        for (var file : files) {
            if (!file.isFile())
                continue;

            var strings = file.getName().split("[_.]");
            var storeName = strings[0];

            var store = storeRepository.findByStoreName(storeName).orElseGet(() -> {
                var newStore = new Store();
                newStore.setStoreName(storeName);
                return storeRepository.save(newStore);
            });

            if (file.getName().contains("discount")) {
                var date = LocalDate.parse(strings[2], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                addDiscount(store, date, file.getPath());
            } else {
                var date = LocalDate.parse(strings[1], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                addPrice(store, date, file.getPath());
            }
        }
    }
}
