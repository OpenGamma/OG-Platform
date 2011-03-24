/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class CornishFisherDeltaGammaVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final Function<Double, Double> ZERO = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      return 0.;
    }

  };
  private static final Function<Double, Double> STD = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      return 0.3;
    }

  };
  private static final Function<Double, Double> SKEW = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      return 1.5;
    }

  };
  private static final Function<Double, Double> KURTOSIS = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      return 5.6;
    }

  };
  private static final Function<Double, Double> NORMAL = new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, ZERO, STD);
  private static final CornishFisherDeltaGammaVaRCalculator<Double> CF1 = new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, ZERO, STD, ZERO, ZERO);
  private static final CornishFisherDeltaGammaVaRCalculator<Double> CF2 = new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, ZERO, STD, SKEW, KURTOSIS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(-HORIZON, PERIODS, QUANTILE, ZERO, ZERO, ZERO, ZERO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePeriod() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, -PERIODS, QUANTILE, ZERO, ZERO, ZERO, ZERO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeQuantile() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, -QUANTILE, ZERO, ZERO, ZERO, ZERO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighQuantile() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, 1 + QUANTILE, ZERO, ZERO, ZERO, ZERO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, null, ZERO, ZERO, ZERO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, ZERO, null, ZERO, ZERO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator3() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, ZERO, ZERO, null, ZERO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator4() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, ZERO, ZERO, ZERO, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CF1.evaluate((Double[]) null);
  }

  @Test
  public void testAgainstNormal() {
    final double eps = 1e-6;
    final Double[] data = new Double[] {0.};
    assertEquals(NORMAL.evaluate(data), CF1.evaluate(data), eps);
    assertTrue(CF2.evaluate(data) > NORMAL.evaluate(data));
  }

  @Test
  public void testEqualsAndHashCode() {
    assertEquals(CF2.getHorizon(), HORIZON, 0);
    assertEquals(CF2.getKurtosisCalculator(), KURTOSIS);
    assertEquals(CF2.getMeanCalculator(), ZERO);
    assertEquals(CF2.getPeriods(), PERIODS, 0);
    assertEquals(CF2.getQuantile(), QUANTILE, 0);
    assertEquals(CF2.getSkewCalculator(), SKEW);
    assertEquals(CF2.getStandardDeviationCalculator(), STD);
    CornishFisherDeltaGammaVaRCalculator<Double> other = new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, ZERO, STD, SKEW, KURTOSIS);
    assertEquals(other, CF2);
    assertEquals(other.hashCode(), CF2.hashCode());
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON + 1, PERIODS, QUANTILE, ZERO, STD, SKEW, KURTOSIS);
    assertFalse(other.equals(CF2));
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS + 1, QUANTILE, ZERO, STD, SKEW, KURTOSIS);
    assertFalse(other.equals(CF2));
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE * 0.1, ZERO, STD, SKEW, KURTOSIS);
    assertFalse(other.equals(CF2));
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, STD, STD, SKEW, KURTOSIS);
    assertFalse(other.equals(CF2));
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, ZERO, ZERO, SKEW, KURTOSIS);
    assertFalse(other.equals(CF2));
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, ZERO, STD, ZERO, KURTOSIS);
    assertFalse(other.equals(CF2));
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, ZERO, STD, SKEW, ZERO);
    assertFalse(other.equals(CF2));
  }

}
