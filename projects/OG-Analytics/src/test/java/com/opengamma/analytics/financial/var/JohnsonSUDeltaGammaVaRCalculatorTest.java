/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class JohnsonSUDeltaGammaVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = 0.99;
  private static final Function<Double, Double> MEAN = new MyFunction(0.);
  private static final Function<Double, Double> STD = new MyFunction(0.25);
  private static final Function<Double, Double> SKEW = new MyFunction(-0.2);
  private static final Function<Double, Double> KURTOSIS = new MyFunction(4.);
  private static final JohnsonSUDeltaGammaVaRCalculator<Double> F = new JohnsonSUDeltaGammaVaRCalculator<>(MEAN, STD, SKEW, KURTOSIS);
  private static final NormalVaRParameters PARAMETERS = new NormalVaRParameters(HORIZON, PERIODS, QUANTILE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new JohnsonSUDeltaGammaVaRCalculator<>(null, STD, SKEW, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new JohnsonSUDeltaGammaVaRCalculator<>(MEAN, null, SKEW, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator3() {
    new JohnsonSUDeltaGammaVaRCalculator<>(MEAN, STD, null, KURTOSIS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator4() {
    new JohnsonSUDeltaGammaVaRCalculator<>(MEAN, STD, SKEW, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate(PARAMETERS, (Double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullParameters() {
    F.evaluate(null, 1.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeKurtosis() {
    final Function<Double, Double> k = new MyFunction(-0.4);
    final JohnsonSUDeltaGammaVaRCalculator<Double> f = new JohnsonSUDeltaGammaVaRCalculator<>(MEAN, STD, SKEW, k);
    f.evaluate(PARAMETERS, 0.);
  }

  @Test
  public void testNormal() {
    final Function<Double, Double> zero = new MyFunction(0.0);
    final NormalLinearVaRCalculator<Double> normal = new NormalLinearVaRCalculator<>(MEAN, STD);
    final JohnsonSUDeltaGammaVaRCalculator<Double> f = new JohnsonSUDeltaGammaVaRCalculator<>(MEAN, STD, zero, zero);
    assertEquals(normal.evaluate(PARAMETERS, 0.).getVaRValue(), f.evaluate(PARAMETERS, 0.).getVaRValue(), 1e-9);
  }

  @Test
  public void test() {
    assertEquals(F.evaluate(PARAMETERS, 0.).getVaRValue(), 0.1376, 1e-4);
  }

  @Test
  public void testEqualsAndHashCode() {
    assertEquals(F.getKurtosisCalculator(), KURTOSIS);
    assertEquals(F.getMeanCalculator(), MEAN);
    assertEquals(F.getSkewCalculator(), SKEW);
    assertEquals(F.getStdCalculator(), STD);
    JohnsonSUDeltaGammaVaRCalculator<Double> other = new JohnsonSUDeltaGammaVaRCalculator<>(MEAN, STD, SKEW, KURTOSIS);
    assertEquals(other, F);
    assertEquals(other.hashCode(), F.hashCode());
    other = new JohnsonSUDeltaGammaVaRCalculator<>(STD, STD, SKEW, KURTOSIS);
    assertFalse(other.equals(F));
    other = new JohnsonSUDeltaGammaVaRCalculator<>(MEAN, MEAN, SKEW, KURTOSIS);
    assertFalse(other.equals(F));
    other = new JohnsonSUDeltaGammaVaRCalculator<>(MEAN, STD, MEAN, KURTOSIS);
    assertFalse(other.equals(F));
    other = new JohnsonSUDeltaGammaVaRCalculator<>(MEAN, STD, SKEW, MEAN);
    assertFalse(other.equals(F));
  }

  private static class MyFunction extends Function1D<Double, Double> {
    private final double _x;

    public MyFunction(final double x) {
      _x = x;
    }

    @Override
    public Double evaluate(final Double x) {
      return _x;
    }

  }
}
