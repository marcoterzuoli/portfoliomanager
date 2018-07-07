package com.mt3.portfoliomanager.portfoliooptimiser;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;

import java.util.List;

public final class PortfolioBuilder {

    public TObjectDoubleMap<Fund> createPortfolio(List<Fund> availableFunds, CorrelationMatrix correlationMatrix) {
        int n = availableFunds.size();
        int iterations = 10_000;

        PortfolioStatsCalculator calculator = new PortfolioStatsCalculator();

        NonLinearConjugateGradientOptimizer.Formula formula = NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE;
        // TODO: what is the relationship between iterations here and max iterations later?
        SimpleValueChecker checker = new SimpleValueChecker(1e-6, 1e-6, iterations / 250);
        NonLinearConjugateGradientOptimizer optimizer = new NonLinearConjugateGradientOptimizer(formula, checker);

        MultivariateFunction function = weights -> {
            normaliseWeights(weights);

            TObjectDoubleMap<Fund> weightedFunds = createWeightsMap(availableFunds, weights);
            PortfolioStats stats = calculator.calculateStats(weightedFunds, correlationMatrix);
            return stats.calculateScore();
        };

        MultivariateVectorFunction gradient = weights -> {
            normaliseWeights(weights);
            double[] result = new double[weights.length];
            for (int i = 0; i < n; i++) {
                Fund fund = availableFunds.get(i);
                TObjectDoubleMap<Fund> weightedFunds = createWeightsMap(availableFunds, weights);
                PortfolioStats stats = calculator.calculateStatsDerivative(weightedFunds, correlationMatrix, fund);
                result[i] = stats.calculateScore();
            }
            return result;
        };

        double[] initialGuess = new double[n];
        double eachValue = 1.0 / (double)n;
        for (int i = 0; i < n; i++) {
            initialGuess[i] = eachValue;
        }

        PointValuePair optimum = optimizer.optimize(new MaxEval(iterations),
                new ObjectiveFunction(function),
                new ObjectiveFunctionGradient(gradient),
                GoalType.MAXIMIZE,
                new InitialGuess(initialGuess));
        return createWeightsMap(availableFunds, optimum.getPoint());
    }

    private void normaliseWeights(double[] weights) {
        double sum = 0.0;
        for (double weight : weights)
            sum += weight;
        for (int i = 0; i < weights.length; i++)
            weights[i] = weights[i] / sum;
    }

    private TObjectDoubleMap<Fund> createWeightsMap(List<Fund> availableFunds, double[] weights) {
        TObjectDoubleMap<Fund> weightedFunds = new TObjectDoubleHashMap<>();
        for (int i = 0; i < availableFunds.size(); i++) {
            weightedFunds.put(availableFunds.get(i), weights[i]);
        }
        return weightedFunds;
    }
}
