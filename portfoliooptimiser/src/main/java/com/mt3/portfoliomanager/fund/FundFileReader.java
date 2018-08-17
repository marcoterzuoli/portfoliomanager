package com.mt3.portfoliomanager.fund;

import com.mt3.portfoliomanager.NumberUtils;
import com.mt3.portfoliomanager.downloader.MarketDataDownloader;
import com.opencsv.CSVReader;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.linked.TDoubleLinkedList;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FundFileReader {

    private static final Logger LOG = Logger.getLogger(FundFileReader.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("ddMMMyyyy");

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

    public static List<Fund> readPortfolioFromFile(Path file, MarketDataDownloader downloader) {
        try {
            List<String> fundNames = Files.readAllLines(file);
            List<String> isins = fundNames.stream()
                    .map(Fund::getIsin)
                    .collect(Collectors.toList());
            return downloader.download(isins);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file " + file.toFile().getAbsolutePath(), e);
        }
    }

    public static PortfolioChain readPortfolioAllocationFromFile(Path file, MarketDataDownloader downloader) {
        List<Portfolio> result = new ArrayList<>();

        TObjectDoubleMap<Fund> portfolio = new TObjectDoubleHashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(file.toFile()))) {
            List<String[]> lines = reader.readAll();
            String[] header = lines.remove(0);

            List<Fund> downloadedFunds = new ArrayList<>();
            for (int headerIndex = 1; headerIndex < header.length; headerIndex++) {
                LocalDate investmentDate = LocalDate.parse(header[headerIndex], DATE_TIME_FORMATTER);

                TObjectDoubleMap<String> isins = new TObjectDoubleHashMap<>();
                for (String[] line : lines) {
                    String isin = Fund.getIsin(line[0]);
                    double allocation = NumberUtils.parsePercentage(line[headerIndex]);
                    isins.put(isin, allocation);
                }

                List<Fund> funds;
                if (downloadedFunds.isEmpty()) {
                    funds = downloader.download(isins.keySet());
                    downloadedFunds.addAll(funds);
                } else {
                    funds = downloadedFunds;
                }

                for (Fund fund : funds) {
                    double allocation = isins.get(fund.getIsin());
                    if (allocation > 0.0)
                        portfolio.put(fund, allocation);
                }
                result.add(new Portfolio(investmentDate, portfolio));
            }

            return new PortfolioChain(result);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file " + file.toFile().getAbsolutePath(), e);
        }
    }
}
