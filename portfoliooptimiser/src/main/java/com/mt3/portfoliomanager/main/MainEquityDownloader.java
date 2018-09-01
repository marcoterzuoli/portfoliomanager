package com.mt3.portfoliomanager.main;

import com.mt3.portfoliomanager.downloader.MarketScreenerEquityDownloader;
import com.mt3.portfoliomanager.downloader.MarketScreenerMarketDownloader;
import com.mt3.portfoliomanager.marketscreener.Equity;
import com.mt3.portfoliomanager.marketscreener.EquityCorrelator;
import com.mt3.portfoliomanager.marketscreener.MarketScreenerInternals;

import java.util.Arrays;
import java.util.List;

public final class MainEquityDownloader {

    public static void main(String[] args) {
        MarketScreenerMarketDownloader marketDownloader = new MarketScreenerMarketDownloader();
        List<MarketScreenerInternals> internals = marketDownloader.download();

        MarketScreenerEquityDownloader equityDownloader = new MarketScreenerEquityDownloader();
        List<Equity> equities = equityDownloader.download(internals);

        for (int preDays = 30; preDays <= 150; preDays += 30) {
            for (int postDays = 5; postDays <= 150; postDays += 5) {
                int finalPreDays = preDays;
                int finalPostDays = postDays;
                EquityCorrelator correlator = new EquityCorrelator();
                double[] correlations = equities.stream()
                        .mapToDouble(x -> correlator.calculateCorrelation(x, finalPreDays, finalPostDays))
                        .filter(x -> !Double.isNaN(x))
                        .toArray();
                double correlation = Arrays.stream(correlations)
                        .average()
                        .orElse(0.0);
                System.out.println(finalPreDays + "\t" + finalPostDays + "\t" + correlation);
            }
        }
    }
}
