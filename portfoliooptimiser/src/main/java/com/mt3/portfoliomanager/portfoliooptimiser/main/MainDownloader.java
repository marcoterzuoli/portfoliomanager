package com.mt3.portfoliomanager.portfoliooptimiser.main;

import com.mt3.portfoliomanager.portfoliooptimiser.downloader.MarketDataDownloader;
import com.mt3.portfoliomanager.portfoliooptimiser.downloader.MorningstarDownloader;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.FundFileReader;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

public final class MainDownloader {

    private static final Logger LOG = Logger.getLogger(MainClusterer.class);

    public static void main(String[] args) throws FileNotFoundException {
        BasicConfigurator.configure();

        LocalDate startDate = LocalDate.of(2018, 8, 11);
        LocalDate endDate = LocalDate.now().minusDays(1);

        MarketDataDownloader downloader = new MorningstarDownloader();
        List<Fund> funds = FundFileReader.readPortfolioFromFile(Paths.get(args[0]), downloader);
        double totalReturn = funds.stream()
                .map(x -> x.view(startDate, endDate))
                .mapToDouble(Fund::getTotalReturn)
                .average().getAsDouble();
        List<Fund> benchmarkFunds = FundFileReader.readPortfolioFromFile(Paths.get(args[1]), downloader);
        double benchmarkTotalReturn = benchmarkFunds.stream()
                .map(x -> x.view(startDate, endDate))
                .mapToDouble(Fund::getTotalReturn)
                .average().getAsDouble();

        System.out.println(totalReturn);
        System.out.println(benchmarkTotalReturn);
        // TODO: automatically get dates to download and calculate results for
        // TODO: use weights in doing the calculation
    }
}
