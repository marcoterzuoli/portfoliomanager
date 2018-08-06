package com.mt3.portfoliomanager.portfoliooptimiser.clustering.correlation;

import com.mt3.portfoliomanager.portfoliooptimiser.CorrelationMatrix;
import com.mt3.portfoliomanager.portfoliooptimiser.clustering.kmeans.ClusterableFund;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import org.apache.commons.math3.ml.clustering.CentroidCluster;

import java.util.ArrayList;
import java.util.List;

public final class CorrelationClusterer {

    public List<CentroidCluster<ClusterableFund>> cluster(List<Fund> funds, CorrelationMatrix correlationMatrix) {
        List<CentroidCluster<ClusterableFund>> result = new ArrayList<>();
        for (Fund fund : funds) {
            List<CentroidCluster<ClusterableFund>> assignableClusters = new ArrayList<>();
            for (CentroidCluster<ClusterableFund> cluster : result) {
                boolean assignableToCluster = true;
                for (ClusterableFund clusterFund : cluster.getPoints()) {
                    if (correlationMatrix.getCorrelation(fund, clusterFund.getFund()) < 0.9) {
                        assignableToCluster = false;
                        break;
                    }
                }
                if (assignableToCluster)
                    assignableClusters.add(cluster);
            }

            ClusterableFund clusterableFund = new ClusterableFund(fund, 0); // index is irrelevant here
            if (assignableClusters.size() == 0) {
                CentroidCluster<ClusterableFund> newCluster = new CentroidCluster<>(null);
                newCluster.addPoint(clusterableFund);
                result.add(newCluster);
            } else if (assignableClusters.size() == 1) {
                assignableClusters.get(0).addPoint(clusterableFund);
            } else {
                // TODO: how to break the tie?
                System.out.println();
            }
        }

        return result;
    }
}
