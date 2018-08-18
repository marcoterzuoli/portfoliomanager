package com.mt3.portfoliomanager.main;

import com.google.common.base.Joiner;
import com.mt3.portfoliomanager.Constants;
import com.mt3.portfoliomanager.CorrelationMatrix;
import com.mt3.portfoliomanager.fund.Fund;
import com.mt3.portfoliomanager.fund.FundFileReader;
import com.mt3.portfoliomanager.movingaverage.MovingAverageCalculator;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public final class MainMonthlyMovingAverage {

    private static final Logger LOG = Logger.getLogger(MainMonthlyMovingAverage.class);

    public static void main(String[] args) {
        MovingAverageCalculator movingAverageCalculator = new MovingAverageCalculator(Fund::getAnnualisedReturn);

        LOG.info("Loading fund files");
        List<Fund> fundMonthlyReturns = Arrays.stream(Objects.requireNonNull(Constants.MARKET_DATA_FOLDER.toFile().listFiles()))
                .map(x -> FundFileReader.readFromCsv(x.toPath()))
                .filter(x -> x.getPrices().size() >= 36 * Constants.BUSINESS_DAYS_IN_MONTH)
                .map(x -> movingAverageCalculator.calculateAsFund(x, Constants.BUSINESS_DAYS_IN_MONTH))
                .collect(Collectors.toList());

        LOG.info("Calculating correlation matrix");
        CorrelationMatrix correlationMatrix = new CorrelationMatrix(fundMonthlyReturns);

        List<List<Fund>> bestByMonth = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int finalI = i;
            List<Fund> bestForMonthI = fundMonthlyReturns.stream()
                    .sorted(Comparator.comparingDouble(x -> -x.getPrices().get(x.getPrices().size() - finalI- 1)))
                    .collect(Collectors.toList());
            bestByMonth.add(bestForMonthI);
        }

        LOG.info("Creating best portfolio");
        List<Fund> portfolio = new ArrayList<>();
        for (int i = 0; ; i = (i + 1) % bestByMonth.size()) {
            boolean fundAdded = false;
            for (Fund fund : bestByMonth.get(i)) {
                boolean add = true;
                for (Fund fundInPortfolio : portfolio) {
                    if (correlationMatrix.getCorrelation(fund, fundInPortfolio) > 0.4) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    fundAdded = true;
                    portfolio.add(fund);
                    break;
                }
            }

            if (!fundAdded)
                break;
        }

        // TODO: only use funds with returns above certain threshold to exclude cash etc

        String portfolioDescription = Joiner.on('\n').join(portfolio.stream().map(Fund::getName).collect(Collectors.toList()));
        LOG.info("Portfolio:\n" + portfolioDescription);
    }
}
