package com.mt3.portfoliomanager.fund;

import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

public final class PortfolioChain {

    private final List<Portfolio> portfolios;

    public PortfolioChain(List<Portfolio> portfolios) {
        this.portfolios = ImmutableList.copyOf(portfolios);
    }

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    public double getTotalReturn(LocalDate endDate) {
        return getReturn(Fund::getTotalReturn, endDate);
    }

    public double getAnnualisedReturn(LocalDate endDate) {
        return getReturn(Fund::getAnnualisedReturn, endDate);
    }

    public double getReturn(Function<Fund, Double> returnFunc, LocalDate endDate) {
        return portfolios.stream()
                .mapToDouble(x -> x.getReturn(returnFunc, endDate))
                .average()
                .orElseThrow(() -> new IllegalArgumentException("Avegate cannot be calculated"));
    }
}
