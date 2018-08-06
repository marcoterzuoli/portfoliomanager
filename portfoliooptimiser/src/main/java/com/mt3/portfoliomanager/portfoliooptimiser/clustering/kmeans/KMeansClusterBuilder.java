package com.mt3.portfoliomanager.portfoliooptimiser.clustering.kmeans;

import com.mt3.portfoliomanager.portfoliooptimiser.CorrelationMatrix;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

import java.util.List;

public final class KMeansClusterBuilder {

    public List<CentroidCluster<ClusterableFund>> cluster(List<Fund> funds, CorrelationMatrix correlationMatrix) {
        DistanceMeasure distanceByIndex = new DistanceByIndex(funds, correlationMatrix);
        KMeansPlusPlusClusterer<ClusterableFund> clusterer = new KMeansPlusPlusClusterer<>(100, 10000, distanceByIndex);
        return clusterer.cluster(ClusterableFund.createList(funds));
    }
}
