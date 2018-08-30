package com.mt3.portfoliomanager.marketscreener;

public final class MarketScreenerInternals {

    private final String internalId;
    private final String codeZb;
    private final String equityName;

    public MarketScreenerInternals(String internalId, String codeZb, String equityName) {
        this.internalId = internalId;
        this.codeZb = codeZb;
        this.equityName = equityName;
    }

    public String getInternalId() {
        return internalId;
    }

    public String getCodeZb() {
        return codeZb;
    }

    public String getEquityName() {
        return equityName;
    }
}
