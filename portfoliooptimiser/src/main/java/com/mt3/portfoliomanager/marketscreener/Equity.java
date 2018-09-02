package com.mt3.portfoliomanager.marketscreener;

import gnu.trove.impl.unmodifiable.TUnmodifiableObjectDoubleMap;
import gnu.trove.map.TObjectDoubleMap;

import java.time.LocalDate;
import java.util.Set;

public final class Equity {

    private static final int DATE_MARGIN = 10;

    private final MarketScreenerInternals marketScreenerInternals;
    private final TObjectDoubleMap<LocalDate> actualPrices;
    private final TObjectDoubleMap<LocalDate> estimatedPrices;

    public Equity(MarketScreenerInternals marketScreenerInternals, TObjectDoubleMap<LocalDate> actualPrices, TObjectDoubleMap<LocalDate> estimatedPrices) {
        this.marketScreenerInternals = marketScreenerInternals;
        this.actualPrices = new TUnmodifiableObjectDoubleMap<>(actualPrices);
        this.estimatedPrices = new TUnmodifiableObjectDoubleMap<>(estimatedPrices);
    }

    public MarketScreenerInternals getMarketScreenerInternals() {
        return marketScreenerInternals;
    }

    public TObjectDoubleMap<LocalDate> getActualPrices() {
        return actualPrices;
    }

    public TObjectDoubleMap<LocalDate> getEstimatedPrices() {
        return estimatedPrices;
    }

    public DateAndPriceJump calculateJump(LocalDate tentativeDateStart, int daysDiff, TObjectDoubleMap<LocalDate> dateToPriceMap) {
        LocalDate startDate = findNearestDate(tentativeDateStart, dateToPriceMap.keySet());
        if (startDate == null)
            return null;

        LocalDate endDate = findNearestDate(startDate.plusDays(daysDiff), dateToPriceMap.keySet());
        if (endDate == null)
            return null;

        double priceJump = dateToPriceMap.get(endDate) / dateToPriceMap.get(startDate);
        return new DateAndPriceJump(this, startDate, endDate, priceJump);
    }

    private LocalDate findNearestDate(LocalDate date, Set<LocalDate> searchDates) {
        for (int i = 0; i <= DATE_MARGIN; i++) {
            LocalDate targetDatePlus = date.plusDays(i);
            if (searchDates.contains(targetDatePlus))
                return targetDatePlus;
            LocalDate targetDateMinus = date.minusDays(i);
            if (searchDates.contains(targetDateMinus))
                return targetDateMinus;
        }
        return null;
    }
}
