package com.mt3.portfoliomanager.portfoliooptimiser.main;

import com.google.common.base.Joiner;
import com.mt3.portfoliomanager.portfoliooptimiser.Constants;
import com.mt3.portfoliomanager.portfoliooptimiser.CorrelationMatrix;
import com.mt3.portfoliomanager.portfoliooptimiser.clustering.ClusterBuilder;
import com.mt3.portfoliomanager.portfoliooptimiser.clustering.ClusterableFund;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.FundFileReader;
import com.mt3.portfoliomanager.portfoliooptimiser.movingaverage.MovingAverageCalculator;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MainClusterer {

    private static final Logger LOG = Logger.getLogger(MainClusterer.class);

    public static void main(String[] args) {
        BasicConfigurator.configure();

        MovingAverageCalculator movingAverageCalculator = new MovingAverageCalculator(Fund::getAnnualisedReturn);

        LOG.info("Loading fund files");
        List<Fund> fundMonthlyReturns = Arrays.stream(Objects.requireNonNull(Constants.MARKET_DATA_FOLDER.toFile().listFiles()))
                .map(x -> FundFileReader.readFromCsv(x.toPath()))
                .filter(x -> x.getPrices().size() >= 36 * Constants.BUSINESS_DAYS_IN_MONTH)
                .map(x -> movingAverageCalculator.calculateAsFund(x, Constants.BUSINESS_DAYS_IN_MONTH))
                .collect(Collectors.toList());

        LOG.info("Calculating correlation matrix");
        CorrelationMatrix correlationMatrix = new CorrelationMatrix(fundMonthlyReturns);

        LOG.info("Finding clusters");
        ClusterBuilder clusterBuilder = new ClusterBuilder();
        List<CentroidCluster<ClusterableFund>> clusters = clusterBuilder.cluster(fundMonthlyReturns, correlationMatrix);

        double totalCorrelation = 0.0;
        for (CentroidCluster<ClusterableFund> cluster : clusters) {
            double clusterCorrelation = 0.0;
            for (ClusterableFund fund1 : cluster.getPoints()) {
                for (ClusterableFund fund2 : cluster.getPoints()) {
                    clusterCorrelation += (1.0 - correlationMatrix.getCorrelation(fund1.getFund(), fund2.getFund()));
                }
            }
            totalCorrelation += clusterCorrelation;
            String clusterDescription = Joiner.on('\n').join(cluster.getPoints().stream().map(x -> x.getFund().getName()).collect(Collectors.toList()));
            LOG.info("--- Cluster " + cluster.getPoints().size() + " " + clusterCorrelation + " --- \n" + clusterDescription + "\n");
        }
        LOG.info("Total correlation: " + totalCorrelation);
    }
}
