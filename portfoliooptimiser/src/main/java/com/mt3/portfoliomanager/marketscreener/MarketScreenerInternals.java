package com.mt3.portfoliomanager.marketscreener;

public final class MarketScreenerInternals {

    private final String internalId;
    private final String codeZb;
    private final String equityName;
    private final String marketInternalId;

    public MarketScreenerInternals(String internalId, String codeZb, String equityName, String marketInternalId) {
        this.internalId = internalId;
        this.codeZb = codeZb;
        this.equityName = equityName;
        this.marketInternalId = marketInternalId;
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

    public String getMarketInternalId() {
        return marketInternalId;
    }
}
