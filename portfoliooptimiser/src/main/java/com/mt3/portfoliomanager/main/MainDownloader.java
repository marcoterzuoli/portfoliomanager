package com.mt3.portfoliomanager.main;

import com.mt3.portfoliomanager.downloader.MarketDataDownloader;
import com.mt3.portfoliomanager.downloader.MorningstarDownloader;
import com.mt3.portfoliomanager.fund.Fund;
import com.mt3.portfoliomanager.fund.FundFileReader;
import com.mt3.portfoliomanager.fund.PortfolioChain;
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
        PortfolioChain portfolioChain = FundFileReader.readPortfolioAllocationFromFile(Paths.get(args[0]), downloader);

        List<Fund> benchmarkFunds = FundFileReader.readPortfolioFromFile(Paths.get(args[1]), downloader);
        double benchmarkTotalReturn = benchmarkFunds.stream()
                .map(x -> x.view(startDate, endDate))
                .mapToDouble(Fund::getTotalReturn)
                .average().getAsDouble();

        System.out.println(100.0 * (portfolioChain.getTotalReturn(endDate) - 1.0));
        System.out.println(100.0 * (benchmarkTotalReturn - 1.0));
        // TODO: automatically get dates to download and calculate results for
        // TODO: calculate current allocation (i.e. considering how each fund has returned)
    }
}
