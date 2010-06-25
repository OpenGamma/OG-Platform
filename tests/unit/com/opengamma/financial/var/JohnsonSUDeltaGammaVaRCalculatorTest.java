/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class JohnsonSUDeltaGammaVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = 0.99;
  private static final VaRCalculator<SkewKurtosisStatistics<?>> F = new JohnsonSUDeltaGammaVaRCalculator(HORIZON, PERIODS, QUANTILE);
  private static final Function1D<Double, Double> ZERO = new MyFunction1D(0);

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate((SkewKurtosisStatistics<?>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeKurtosis() {
    final SkewKurtosisStatistics<?> stats = new SkewKurtosisStatistics<Double>(ZERO, ZERO, ZERO, new MyFunction1D(-1), 0.);
    F.evaluate(stats);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidParameters() {
    F.evaluate(new SkewKurtosisStatistics<Double>(ZERO, ZERO, new MyFunction1D(0.), new MyFunction1D(0.1), 0.));
  }

  @Test
  public void testNormal() {
    final Function1D<Double, Double> mean = new MyFunction1D(0.5);
    final Function1D<Double, Double> std = new MyFunction1D(0.65);
    final VaRCalculator<NormalStatistics<?>> normal = new NormalLinearVaRCalculator(HORIZON, PERIODS, QUANTILE);
    final SkewKurtosisStatistics<Double> stats = new SkewKurtosisStatistics<Double>(mean, std, ZERO, ZERO, 0.);
    assertEquals(normal.evaluate(stats), F.evaluate(stats), 1e-9);
  }

  @Test
  public void test() {
    final SkewKurtosisStatistics<Double> stats = new SkewKurtosisStatistics<Double>(ZERO, new MyFunction1D(0.25), new MyFunction1D(-0.2), new MyFunction1D(4), 0.);
    assertEquals(F.evaluate(stats), 0.1376, 1e-4);
  }

  private static class MyFunction1D extends Function1D<Double, Double> {
    private final double _x;

    public MyFunction1D(final double x) {
      _x = x;
    }

    @Override
    public Double evaluate(final Double x) {
      return _x;
    }

  }

  @Test
  public void testEqualsAndHashCode() {
    JohnsonSUDeltaGammaVaRCalculator f = new JohnsonSUDeltaGammaVaRCalculator(HORIZON, PERIODS, QUANTILE);
    assertEquals(f, F);
    assertEquals(f.hashCode(), F.hashCode());
    f.setHorizon(HORIZON - 1);
    assertFalse(f.equals(F));
    f.setHorizon(HORIZON);
    f.setPeriods(PERIODS - 1);
    assertFalse(f.equals(F));
    f.setPeriods(PERIODS);
    f.setQuantile(0.95);
    assertFalse(f.equals(F));
  }
}
