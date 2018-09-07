package com.mt3.portfoliomanager.fund;

import com.google.common.util.concurrent.AtomicDouble;
import gnu.trove.impl.unmodifiable.TUnmodifiableObjectDoubleMap;
import gnu.trove.map.TObjectDoubleMap;

import java.time.LocalDate;
import java.util.function.Function;

public final class Portfolio {

    private final LocalDate investmentDate;
    private final TObjectDoubleMap<Fund> allocation;

    public Portfolio(LocalDate investmentDate, TObjectDoubleMap<Fund> allocation) {
        this.investmentDate = investmentDate;
        this.allocation = new TUnmodifiableObjectDoubleMap<Fund>(allocation);
    }

    public LocalDate getInvestmentDate() {
        return investmentDate;
    }

    public TObjectDoubleMap<Fund> getAllocation() {
        return allocation;
    }

    public double getTotalReturn(LocalDate startDate, LocalDate endDate) {
        return getReturn(Fund::getTotalReturn, startDate, endDate);
    }

    public double getAnnualisedReturn(LocalDate startDate, LocalDate endDate) {
        return getReturn(Fund::getAnnualisedReturn, startDate, endDate);
    }

    public double getReturn(Function<Fund, Double> returnFunc, LocalDate startDate, LocalDate endDate) {
        LocalDate finalStartDate = startDate == null ? investmentDate : startDate;

        AtomicDouble totalReturn = new AtomicDouble();
        getAllocation().forEachEntry((fund, weight) -> {
            double calculatedReturn = returnFunc.apply(fund.view(finalStartDate, endDate));
            totalReturn.addAndGet(calculatedReturn * weight);
            return true;
        });
        return totalReturn.get();
    }
}
