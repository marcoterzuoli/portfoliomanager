package com.mt3.portfoliomanager.main;

import com.mt3.portfoliomanager.Constants;
import com.mt3.portfoliomanager.CorrelationMatrix;
import com.mt3.portfoliomanager.fund.Fund;
import com.mt3.portfoliomanager.fund.FundFileReader;
import com.mt3.portfoliomanager.movingaverage.MovingAverageCalculator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MainCorrelator {

    private static final Logger LOG = Logger.getLogger(MainClusterer.class);

    private static final double MONTHS_TO_INCLUDE_IN_REPORT = 60.0;
    private static final int DAYS_TO_INCLUDE_IN_REPORT = (int)(MONTHS_TO_INCLUDE_IN_REPORT * Constants.BUSINESS_DAYS_IN_MONTH);
    private static final double MONTHS_TO_INCLUDE_IN_CALCULATION = 36.0;
    private static final int DAYS_TO_INCLUDE_IN_CALCULATION = (int)(MONTHS_TO_INCLUDE_IN_CALCULATION * Constants.BUSINESS_DAYS_IN_MONTH);

    private static double annualise(double returnOver3Years) {
        return Math.pow(returnOver3Years, 1.0 / 3.0);
    }

    public static void main(String[] args) throws FileNotFoundException {
        BasicConfigurator.configure();

        MovingAverageCalculator movingAverageCalculator = new MovingAverageCalculator(Fund::getTotalReturn);

        LOG.info("Loading fund files");
        List<Fund> allFunds = Arrays.stream(Objects.requireNonNull(Constants.MARKET_DATA_FOLDER.toFile().listFiles()))
                .filter(File::isFile)
                .map(x -> FundFileReader.readFromCsv(x.toPath()))
                .collect(Collectors.toList());

        LOG.info("Calculating 3-year returns");
        List<Fund> fundMonthlyReturns = allFunds.stream()
                .filter(x -> x.getPrices().size() >= DAYS_TO_INCLUDE_IN_CALCULATION)
                .map(x -> x.view(DAYS_TO_INCLUDE_IN_CALCULATION, 0))
                .map(x -> movingAverageCalculator.calculateAsFund(x, Constants.BUSINESS_DAYS_IN_MONTH))
                .collect(Collectors.toList());

        LOG.info("Calculating yearly 5-year returns");
        List<Fund> fundYearlyReturns5Years = allFunds.stream()
                .filter(x -> x.getPrices().size() >= DAYS_TO_INCLUDE_IN_REPORT)
                .map(x -> x.view(DAYS_TO_INCLUDE_IN_REPORT, 0))
                .map(x -> movingAverageCalculator.calculateAsFund(x, Constants.BUSINESS_DAYS_IN_YEAR))
                .collect(Collectors.toList());
        LOG.info("Calculating yearly 3-year returns");
        List<Fund> fundYearlyReturns3Years = allFunds.stream()
                .filter(x -> x.getPrices().size() >= DAYS_TO_INCLUDE_IN_CALCULATION)
                .map(x -> x.view(DAYS_TO_INCLUDE_IN_CALCULATION, 0))
                .map(x -> movingAverageCalculator.calculateAsFund(x, Constants.BUSINESS_DAYS_IN_YEAR))
                .collect(Collectors.toList());

        LOG.info("Loading my portfolio");
        List<Fund> currentPortfolio = FundFileReader.readPortfolioFromFile(Paths.get(args[0]), fundMonthlyReturns);
        List<Fund> notInPortfolio = fundMonthlyReturns.stream()
                .filter(x -> !currentPortfolio.contains(x))
                .collect(Collectors.toList());

        LOG.info("Calculating correlation matrix");
        CorrelationMatrix correlationMatrix = new CorrelationMatrix(fundMonthlyReturns);

        LOG.info("Calculating possible replacements from outside of portfolio");
        for (Fund fund1 : currentPortfolio) {
            Fund bestReplacement = fund1;
            for (Fund fund2 : notInPortfolio) {
                double correlation = correlationMatrix.getCorrelation(fund1, fund2);
                if (correlation > 0.9 && bestReplacement.getProductReturn() < fund2.getProductReturn()) {
                    bestReplacement = fund2;
                }
            }
            if (bestReplacement != fund1) {
                double improvement = annualise(bestReplacement.getProductReturn()) - annualise(fund1.getProductReturn());
                if (improvement > 0.02) {
                    double correlation = correlationMatrix.getCorrelation(fund1, bestReplacement);
                    LOG.info("Replace " + fund1.getName() + " and " + bestReplacement.getName() + ": correlation is " + correlation +
                            " and yearly return improvement is from " + annualise(fund1.getProductReturn())
                            + " to " + annualise(bestReplacement.getProductReturn()) +
                            ", with improvement of " + improvement);
                }
            }
        }

        LOG.info("Calculating possible merges in portfolio");
        for (Fund fund1 : currentPortfolio) {
            for (Fund fund2 : currentPortfolio) {
                if (fund1 != fund2) {
                    double correlation = correlationMatrix.getCorrelation(fund1, fund2);
                    if (correlation > 0.85) {
                        LOG.info("Merge " + fund1.getName() + " and " + fund2.getName() + ": correlation is " + correlation);
                    }
                }
            }
        }

        LOG.info("Calculating possible additions from outside of portfolio");
        for (Fund fund2 : notInPortfolio) {
            Fund fund5Years = fundYearlyReturns5Years.stream()
                    .filter(x -> x.getName().equals(fund2.getName()))
                    .findFirst()
                    .orElse(null);
            if (fund2.getMax() > 1.1 && annualise(fund2.getProductReturn()) > 1.1) {
                if (fund5Years == null || annualise(fund2.getProductReturn()) > 1.1) {
                    double highestCorrelation = currentPortfolio.stream()
                            .mapToDouble(x -> correlationMatrix.getCorrelation(x, fund2))
                            .max()
                            .orElse(1.0);
                    if (highestCorrelation < 0.6) {
                        LOG.info("Add " + fund2.getName() + ": max correlation is " + highestCorrelation
                                + " and yearly return is " + annualise(fund2.getProductReturn())
                                + " and max monthly return is " + fund2.getMax());
                    }
                }
            }
        }

        // TODO: save csv with fund1, fund2, correlation and csv with fund1, returns in each of last 3 years, other stats like mean/product total and per year
        LOG.info("Saving all correlations");
        try (PrintWriter writer = new PrintWriter(args[1])) {
            writer.println("Fund1,Fund2,InPtf1,InPtf2,Correlation");
            for (Fund fund1 : allFunds) {
                for (Fund fund2 : allFunds) {
                    writer.println(fund1.getName() + "," + fund2.getName() + "," + currentPortfolio.contains(fund1) +
                            "," + currentPortfolio.contains(fund2) + "," + correlationMatrix.getCorrelation(fund1, fund2));
                }
            }
        }

        LOG.info("Saving all yearly returns");
        try (PrintWriter writer = new PrintWriter(args[2])) {
            writer.println("Fund,InPtf,T-5,T-4,T-3,T-2,T-1");
            for (Fund fund : allFunds) {
                Fund fund5Years = fundYearlyReturns5Years.stream()
                        .filter(x -> x.getName().equals(fund.getName()))
                        .findFirst()
                        .orElse(null);
                double[] returns = new double[5];
                if (fund5Years != null) {
                    returns = fund5Years.getPrices().toArray();
                } else {
                    Fund fund3Years = fundYearlyReturns3Years.stream()
                            .filter(x -> x.getName().equals(fund.getName()))
                            .findFirst()
                            .orElse(null);
                    if (fund3Years != null) {
                        for (int i = 0; i < 3; i++)
                            returns[i + 2] = fund3Years.getPrices().toArray()[i];
                    }
                }
                writer.println(fund.getName() + "," + currentPortfolio.contains(fund) +
                        "," + returns[0] + "," + returns[1] + "," + returns[2]
                        + "," + returns[3] + "," + returns[4]);
            }
        }

        // TODO: add fidelity parser to find all available funds
        // TODO: add zurich / standard life downloader
        // TODO: print portfolio.txt automatically
        // TODO: print best 3 replacement, not just best 1
    }
}
