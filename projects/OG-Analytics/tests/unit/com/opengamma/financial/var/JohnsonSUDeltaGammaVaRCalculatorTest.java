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

/**
 * 
 */
public class JohnsonSUDeltaGammaVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = 0.99;
  private static final Function<Double, Double> MEAN = new MyFunction(0.);
  private static final Function<Double, Double> STD = new MyFunction(0.25);
  private static final Function<Double, Double> SKEW = new MyFunction(-0.2);
  private static final Function<Double, Double> KURTOSIS = new MyFunction(4.);
  private static final JohnsonSUDeltaGammaVaRCalculator<Double> F = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD, SKEW, KURTOSIS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    new JohnsonSUDeltaGammaVaRCalculator<Double>(-HORIZON, PERIODS, QUANTILE, MEAN, STD, SKEW, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePeriods() {
    new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, -PERIODS, QUANTILE, MEAN, STD, SKEW, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighQuantile() {
    new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE + 1, MEAN, STD, SKEW, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowQuantile() {
    new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE - 1, MEAN, STD, SKEW, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, null, STD, SKEW, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, null, SKEW, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator3() {
    new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD, null, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator4() {
    new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD, SKEW, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate((Double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeKurtosis() {
    final Function<Double, Double> k = new MyFunction(-0.4);
    final JohnsonSUDeltaGammaVaRCalculator<Double> f = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD, SKEW, k);
    f.evaluate(new Double[0]);
  }

  @Test
  public void testNormal() {
    final Function<Double, Double> zero = new MyFunction(0.0);
    final NormalLinearVaRCalculator<Double> normal = new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD);
    final JohnsonSUDeltaGammaVaRCalculator<Double> f = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD, zero, zero);
    assertEquals(normal.evaluate(0.), f.evaluate(0.), 1e-9);
  }

  @Test
  public void test() {
    assertEquals(F.evaluate(0.), 0.1376, 1e-4);
  }

  @Test
  public void testEqualsAndHashCode() {
    assertEquals(F.getHorizon(), HORIZON, 0);
    assertEquals(F.getKurtosisCalculator(), KURTOSIS);
    assertEquals(F.getMeanCalculator(), MEAN);
    assertEquals(F.getPeriods(), PERIODS, 0);
    assertEquals(F.getQuantile(), QUANTILE, 0);
    assertEquals(F.getSkewCalculator(), SKEW);
    assertEquals(F.getStdCalculator(), STD);
    JohnsonSUDeltaGammaVaRCalculator<Double> other = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD, SKEW, KURTOSIS);
    assertEquals(other, F);
    assertEquals(other.hashCode(), F.hashCode());
    other = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON + 1, PERIODS, QUANTILE, MEAN, STD, SKEW, KURTOSIS);
    assertFalse(other.equals(F));
    other = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS + 1, QUANTILE, MEAN, STD, SKEW, KURTOSIS);
    assertFalse(other.equals(F));
    other = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE * 0.5, MEAN, STD, SKEW, KURTOSIS);
    assertFalse(other.equals(F));
    other = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, STD, STD, SKEW, KURTOSIS);
    assertFalse(other.equals(F));
    other = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, MEAN, SKEW, KURTOSIS);
    assertFalse(other.equals(F));
    other = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD, MEAN, KURTOSIS);
    assertFalse(other.equals(F));
    other = new JohnsonSUDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD, SKEW, MEAN);
    assertFalse(other.equals(F));
  }

  private static class MyFunction implements Function<Double, Double> {
    private final double _x;

    public MyFunction(final double x) {
      _x = x;
    }

    @Override
    public Double evaluate(final Double... x) {
      return _x;
    }

  }
}
