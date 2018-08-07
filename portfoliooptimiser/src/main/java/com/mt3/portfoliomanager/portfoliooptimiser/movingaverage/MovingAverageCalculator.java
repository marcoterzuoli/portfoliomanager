package com.mt3.portfoliomanager.portfoliooptimiser.movingaverage;

import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.function.Function;

public final class MovingAverageCalculator {

    private final Function<Fund, Double> returnCalculator;

    public MovingAverageCalculator(Function<Fund, Double> returnCalculator) {
        this.returnCalculator = returnCalculator;
    }

    public double[] calculate(Fund fund, double frequency) {
        TDoubleList result = new TDoubleArrayList();

        int n = fund.getPrices().size() - (int)frequency + 1; // only allow equal splits
        for (float i = 0; i < n; i += frequency) {
            Fund subset = fund.view((int)(i + frequency), (int)i);
            result.insert(0, returnCalculator.apply(subset));
        }

        return result.toArray();
    }

    public Fund calculateAsFund(Fund fund, double frequency) {
        double[] averages = calculate(fund, frequency);
        return new Fund(fund.getName(), new TDoubleArrayList(averages), false);
    }
}
