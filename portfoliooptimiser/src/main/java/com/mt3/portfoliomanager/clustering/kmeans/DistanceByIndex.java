package com.mt3.portfoliomanager.clustering.kmeans;

import com.mt3.portfoliomanager.CorrelationMatrix;
import com.mt3.portfoliomanager.fund.Fund;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

import java.util.Collections;
import java.util.List;

public final class DistanceByIndex implements DistanceMeasure {

    private final List<Fund> funds;
    private final CorrelationMatrix correlationMatrix;

    public DistanceByIndex(List<Fund> funds, CorrelationMatrix correlationMatrix) {
        this.funds = Collections.unmodifiableList(funds);
        this.correlationMatrix = correlationMatrix;
    }

    @Override
    public double compute(double[] points1, double[] points2) throws DimensionMismatchException {
        Fund fund1 = funds.get((int)points1[0]);
        Fund fund2 = funds.get((int)points2[0]);
        return 1.0 - correlationMatrix.getCorrelation(fund1, fund2);
    }


}
