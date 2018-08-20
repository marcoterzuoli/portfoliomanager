package com.mt3.portfoliomanager.fund;

import com.google.common.collect.ImmutableList;
import com.mt3.portfoliomanager.Constants;
import gnu.trove.impl.unmodifiable.TUnmodifiableDoubleList;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.time.LocalDate;
import java.util.*;

public final class Fund {
    private final String name;
    private final List<LocalDate> dates;
    private final TDoubleList prices;
    private final SummaryStatistics statistics = new SummaryStatistics(); // functionally immutable

    public Fund(String name, TDoubleList prices) {
        this(name, prices, true);
    }

    public Fund(String name, TDoubleList prices, boolean normalise) {
        this(name, ImmutableList.of(), prices, normalise);
    }

    public Fund(String name, List<LocalDate> dates, TDoubleList prices, boolean normalise) {
        if (!dates.isEmpty() && dates.size() != prices.size())
            throw new IllegalArgumentException("Dates and prices must be of the same length, or dates be empty");

        this.name = name;

        double price0 = prices.get(0);
        List<LocalDate> datesTemp = new ArrayList<>();
        TDoubleList pricesTemp = new TDoubleArrayList();

        double[] pricesArray = prices.toArray();
        LocalDate[] datesArray = dates.toArray(new LocalDate[0]);
        for (int i = 0; i < pricesArray.length; i++) {
            double price = pricesArray[i];
            if (normalise)
                price = price / price0;
            statistics.addValue(price);
            pricesTemp.add(price);

            if (!dates.isEmpty()) {
                datesTemp.add(datesArray[i]);
            }
        }
        this.dates = ImmutableList.copyOf(datesTemp);
        this.prices = new TUnmodifiableDoubleList(pricesTemp);
    }

    public String getName() {
        return name;
    }

    public String getIsin() {
        return getIsin(name);
    }

    public static String getIsin(String name) {
        return name.split(" - ")[0];
    }

    public TDoubleList getPrices() {
        return prices;
    }

    public List<LocalDate> getDates() {
        return dates;
    }

    public Map<LocalDate, Double> getPricesAndDates() {
        Map<LocalDate, Double> result = new LinkedHashMap<>();
        double[] prices = getPrices().toArray();
        LocalDate[] dates = getDates().toArray(new LocalDate[0]);
        for (int i = 0; i < prices.length; i++)
            result.put(dates[i], prices[i]);
        return result;
    }

    public double getMean() {
        return statistics.getMean();
    }

    public double getMax() {
        return statistics.getMax();
    }

    public double getVariance() {
        return statistics.getVariance();
    }

    public double getStdev() {
        return statistics.getStandardDeviation();
    }

    public double getTotalReturn() {
        return prices.get(prices.size() - 1) / prices.get(0);
    }

    public double getAnnualisedReturn() {
        double exponent = 1.0 / ((double)prices.size() / Constants.BUSINESS_DAYS_IN_YEAR);
        return Math.pow(getTotalReturn(), exponent);
    }

    public double getProductReturn() {
        double result = 1.0;
        for (double price : prices.toArray())
            result *= price;
        return result;
    }

    public Fund view(LocalDate fromDate, LocalDate toDate) {
        int fromIndex = -1;
        int toIndex = -1;
        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = dates.get(i);
            if (fromDate.equals(date)) {
                fromIndex = i;
            }
            if (toDate.equals(date)) {
                toIndex = i;
            }
            if (fromIndex >= 0 && toIndex >= 0)
                break;
        }
        if (fromIndex < 0)
            throw new IllegalArgumentException("From date " + fromDate + " cannot be found");
        if (toIndex < 0)
            throw new IllegalArgumentException("To date " + toDate + " cannot be found");
        int n = prices.size();
        return view(n - fromIndex, n - toIndex + 1); // +1 because toDate is included, but toIndex is not
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fund fund = (Fund) o;
        return Objects.equals(getName(), fund.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
