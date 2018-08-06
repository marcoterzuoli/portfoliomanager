package com.mt3.portfoliomanager.portfoliooptimiser.clustering.kmeans;

import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import org.apache.commons.math3.ml.clustering.Clusterable;

import java.util.ArrayList;
import java.util.List;

public final class ClusterableFund implements Clusterable  {

    public static List<ClusterableFund> createList(List<Fund> funds) {
        List<ClusterableFund> result = new ArrayList<>(funds.size());
        for (int i = 0; i < funds.size(); i++)
            result.add(new ClusterableFund(funds.get(i), i));
        return result;
    }

    private final Fund fund;
    private final int index;

    public ClusterableFund(Fund fund, int index) {
        this.fund = fund;
        this.index = index;
    }

    public Fund getFund() {
        return fund;
    }

    @Override
    public double[] getPoint() {
        return new double[] { index };
    }
}
