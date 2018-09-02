package com.mt3.portfoliomanager.main;

import com.mt3.portfoliomanager.downloader.MarketScreenerEquityDownloader;
import com.mt3.portfoliomanager.downloader.MarketScreenerMarketDownloader;
import com.mt3.portfoliomanager.marketscreener.DateAndPriceJump;
import com.mt3.portfoliomanager.marketscreener.Equity;
import com.mt3.portfoliomanager.marketscreener.MarketScreenerInternals;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class MainEquityChooser {

    public static void main(String[] args) {
        MarketScreenerMarketDownloader marketDownloader = new MarketScreenerMarketDownloader();
        List<MarketScreenerInternals> internals = marketDownloader.download();

        MarketScreenerEquityDownloader equityDownloader = new MarketScreenerEquityDownloader();
        List<Equity> equities = equityDownloader.download(internals);

        int preDays = 150;
        LocalDate startDate = LocalDate.now().minusDays(preDays);
        List<DateAndPriceJump> sortedEquities = equities.stream()
                .map(x -> x.calculateJump(startDate, preDays, x.getEstimatedPrices()))
                .filter(x -> x != null)
                .sorted(Comparator.comparingDouble(x -> x.getPriceJump()))
                .collect(Collectors.toList());
        for (int i = 0; i < 20; i++) {
            System.out.println(sortedEquities.get(i).getEquity().getMarketScreenerInternals());
        }
    }
}
