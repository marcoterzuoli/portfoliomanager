package com.mt3.portfoliomanager.portfoliooptimiser.fund;

import com.mt3.portfoliomanager.portfoliooptimiser.Constants;
import gnu.trove.impl.unmodifiable.TUnmodifiableDoubleList;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public final class Fund {
    private final String name;
    private final TDoubleList prices;
    private final SummaryStatistics statistics = new SummaryStatistics(); // functionally immutable

    public Fund(String name, TDoubleList prices) {
        this(name, prices, true);
    }

    public Fund(String name, TDoubleList prices, boolean normalise) {
        this.name = name;

        double price0 = prices.get(0);
        TDoubleList pricesTemp = new TDoubleArrayList();
        for (double price : prices.toArray()) {
            if (normalise)
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

    public double getTotalReturn() {
        return prices.get(prices.size() - 1);
    }

    public double getAnnualisedReturn() {
        double exponent = 1.0 / ((double)prices.size() / Constants.BUSINESS_DAYS_IN_YEAR);
        return Math.pow(getTotalReturn(), exponent);
    }

    public Fund view(int fromIndexAgo, int toIndexAgo) {
        int n = prices.size();
        int fromIndex = n - fromIndexAgo;
        int toIndex = n - toIndexAgo;
        return new Fund(getName(), prices.subList(fromIndex, toIndex));
    }

    public Fund[] split(int index) {
        Fund beforeFund = new Fund(getName(), prices.subList(0, index));
        Fund afterFund = new Fund(getName(), prices.subList(index, prices.size()));
        return new Fund[] {beforeFund, afterFund};
    }
}
