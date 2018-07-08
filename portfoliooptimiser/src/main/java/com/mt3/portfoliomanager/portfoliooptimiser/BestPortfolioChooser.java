package com.mt3.portfoliomanager.portfoliooptimiser;

import gnu.trove.map.TObjectDoubleMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class BestPortfolioChooser {

    public TObjectDoubleMap<Fund> createPortfolio(List<Fund> funds, int maxFundsInPortfolio) {
        CorrelationMatrix correlationMatrix = new CorrelationMatrix(funds);
        AtomicReference<PortfolioStats> maxStats = new AtomicReference<>();
        AtomicReference<TObjectDoubleMap<Fund>> bestPortfolio = new AtomicReference<>();
        CombinationsSelector<Fund> combinationsSelector = new CombinationsSelector<>();
        PortfolioBuilder builder = new PortfolioBuilder();
        PortfolioStatsCalculator calculator = new PortfolioStatsCalculator();

        // process high-return funds first since they have a higher chance of being used in the best portfolio
        List<Map.Entry<Fund, Double>> singleFundReturns = new ArrayList<>();
        for (Fund fund : funds) {
            TObjectDoubleMap<Fund> portfolio = builder.createPortfolio(funds, correlationMatrix);
            PortfolioStats portfolioStats = calculator.calculateStats(portfolio, correlationMatrix);
            singleFundReturns.add(new AbstractMap.SimpleEntry<>(fund, portfolioStats.getExpectedReturn()));
        }
        singleFundReturns.sort((o1, o2) -> (int)(1_000.0 * (o2.getValue() - o1.getValue())));
        List<Fund> sortedFunds = new ArrayList<>(singleFundReturns.stream().map(Map.Entry::getKey).collect(Collectors.toList()));

/*        long totalCombinations = 1;
        for (int maxFundsInPortfolioForThisIteration = 2; maxFundsInPortfolioForThisIteration <= maxFundsInPortfolio; maxFundsInPortfolioForThisIteration++) {
            for (int i = 0; i < maxFundsInPortfolio; i++)
                totalCombinations
        }*/

        // TODO: how to multithread?
        for (int maxFundsInPortfolioForThisIteration = 2; maxFundsInPortfolioForThisIteration <= maxFundsInPortfolio; maxFundsInPortfolioForThisIteration++) {
            combinationsSelector.select(sortedFunds, maxFundsInPortfolioForThisIteration, fundCombination -> {
                // discard combination if 2 funds are too highly correlated, since it's better to have a simpler portfolio
                // with only one of them in it
                for (Fund fund1 : fundCombination)
                    for (Fund fund2 : fundCombination)
                        if (fund1 != fund2 && correlationMatrix.getCorrelation(fund1, fund2) > 0.95)
                            return true;

                TObjectDoubleMap<Fund> portfolio = builder.createPortfolio(fundCombination, correlationMatrix);
                PortfolioStats portfolioStats = calculator.calculateStats(portfolio, correlationMatrix);
                if (maxStats.get() == null || maxStats.get().calculateScore() < portfolioStats.calculateScore()) {
                    maxStats.set(portfolioStats);
                    bestPortfolio.set(portfolio);

                    // TODO: print portfolio
                }
                return true;
            });
        }

        return bestPortfolio.get();
    }
}
