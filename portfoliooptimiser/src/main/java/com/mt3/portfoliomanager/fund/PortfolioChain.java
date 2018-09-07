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

    public double getTotalReturn(LocalDate startDate, LocalDate endDate) {
        return getReturn(Fund::getTotalReturn, startDate, endDate);
    }

    public double getAnnualisedReturn(LocalDate startDate, LocalDate endDate) {
        return getReturn(Fund::getAnnualisedReturn, startDate, endDate);
    }

    public double getReturn(Function<Fund, Double> returnFunc, LocalDate startDate, LocalDate endDate) {
        return portfolios.stream()
                .mapToDouble(x -> x.getReturn(returnFunc, startDate, endDate))
                .average()
                .orElseThrow(() -> new IllegalArgumentException("Avegate cannot be calculated"));
    }
}
