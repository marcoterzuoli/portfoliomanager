package com.mt3.portfoliomanager.portfoliooptimiser.main;

import com.google.common.base.Joiner;
import com.mt3.portfoliomanager.portfoliooptimiser.Constants;
import com.mt3.portfoliomanager.portfoliooptimiser.CorrelationMatrix;
import com.mt3.portfoliomanager.portfoliooptimiser.clustering.correlation.CorrelationClusterer;
import com.mt3.portfoliomanager.portfoliooptimiser.clustering.kmeans.ClusterableFund;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.FundFileReader;
import com.mt3.portfoliomanager.portfoliooptimiser.movingaverage.MovingAverageCalculator;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MainClusterer {

    private static final Logger LOG = Logger.getLogger(MainClusterer.class);

    private static final int MONTHS_TO_INCLUDE = 36;
    private static final int DAYS_TO_INCLUDE = MONTHS_TO_INCLUDE * Constants.BUSINESS_DAYS_IN_MONTH;

    public static void main(String[] args) {
        BasicConfigurator.configure();

        MovingAverageCalculator movingAverageCalculator = new MovingAverageCalculator(Fund::getAnnualisedReturn);

        LOG.info("Loading fund files");
        List<Fund> fundMonthlyReturns = Arrays.stream(Objects.requireNonNull(Constants.MARKET_DATA_FOLDER.toFile().listFiles()))
                .map(x -> FundFileReader.readFromCsv(x.toPath()))
                .filter(x -> x.getPrices().size() >= DAYS_TO_INCLUDE)
                .map(x -> x.view(DAYS_TO_INCLUDE, 0))
                .map(x -> movingAverageCalculator.calculateAsFund(x, Constants.BUSINESS_DAYS_IN_MONTH))
                .collect(Collectors.toList());

        LOG.info("Calculating correlation matrix");
        CorrelationMatrix correlationMatrix = new CorrelationMatrix(fundMonthlyReturns);

        LOG.info("Finding clusters");
        CorrelationClusterer clusterBuilder = new CorrelationClusterer();
        List<CentroidCluster<ClusterableFund>> clusters = clusterBuilder.cluster(fundMonthlyReturns, correlationMatrix);

        double totalCorrelation = 0.0;
        for (CentroidCluster<ClusterableFund> cluster : clusters) {
            double clusterCorrelation = 0.0;
            double minCorrelation = 1.0;
            for (ClusterableFund fund1 : cluster.getPoints()) {
                for (ClusterableFund fund2 : cluster.getPoints()) {
                    double correlation = correlationMatrix.getCorrelation(fund1.getFund(), fund2.getFund());
                    clusterCorrelation += (1.0 - correlation);
                    minCorrelation = Math.min(minCorrelation, correlation);
                }
            }
            if (cluster.getPoints().size() > 0)
                clusterCorrelation /= ((double)cluster.getPoints().size() * (double)cluster.getPoints().size());
            totalCorrelation += clusterCorrelation;
            String clusterDescription = Joiner.on('\n').join(cluster.getPoints().stream()
                    .sorted(Comparator.comparingDouble(x -> -x.getFund().getMean()))
                    .map(x -> x.getFund().getName() + " - " + x.getFund().getMean())
                    .collect(Collectors.toList()));
            LOG.info("--- Cluster " + cluster.getPoints().size() + " " + clusterCorrelation + " " + minCorrelation + " --- \n" + clusterDescription + "\n");

            // TODO: eliminate non-equity funds a-priori (except those with high returns?)
            // TODO: run clusterer several times and somehow merge results, or implement a different clusterer
            // TODO: if clusterCorrelation (i.e. lack of correlation) too high (like > 0.3 / 0.4) and/or minCorrelation (i.e. outlier's correlation)
            //          too low (like < 0.4 / 0.6) and/or too many funds in cluster (like > 50), consider splitting the cluster further by
            //          calling clusterBuilder.cluster() again just on that specific subset of funds
            // TODO: remove clusters where most funds have low mean returns automatically
            // TODO: from resulting portfolio, simplify by removing funds when there's very high correlation
        }
        LOG.info("Total correlation: " + totalCorrelation);
    }
}
