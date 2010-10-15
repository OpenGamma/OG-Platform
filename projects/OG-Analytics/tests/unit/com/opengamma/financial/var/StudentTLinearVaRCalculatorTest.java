/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class StudentTLinearVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final double DOF = 4;
  private static final Function<Double, Double> MEAN = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      return 0.4;
    }

  };
  private static final Function<Double, Double> STD = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      return 1.;
    }

  };
  private static final NormalWithMeanLinearVaRCalculator<Double> NORMAL = new NormalWithMeanLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD);
  private static final StudentTLinearVaRCalculator<Double> HIGH_DOF = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, 1000000, MEAN, STD);
  private static final StudentTLinearVaRCalculator<Double> STUDENT_T = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, DOF, MEAN, STD);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDOF() {
    new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, -DOF, MEAN, STD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    STUDENT_T.evaluate((Double[]) null);
  }

  @Test
  public void test() {
    final Double[] data = new Double[0];
    assertEquals(NORMAL.evaluate(data), HIGH_DOF.evaluate(data), 1e-6);
    assertTrue(STUDENT_T.evaluate(data) > NORMAL.evaluate(data));
  }

  @Test
  public void testEqualsAndHashCode() {
    final StudentTLinearVaRCalculator<Double> studentT = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, DOF, MEAN, STD);
    assertEquals(studentT, STUDENT_T);
    assertEquals(studentT.hashCode(), STUDENT_T.hashCode());
  }
}
