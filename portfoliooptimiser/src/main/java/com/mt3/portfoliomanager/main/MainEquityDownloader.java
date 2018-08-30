package com.mt3.portfoliomanager.main;

import com.mt3.portfoliomanager.downloader.MarketScreenerEquityDownloader;
import com.mt3.portfoliomanager.downloader.MarketScreenerMarketDownloader;
import com.mt3.portfoliomanager.marketscreener.Equity;
import com.mt3.portfoliomanager.marketscreener.EquityCorrelator;
import com.mt3.portfoliomanager.marketscreener.MarketScreenerInternals;

import java.util.List;

public final class MainEquityDownloader {

    public static void main(String[] args) {
        MarketScreenerMarketDownloader marketDownloader = new MarketScreenerMarketDownloader();
        List<MarketScreenerInternals> internals = marketDownloader.download();

        MarketScreenerEquityDownloader equityDownloader = new MarketScreenerEquityDownloader();
        List<Equity> equities = equityDownloader.download(internals);

        EquityCorrelator correlator = new EquityCorrelator();
        double[] correlations = equities.stream()
                .mapToDouble(x -> correlator.calculateCorrelation(x, 30, 10))
                .filter(x -> !Double.isNaN(x))
                .toArray();
        double correlation = equities.stream()
                .mapToDouble(x -> correlator.calculateCorrelation(x, 30, 10))
                .filter(x -> !Double.isNaN(x))
                .average()
                .orElse(0.0);
        System.out.println(correlation);
    }
}
