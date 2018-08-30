package com.mt3.portfoliomanager.marketscreener;

import gnu.trove.impl.unmodifiable.TUnmodifiableObjectDoubleMap;
import gnu.trove.map.TObjectDoubleMap;

import java.time.LocalDate;

public final class Equity {

    private final String name;
    private final TObjectDoubleMap<LocalDate> actualPrices;
    private final TObjectDoubleMap<LocalDate> estimatedPrices;

    public Equity(String name, TObjectDoubleMap<LocalDate> actualPrices, TObjectDoubleMap<LocalDate> estimatedPrices) {
        this.name = name;
        this.actualPrices = new TUnmodifiableObjectDoubleMap<>(actualPrices);
        this.estimatedPrices = new TUnmodifiableObjectDoubleMap(estimatedPrices);
    }

    public String getName() {
        return name;
    }

    public TObjectDoubleMap<LocalDate> getActualPrices() {
        return actualPrices;
    }

    public TObjectDoubleMap<LocalDate> getEstimatedPrices() {
        return estimatedPrices;
    }
}
