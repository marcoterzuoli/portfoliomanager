package com.mt3.portfoliomanager.portfoliooptimiser.movingaverage;

import com.mt3.portfoliomanager.portfoliooptimiser.fund.Fund;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import org.junit.Assert;
import org.junit.Test;

public class MovingAverageCalculatorTest {

    private static final double EPSILON = 0.00001;

    @Test
    public void testPerfectSplit() {
        TDoubleList prices = new TDoubleArrayList(new double[] {
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0
        });
        Fund fund = new Fund("name", prices);
        MovingAverageCalculator calculator = new MovingAverageCalculator(Fund::getTotalReturn);
        double[] returns = calculator.calculate(fund, 3);
        Assert.assertEquals(2, returns.length);
        Assert.assertEquals(3.0 / 1.0, returns[0], EPSILON);
        Assert.assertEquals(6.0 / 4.0, returns[1], EPSILON);
    }

    @Test
    public void testImperfectSplit() {
        TDoubleList prices = new TDoubleArrayList(new double[] {
                1.0, 2.0, 3.0, 4.0, 5.0
        });
        Fund fund = new Fund("name", prices);
        MovingAverageCalculator calculator = new MovingAverageCalculator(Fund::getTotalReturn);
        double[] returns = calculator.calculate(fund, 2);
        Assert.assertEquals(2, returns.length);
        Assert.assertEquals(3.0 / 2.0, returns[0], EPSILON);
        Assert.assertEquals(5.0 / 4.0, returns[1], EPSILON);
    }

    @Test
    public void testIncludeEach() {
        TDoubleList prices = new TDoubleArrayList(new double[] {
                1.0, 2.0, 3.0, 4.0, 5.0
        });
        Fund fund = new Fund("name", prices);
        MovingAverageCalculator calculator = new MovingAverageCalculator(Fund::getTotalReturn);
        double[] returns = calculator.calculate(fund, 1);
        Assert.assertEquals(5, returns.length);
        for (double ret : returns)
            Assert.assertEquals(1.0, ret, EPSILON);
    }

    @Test
    public void testFrequencyEqualsLength() {
        TDoubleList prices = new TDoubleArrayList(new double[] {
                1.0, 2.0, 3.0, 4.0, 5.0
        });
        Fund fund = new Fund("name", prices);
        MovingAverageCalculator calculator = new MovingAverageCalculator(Fund::getTotalReturn);
        double[] returns = calculator.calculate(fund, 5);
        Assert.assertEquals(1, returns.length);
        Assert.assertEquals(5.0, returns[0], EPSILON);
    }

    @Test
    public void testFrequencyGreaterThanLength() {
        TDoubleList prices = new TDoubleArrayList(new double[] {
                1.0, 2.0, 3.0, 4.0, 5.0
        });
        Fund fund = new Fund("name", prices);
        MovingAverageCalculator calculator = new MovingAverageCalculator(Fund::getTotalReturn);
        double[] returns = calculator.calculate(fund, 6);
        Assert.assertEquals(0, returns.length);
    }

    @Test
    public void testFractionalFrequency() {
        TDoubleList prices = new TDoubleArrayList(new double[] {
                1.0, 2.0, 3.0, 4.0, 5.0
        });
        Fund fund = new Fund("name", prices);
        MovingAverageCalculator calculator = new MovingAverageCalculator(Fund::getTotalReturn);
        double[] returns = calculator.calculate(fund, 2.5);
        Assert.assertEquals(2, returns.length);
        Assert.assertEquals(3.0, returns[0], EPSILON);
        Assert.assertEquals(5.0 / 4.0, returns[1], EPSILON);
    }
}
