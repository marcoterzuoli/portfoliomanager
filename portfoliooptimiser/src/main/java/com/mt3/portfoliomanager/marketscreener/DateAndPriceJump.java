package com.mt3.portfoliomanager.marketscreener;

import java.time.LocalDate;

public final class DateAndPriceJump {

    private final Equity equity;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final double priceJump;

    public DateAndPriceJump(Equity equity, LocalDate startDate, LocalDate endDate, double priceJump) {
        this.equity = equity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priceJump = priceJump;
    }

    public Equity getEquity() {
        return equity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public double getPriceJump() {
        return priceJump;
    }
}
