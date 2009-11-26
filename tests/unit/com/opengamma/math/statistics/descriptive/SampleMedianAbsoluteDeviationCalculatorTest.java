/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 */
public class SampleMedianAbsoluteDeviationCalculatorTest {
  private static final double MEAN = 3.5;
  private static final double STD = 0.2;
  private static final Function1D<Double[], Double> MAD_CALC = new SampleMedianAbsoluteDeviationCalculator();
  private static final Function1D<Double[], Double> STD_CALC = new SampleStandardDeviationCalculator();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalProbabilityDistribution(MEAN, STD);
  private static final Double[] NORMAL_DATA = new Double[50000];
  private static final double EPS = 1e-2;
  static {
    for (int i = 0; i < 50000; i++) {
      NORMAL_DATA[i] = NORMAL.nextRandom();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    MAD_CALC.evaluate((Double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientData() {
    MAD_CALC.evaluate(new Double[] { 3. });
  }

  @Test
  public void test() {
    assertEquals(STD_CALC.evaluate(NORMAL_DATA), MAD_CALC.evaluate(NORMAL_DATA) / .6745, EPS);
  }
}
