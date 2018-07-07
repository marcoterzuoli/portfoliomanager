package com.mt3.portfoliomanager.portfoliooptimiser;

import gnu.trove.impl.unmodifiable.TUnmodifiableDoubleList;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public final class Fund {
    private final String name;
    private final TDoubleList prices;
    private final SummaryStatistics statistics = new SummaryStatistics(); // functionally immutable

    public Fund(String name, TDoubleList prices) {
        this.name = name;

        double price0 = prices.get(0);
        TDoubleList pricesTemp = new TDoubleArrayList();
        for (double price : prices.toArray()) {
            price = price / price0;
            statistics.addValue(price);
            pricesTemp.add(price);
        }
        this.prices = new TUnmodifiableDoubleList(pricesTemp);
    }

    public String getName() {
        return name;
    }

    public TDoubleList getPrices() {
        return prices;
    }

    public double getMean() {
        return statistics.getMean();
    }

    public double getVariance() {
        return statistics.getVariance();
    }

    public double getStdev() {
        return statistics.getStandardDeviation();
    }
}
