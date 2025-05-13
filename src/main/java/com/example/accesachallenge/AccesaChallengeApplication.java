package com.example.accesachallenge;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

@SpringBootApplication
public class AccesaChallengeApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AccesaChallengeApplication.class, args);
    }

    // TODO: Parse stuff
    @Override
    public void run(String... args) throws Exception {
        try (var reader = new CSVReaderBuilder(new FileReader("data/lidl_2025-05-01.csv"))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {

            String[] header = reader.readNext();

            String[] record;
            while ((record = reader.readNext()) != null) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
