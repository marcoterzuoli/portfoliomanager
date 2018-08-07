package com.mt3.portfoliomanager.portfoliooptimiser.fund;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.linked.TDoubleLinkedList;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class FundFileReader {

    private static final Logger LOG = Logger.getLogger(FundFileReader.class);

    public static Fund readFromCsv(Path file) {
        try {
            LOG.info(file.toFile().getAbsolutePath());
            String name = file.toFile().getName();
            List<String> lines = Files.readAllLines(file);

            TDoubleList prices = new TDoubleLinkedList();
            for (String line : lines) {
                String[] parts = line.split(",", 2);
                double price = Double.parseDouble(parts[1]);
                prices.add(price);
            }

            return new Fund(name, prices);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file " + file.toFile().getAbsolutePath(), e);
        }
    }

    public static List<Fund> readPortfolioFromFile(Path file, List<Fund> funds) {
        try {
            List<String> fundNames = Files.readAllLines(file);
            return funds.stream()
                    .filter(x -> fundNames.contains(x.getName()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file " + file.toFile().getAbsolutePath(), e);
        }
    }
}
