package com.mt3.portfoliomanager.fund;

import java.util.Objects;

public final class FundDefinition {

    private static final String ISIN_NAME_SEPARATOR = " - ";

    private final String morningStarId;
    private final String name;
    private final String isin;

    public FundDefinition(String morningStarId, String name, String isin) {
        this.morningStarId = morningStarId;
        this.name = name;
        this.isin = isin;
    }

    public String getMorningStarId() {
        return morningStarId;
    }

    public String getName() {
        return name;
    }

    public String getIsin() {
        return isin;
    }

    public static String getIsin(String name) {
        return name.split(ISIN_NAME_SEPARATOR)[0];
    }

    public String getIsinAndName() {
        return isin + ISIN_NAME_SEPARATOR + getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FundDefinition definition = (FundDefinition) o;
        return Objects.equals(getIsin(), definition.getIsin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIsin());
    }

    @Override
    public String toString() {
        return "FundDefinition{" +
                "morningStarId='" + morningStarId + '\'' +
                ", name='" + name + '\'' +
                ", isin='" + isin + '\'' +
                '}';
    }
}
