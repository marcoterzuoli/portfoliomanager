package com.mt3.portfoliomanager.portfoliooptimiser.main;

import com.mt3.portfoliomanager.portfoliooptimiser.Constants;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.FundFileReader;
import com.mt3.portfoliomanager.portfoliooptimiser.movingaverage.MovingAverageCalculator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MainMonthlyMovingAverage {

    public static void main(String[] args) {
        MovingAverageCalculator movingAverageCalculator = new MovingAverageCalculator(Fund::getAnnualisedReturn);
        List<Fund> funds = Arrays.stream(Objects.requireNonNull(Constants.MARKET_DATA_FOLDER.toFile().listFiles()))
                .map(x -> FundFileReader.readFromCsv(x.toPath()))
                .filter(x -> x.getPrices().size() >= 36)
                .map(x -> movingAverageCalculator.calculateAsFund(x, Constants.BUSINESS_DAYS_IN_MONTH))
                .collect(Collectors.toList());

    }
}
