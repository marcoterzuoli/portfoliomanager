package com.mt3.portfoliomanager.main;

import com.mt3.portfoliomanager.downloader.MorningstarDownloader;
import com.mt3.portfoliomanager.fund.Fund;
import com.mt3.portfoliomanager.fund.FundFileReader;
import com.mt3.portfoliomanager.fund.PortfolioChain;
import com.mt3.portfoliomanager.utils.NumberUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class MainPortfolioDownloader {

    private static final Logger LOG = Logger.getLogger(MainClusterer.class);

    public static void main(String[] args) throws FileNotFoundException {
        LocalDate globalStartDate = LocalDate.of(2018, 8, 11);
        LocalDate monthStartDate = LocalDate.now().withDayOfMonth(1).minusDays(1);
        LocalDate[] startDates = new LocalDate[] { globalStartDate, monthStartDate };
        LocalDate endDate = LocalDate.now().minusDays(1);

        MorningstarDownloader downloader = new MorningstarDownloader();
        PortfolioChain portfolioChain = FundFileReader.readPortfolioAllocationFromFile(Paths.get(args[0]), downloader);
        List<Fund> benchmarkFunds = FundFileReader.readPortfolioFromFile(Paths.get(args[1]), downloader);

        try (PrintWriter writer = new PrintWriter(new File(args[2]))) {
            for (LocalDate startDate : startDates) {
                double benchmarkTotalReturn = benchmarkFunds.stream()
                        .map(x -> x.view(startDate, endDate))
                        .mapToDouble(Fund::getTotalReturn)
                        .average().getAsDouble();

                log(writer, "From close of " + startDate + " to close of " + endDate);
                log(writer, "My Fund," + NumberUtils.getAsPercenage(portfolioChain.getTotalReturn(startDate, endDate)));
                log(writer, "Benchmark," + NumberUtils.getAsPercenage(benchmarkTotalReturn));

                List<Fund> investedFunds = portfolioChain.getPortfolios().stream()
                        .flatMap(x -> x.getAllocation().keySet().stream())
                        .map(x -> x.view(startDate, endDate))
                        .sorted(Comparator.comparingDouble(x -> -x.getTotalReturn()))
                        .collect(Collectors.toList());
                for (Fund fund : investedFunds) {
                    log(writer, fund.getDefinition().getIsinAndName() + ": " + NumberUtils.getAsPercenage(fund.getTotalReturn()));
                }
                log(writer, "");
            }
        }
    }

    private static void log(PrintWriter writer, String message) {
        LOG.info(message);
        writer.println(message);
    }
}
