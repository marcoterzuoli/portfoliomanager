package com.mt3.portfoliomanager.portfoliooptimiser;

import com.google.common.util.concurrent.AtomicDouble;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import gnu.trove.map.TObjectDoubleMap;

public final class PortfolioStatsCalculator {

    public PortfolioStats calculateStats(TObjectDoubleMap<Fund> weightedFunds, CorrelationMatrix correlationMatrix) {
        return calculateStats(weightedFunds, correlationMatrix, null, Double.NaN, Double.NaN);
    }

    public PortfolioStats calculateStatsDerivative(TObjectDoubleMap<Fund> weightedFunds, CorrelationMatrix correlationMatrix,
                                          Fund derivariveFund) {
        return calculateStats(weightedFunds, correlationMatrix, derivariveFund, 1.0, 2.0);

    }

    // weight1OrDerivative and weight1SquareOrDerivative mean
    private PortfolioStats calculateStats(TObjectDoubleMap<Fund> weightedFunds, CorrelationMatrix correlationMatrix,
                                          Fund derivariveFund, double weight1OrDerivative, double weight1SquareOrDerivative) {
        AtomicDouble expectedReturn = new AtomicDouble();
        weightedFunds.forEachEntry((fund, weight) -> {
            // stdev is highly correlated with returns, so we use it to calculate expected returns from a fund
            double actualWeight1 = derivariveFund != fund ? weight : weight1OrDerivative;
            if (actualWeight1 < 0.0) {
                expectedReturn.set(Double.MIN_VALUE);
                return false;
            }
            expectedReturn.addAndGet(actualWeight1 * fund.getStdev());
            return  true;
        });

        AtomicDouble portfolioStdev = new AtomicDouble();
        weightedFunds.forEachEntry((fund1, weight1) -> {
            double actualWeight1 = derivariveFund != fund1 ? weight1 : weight1OrDerivative;
            if (actualWeight1 < 0.0) {
                portfolioStdev.set(Double.MAX_VALUE);
                return false;
            }

            double actualWeight1Square = (derivariveFund != fund1 ? weight1 : weight1SquareOrDerivative) * weight1;
            portfolioStdev.addAndGet(actualWeight1Square * fund1.getVariance());

            weightedFunds.forEachEntry((fund2, weight2) -> {
                if (fund1 != fund2) {
                    // not multiplying by 2 because the following line will be calculated for fund1 and fund2 in both orders
                    double correlation = correlationMatrix.getCorrelation(fund1, fund2);
                    portfolioStdev.addAndGet(correlation * actualWeight1 * weight2 * fund1.getStdev() * fund2.getStdev());
                }
                return true;
            });

            return true;
        });

        return new PortfolioStats(expectedReturn.get(), portfolioStdev.get());
    }
}
