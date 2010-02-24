/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * @author emcleod
 * 
 */
public class StudentTLinearVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final double DOF = 4;
  private static final Function1D<NormalStatistics<?>, Double> NORMAL = new NormalLinearVaRCalculator(HORIZON, PERIODS, QUANTILE);
  private static final Function1D<NormalStatistics<?>, Double> HIGH_DOF = new StudentTLinearVaRCalculator(HORIZON, PERIODS, QUANTILE, 1000000);
  private static final Function1D<NormalStatistics<?>, Double> STUDENT_T = new StudentTLinearVaRCalculator(HORIZON, PERIODS, QUANTILE, DOF);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDOF() {
    new StudentTLinearVaRCalculator(HORIZON, PERIODS, QUANTILE, -DOF);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetDOF() {
    ((StudentTLinearVaRCalculator) STUDENT_T).setDegreesOfFreedom(-4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    NORMAL.evaluate((NormalStatistics<?>) null);
  }

  @Test
  public void test() {
    final NormalStatistics<Double> stats = new NormalStatistics<Double>(new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 0.4;
      }
    }, new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 1.;
      }

    }, 0.);
    assertEquals(NORMAL.evaluate(stats), HIGH_DOF.evaluate(stats), 1e-6);
    assertTrue(STUDENT_T.evaluate(stats) > NORMAL.evaluate(stats));
  }
}
