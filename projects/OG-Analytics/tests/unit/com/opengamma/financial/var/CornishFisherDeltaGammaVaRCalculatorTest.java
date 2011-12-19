/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class CornishFisherDeltaGammaVaRCalculatorTest {
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final NormalVaRParameters CORNISH_FISHER_PARAMETERS = new NormalVaRParameters(10, 250, QUANTILE);
  private static final NormalVaRParameters PARAMETERS = new NormalVaRParameters(10, 250, QUANTILE);
  private static final Function1D<Double, Double> ZERO = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 0.;
    }

  };
  private static final Function1D<Double, Double> STD = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 0.3;
    }

  };
  private static final Function1D<Double, Double> SKEW = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 1.5;
    }

  };
  private static final Function1D<Double, Double> KURTOSIS = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 5.6;
    }

  };
  private static final NormalLinearVaRCalculator<Double> NORMAL = new NormalLinearVaRCalculator<Double>(ZERO, STD);
  private static final CornishFisherDeltaGammaVaRCalculator<Double> CF1 = new CornishFisherDeltaGammaVaRCalculator<Double>(ZERO, STD, ZERO, ZERO);
  private static final CornishFisherDeltaGammaVaRCalculator<Double> CF2 = new CornishFisherDeltaGammaVaRCalculator<Double>(ZERO, STD, SKEW, KURTOSIS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(null, ZERO, ZERO, ZERO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(ZERO, null, ZERO, ZERO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator3() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(ZERO, ZERO, null, ZERO);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator4() {
    new CornishFisherDeltaGammaVaRCalculator<Double>(ZERO, ZERO, ZERO, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullParameters() {
    CF1.evaluate(null, 3.);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CF1.evaluate(CORNISH_FISHER_PARAMETERS, (Double[]) null);
  }

  @Test
  public void testAgainstNormal() {
    final double eps = 1e-6;
    final Double data = 0.;
    assertEquals(NORMAL.evaluate(PARAMETERS, data), CF1.evaluate(PARAMETERS, data), eps);
    assertTrue(CF2.evaluate(PARAMETERS, data) > NORMAL.evaluate(PARAMETERS, data));
  }

  @Test
  public void testEqualsAndHashCode() {
    assertEquals(CF2.getKurtosisCalculator(), KURTOSIS);
    assertEquals(CF2.getMeanCalculator(), ZERO);
    assertEquals(CF2.getSkewCalculator(), SKEW);
    assertEquals(CF2.getStandardDeviationCalculator(), STD);
    CornishFisherDeltaGammaVaRCalculator<Double> other = new CornishFisherDeltaGammaVaRCalculator<Double>(ZERO, STD, SKEW, KURTOSIS);
    assertEquals(other, CF2);
    assertEquals(other.hashCode(), CF2.hashCode());
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(STD, STD, SKEW, KURTOSIS);
    assertFalse(other.equals(CF2));
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(ZERO, ZERO, SKEW, KURTOSIS);
    assertFalse(other.equals(CF2));
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(ZERO, STD, ZERO, KURTOSIS);
    assertFalse(other.equals(CF2));
    other = new CornishFisherDeltaGammaVaRCalculator<Double>(ZERO, STD, SKEW, ZERO);
    assertFalse(other.equals(CF2));
  }

}
