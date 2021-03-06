package com.mt3.portfoliomanager;

import com.mt3.portfoliomanager.fund.Fund;
import gnu.trove.impl.unmodifiable.TUnmodifiableObjectIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.Arrays;
import java.util.List;

public final class CorrelationMatrix {

    private final TObjectIntMap<Fund> fundToIndexMap;
    private final double[][] correlations;  // functionally immutable

    public CorrelationMatrix(List<Fund> funds) {
        TObjectIntMap<Fund> fundToIndexMapTemp = new TObjectIntHashMap<>();
        for (int i = 0; i < funds.size(); i++) {
            fundToIndexMapTemp.put(funds.get(i), i);
        }

        this.fundToIndexMap = new TUnmodifiableObjectIntMap<>(fundToIndexMapTemp);
        this.correlations = buildCorrelations(funds);
    }

    private double[][] buildCorrelations(List<Fund> funds) {
        int n = funds.size();
        double[][] correlations = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double correlation;

                if (i == j) {
                    correlation = 1.0;
                } else {
                    Fund fundI = funds.get(i);
                    Fund fundJ = funds.get(j);

                    double[] pricesI = fundI.getPrices().toArray();
                    double[] pricesJ = fundJ.getPrices().toArray();
                    int minLength = Math.min(pricesI.length, pricesJ.length);
                    pricesI = Arrays.copyOfRange(pricesI, pricesI.length - minLength, pricesI.length);
                    pricesJ = Arrays.copyOfRange(pricesJ, pricesJ.length - minLength, pricesJ.length);

                    correlation = new PearsonsCorrelation().correlation(pricesI, pricesJ);
                    if (Double.isNaN(correlation)) {
                        correlation = 0.0;
                    }
                }

                correlations[i][j] = correlation;
                correlations[j][i] = correlation;
            }
        }

        return correlations;
    }

    public double getCorrelation(Fund fund1, Fund fund2) {
        int index1 = fundToIndexMap.get(fund1);
        int index2 = fundToIndexMap.get(fund2);
        return correlations[index1][index2];
    }
}
