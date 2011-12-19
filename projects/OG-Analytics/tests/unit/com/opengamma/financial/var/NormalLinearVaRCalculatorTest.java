/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class NormalLinearVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final NormalVaRParameters PARAMETERS = new NormalVaRParameters(HORIZON, PERIODS, QUANTILE);
  private static final Function1D<Double, Double> MEAN_CALCULATOR = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 0.4;
    }

  };
  private static final Function1D<Double, Double> STD_CALCULATOR = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 1.;
    }

  };
  private static final NormalLinearVaRCalculator<Double> CALCULATOR = new NormalLinearVaRCalculator<Double>(MEAN_CALCULATOR, STD_CALCULATOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new NormalLinearVaRCalculator<Double>(null, STD_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new NormalLinearVaRCalculator<Double>(MEAN_CALCULATOR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullParameters() {
    CALCULATOR.evaluate(null, 0.);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.evaluate(PARAMETERS, (Double[]) null);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(PARAMETERS, 0.), 3 * 0.2 - 0.016, 1e-9);
  }

  @Test
  public void testEqualsHashCodeAndGetters() {
    assertEquals(CALCULATOR.getMeanCalculator(), MEAN_CALCULATOR);
    assertEquals(CALCULATOR.getStandardDeviationCalculator(), STD_CALCULATOR);
    NormalLinearVaRCalculator<Double> other = new NormalLinearVaRCalculator<Double>(MEAN_CALCULATOR, STD_CALCULATOR);
    assertEquals(other, CALCULATOR);
    assertEquals(other.hashCode(), CALCULATOR.hashCode());
    other = new NormalLinearVaRCalculator<Double>(STD_CALCULATOR, STD_CALCULATOR);
    assertFalse(CALCULATOR.equals(other));
    other = new NormalLinearVaRCalculator<Double>(MEAN_CALCULATOR, MEAN_CALCULATOR);
    assertFalse(CALCULATOR.equals(other));
  }
}
