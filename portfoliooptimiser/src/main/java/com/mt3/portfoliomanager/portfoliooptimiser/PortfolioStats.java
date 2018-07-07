package com.mt3.portfoliomanager.portfoliooptimiser;

public final class PortfolioStats {

    private final double expectedReturn;
    private final double expectedVariance;

    public PortfolioStats(double expectedReturn, double expectedVariance) {
        this.expectedReturn = expectedReturn;
        this.expectedVariance = expectedVariance;
    }

    public double calculateScore() {
        return getExpectedReturn() - getExpectedVariance();
    }

    public double getExpectedReturn() {
        return expectedReturn;
    }

    public double getExpectedVariance() {
        return expectedVariance;
    }
}
