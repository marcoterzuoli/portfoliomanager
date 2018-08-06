package com.mt3.portfoliomanager.portfoliooptimiser.main;

import com.mt3.portfoliomanager.portfoliooptimiser.Constants;
import com.mt3.portfoliomanager.portfoliooptimiser.CorrelationMatrix;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import com.mt3.portfoliomanager.portfoliooptimiser.fund.FundFileReader;
import com.mt3.portfoliomanager.portfoliooptimiser.movingaverage.MovingAverageCalculator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MainCorrelator {

    private static final Logger LOG = Logger.getLogger(MainClusterer.class);

    private static final int MONTHS_TO_INCLUDE = 36;
    private static final int DAYS_TO_INCLUDE = MONTHS_TO_INCLUDE * Constants.BUSINESS_DAYS_IN_MONTH;

    public static void main(String[] args) throws FileNotFoundException {
        BasicConfigurator.configure();

        MovingAverageCalculator movingAverageCalculator = new MovingAverageCalculator(Fund::getAnnualisedReturn);

        LOG.info("Loading fund files");
        List<Fund> allFunds = Arrays.stream(Objects.requireNonNull(Constants.MARKET_DATA_FOLDER.toFile().listFiles()))
                .filter(x -> x.isFile())
                .map(x -> FundFileReader.readFromCsv(x.toPath()))
                .collect(Collectors.toList());

        LOG.info("Calculating past returns");
        List<Fund> fundMonthlyReturns = allFunds.stream()
                .filter(x -> x.getPrices().size() >= DAYS_TO_INCLUDE)
                .map(x -> x.view(DAYS_TO_INCLUDE, 0))
                .map(x -> movingAverageCalculator.calculateAsFund(x, Constants.BUSINESS_DAYS_IN_MONTH))
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
                if (correlation > 0.9 && bestReplacement.getMean() < fund2.getMean()) {
                    bestReplacement = fund2;
                }
            }
            if (bestReplacement != fund1) {
                double correlation = correlationMatrix.getCorrelation(fund1, bestReplacement);
                LOG.info("Replace " + fund1.getName() + " and " + bestReplacement.getName() + ": correlation is " + correlation +
                        " and return improvement is from " + fund1.getMean() + " to " + bestReplacement.getMean() + ", with improvement of "
                        + (bestReplacement.getMean() - fund1.getMean()));
            }
        }

        LOG.info("Calculating possible merges in portfolio");
        for (Fund fund1 : currentPortfolio) {
            for (Fund fund2 : currentPortfolio) {
                if (fund1 != fund2) {
                    double correlation = correlationMatrix.getCorrelation(fund1, fund2);
                    if (correlation > 0.8) {
                        LOG.info("Merge " + fund1.getName() + " and " + fund2.getName() + ": correlation is " + correlation);
                    }
                }
            }
        }

        LOG.info("Calculating possible additions from outside of portfolio");
        for (Fund fund2 : notInPortfolio) {
            if (fund2.getMax() > 1.25 && fund2.getMean() > 1.1) {
                double highestCorrelation = currentPortfolio.stream()
                        .mapToDouble(x -> correlationMatrix.getCorrelation(x, fund2))
                        .max()
                        .getAsDouble();
                if (highestCorrelation < 0.3) {
                    LOG.info("Add " + fund2.getName() + ": max correlation is " + highestCorrelation + " and average return is " + fund2.getMean()
                            + " and product return is " + fund2.getProductReturn()
                            + " and max return is " + fund2.getMax());
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

        // TODO: add fidelity parser to find all available funds
        // TODO: print portfolio.txt automatically
        // TODO: print best 3 replacement, not just best 1
    }
}
