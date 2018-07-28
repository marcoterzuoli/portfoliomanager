package com.mt3.portfoliomanager.portfoliooptimiser.fund;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.linked.TDoubleLinkedList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class FundFileReader {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static Fund readFromCsv(Path file) {
        try {
            String name = file.toFile().getName();
            List<String> lines = Files.readAllLines(file);

            TDoubleList prices = new TDoubleLinkedList();
            for (String line : lines) {
                String[] parts = line.split(",", 2);
                LocalDate date = LocalDate.parse(parts[0], FORMATTER);
                if (!IsWeekend(date)) {
                    double price = Double.parseDouble(parts[1]);
                    prices.add(price);
                }
            }

            return new Fund(name, prices);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file " + file.toFile().getAbsolutePath(), e);
        }
    }

    private static boolean IsWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
