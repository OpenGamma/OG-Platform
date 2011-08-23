/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class NormalLinearVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final Function<Double, Double> MEAN_CALCULATOR = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      return 0.4;
    }

  };
  private static final Function<Double, Double> STD_CALCULATOR = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      return 1.;
    }

  };
  private static final NormalLinearVaRCalculator<Double> CALCULATOR = new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN_CALCULATOR, STD_CALCULATOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    new NormalLinearVaRCalculator<Double>(-HORIZON, PERIODS, QUANTILE, MEAN_CALCULATOR, STD_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePeriod() {
    new NormalLinearVaRCalculator<Double>(HORIZON, -PERIODS, QUANTILE, MEAN_CALCULATOR, STD_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeQuantile() {
    new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, -QUANTILE, MEAN_CALCULATOR, STD_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighQuantile() {
    new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, 1 + QUANTILE, MEAN_CALCULATOR, STD_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, null, STD_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN_CALCULATOR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.evaluate((Double[]) null);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(0.), 3 * 0.2 - 0.016, 1e-9);
  }

  @Test
  public void testEqualsHashCodeAndGetters() {
    assertEquals(CALCULATOR.getHorizon(), HORIZON, 0);
    assertEquals(CALCULATOR.getPeriods(), PERIODS, 0);
    assertEquals(CALCULATOR.getQuantile(), QUANTILE, 0);
    assertEquals(CALCULATOR.getMeanCalculator(), MEAN_CALCULATOR);
    assertEquals(CALCULATOR.getStandardDeviationCalculator(), STD_CALCULATOR);
    NormalLinearVaRCalculator<Double> other = new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN_CALCULATOR, STD_CALCULATOR);
    assertEquals(other, CALCULATOR);
    assertEquals(other.hashCode(), CALCULATOR.hashCode());
    other = new NormalLinearVaRCalculator<Double>(HORIZON + 1, PERIODS, QUANTILE, MEAN_CALCULATOR, STD_CALCULATOR);
    assertFalse(CALCULATOR.equals(other));
    other = new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS + 1, QUANTILE, MEAN_CALCULATOR, STD_CALCULATOR);
    assertFalse(CALCULATOR.equals(other));
    other = new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE * 0.01, MEAN_CALCULATOR, STD_CALCULATOR);
    assertFalse(CALCULATOR.equals(other));
    other = new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, STD_CALCULATOR, STD_CALCULATOR);
    assertFalse(CALCULATOR.equals(other));
    other = new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN_CALCULATOR, MEAN_CALCULATOR);
    assertFalse(CALCULATOR.equals(other));
  }
}
