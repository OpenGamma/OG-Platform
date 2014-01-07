/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
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
  private static final NormalLinearVaRCalculator<Double> CALCULATOR = new NormalLinearVaRCalculator<>(MEAN_CALCULATOR, STD_CALCULATOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new NormalLinearVaRCalculator<>(null, STD_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new NormalLinearVaRCalculator<>(MEAN_CALCULATOR, null);
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
    final VaRCalculationResult calcResult = CALCULATOR.evaluate(PARAMETERS, 0.);
    assertEquals(calcResult.getVaRValue(), 3 * 0.2 - 0.016, 1e-9);
    assertEquals(calcResult.getStdDev(), 1., 1e-9);
  }

  @Test
  public void testEqualsHashCodeAndGetters() {
    assertEquals(CALCULATOR.getMeanCalculator(), MEAN_CALCULATOR);
    assertEquals(CALCULATOR.getStandardDeviationCalculator(), STD_CALCULATOR);
    NormalLinearVaRCalculator<Double> other = new NormalLinearVaRCalculator<>(MEAN_CALCULATOR, STD_CALCULATOR);
    assertEquals(other, CALCULATOR);
    assertEquals(other.hashCode(), CALCULATOR.hashCode());
    other = new NormalLinearVaRCalculator<>(STD_CALCULATOR, STD_CALCULATOR);
    assertFalse(CALCULATOR.equals(other));
    other = new NormalLinearVaRCalculator<>(MEAN_CALCULATOR, MEAN_CALCULATOR);
    assertFalse(CALCULATOR.equals(other));
  }
}
