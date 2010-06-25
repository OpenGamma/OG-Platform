/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.conditional;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.var.NormalStatistics;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class NormalLinearConditionalVaRCalculatorTest {
  private static final double PERIODS = 250;
  private static final double HORIZON = 10;
  private static final double QUANTILE = 0.99;
  private static final NormalLinearConditionalVaRCalculator F = new NormalLinearConditionalVaRCalculator(HORIZON, PERIODS, QUANTILE);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    new NormalLinearConditionalVaRCalculator(-HORIZON, PERIODS, QUANTILE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePeriod() {
    new NormalLinearConditionalVaRCalculator(HORIZON, -PERIODS, QUANTILE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeQuantile() {
    new NormalLinearConditionalVaRCalculator(HORIZON, PERIODS, -QUANTILE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighQuantile() {
    new NormalLinearConditionalVaRCalculator(HORIZON, PERIODS, 1 + QUANTILE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetHorizon() {
    F.setHorizon(-100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetPeriods() {
    F.setPeriods(-10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetLowQuantile() {
    F.setQuantile(-0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetHighQuantile() {
    F.setQuantile(1.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullStatistics() {
    F.evaluate((NormalStatistics<?>) null);
  }

  @Test
  public void test() {
    final Function1D<Double, Double> mean = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 0.;
      }

    };
    final Function1D<Double, Double> std = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 0.3;
      }

    };
    assertEquals(F.evaluate(new NormalStatistics<Double>(mean, std, 0.)), 0.1599, 1e-4);
  }
}
