package com.mt3.portfoliomanager.marketscreener;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

public final class EquityCorrelator {

    private static final Logger LOG = Logger.getLogger(EquityCorrelator.class);

    private static final int DATE_MARGIN = 3;

    public double calculateCorrelation(Equity equity, int preDays, int postDays) {
        LOG.info("Calculating correlation for " + equity.getName());

        TDoubleList estimateJumps = new TDoubleArrayList();
        TDoubleList actualJumps = new TDoubleArrayList();

        Set<LocalDate> estimateDates = new TreeSet<>(equity.getEstimatedPrices().keySet());
        for (LocalDate estimateDateStart : estimateDates) {
            LocalDate estimateDateEnd = findNearestDate(estimateDateStart.plusDays(preDays), equity.getEstimatedPrices().keySet());
            if (estimateDateEnd == null)
                continue;

            LocalDate actualDateStart = findNearestDate(estimateDateStart.plusDays(preDays), equity.getActualPrices().keySet());
            if (actualDateStart == null)
                continue;

            LocalDate actualDateEnd = findNearestDate(actualDateStart.plusDays(postDays), equity.getActualPrices().keySet());
            if (actualDateEnd == null)
                continue;

            double estimateJump = equity.getEstimatedPrices().get(estimateDateEnd) - equity.getEstimatedPrices().get(estimateDateStart);
            estimateJumps.add(estimateJump);
            double actualJump = equity.getEstimatedPrices().get(actualDateEnd) - equity.getEstimatedPrices().get(actualDateStart);
            actualJumps.add(actualJump);
        }

        if (estimateJumps.size() <= 2)
            return Double.NaN;
        return new PearsonsCorrelation().correlation(estimateJumps.toArray(), actualJumps.toArray());
    }

    private LocalDate findNearestDate(LocalDate date, Set<LocalDate> searchDates) {
        for (int i = 0; i <= DATE_MARGIN; i++) {
            LocalDate targetDatePlus = date.plusDays(i);
            if (searchDates.contains(targetDatePlus))
                return targetDatePlus;
            LocalDate targetDateMinus = date.minusDays(i);
            if (searchDates.contains(targetDateMinus))
                return targetDateMinus;
        }
        return null;
    }
}
