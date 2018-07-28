package com.mt3.portfoliomanager.portfoliooptimiser.movingaverage;

import com.mt3.portfoliomanager.portfoliooptimiser.Constants;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.function.Function;

public final class MovingAverageCalculator {

    private final Function<Fund, Double> returnCalculator;

    public MovingAverageCalculator(Function<Fund, Double> returnCalculator) {
        this.returnCalculator = returnCalculator;
    }

    public double[] calculate(Fund fund, int frequency) {
        TDoubleList result = new TDoubleArrayList();

        int n = fund.getPrices().size() - frequency + 1; // only allow equal splits
        for (int i = 0; i < n; i += frequency) {
            Fund subset = fund.view(i + frequency, i);
            result.insert(0, returnCalculator.apply(subset));
        }

        return result.toArray();
    }

    public Fund calculateAsFund(Fund fund, int frequency) {
        double[] averages = calculate(fund, Constants.BUSINESS_DAYS_IN_MONTH);
        return new Fund(fund.getName(), new TDoubleArrayList(averages), false);
    }
}
