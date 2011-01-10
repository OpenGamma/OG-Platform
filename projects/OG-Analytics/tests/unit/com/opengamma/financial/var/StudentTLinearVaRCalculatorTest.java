/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
  private static final NormalLinearVaRCalculator<Double> NORMAL = new NormalLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, MEAN, STD);
  private static final StudentTLinearVaRCalculator<Double> HIGH_DOF = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, 1000000, MEAN, STD);
  private static final StudentTLinearVaRCalculator<Double> STUDENT_T = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, DOF, MEAN, STD);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    new StudentTLinearVaRCalculator<Double>(-HORIZON, PERIODS, QUANTILE, DOF, MEAN, STD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePeriod() {
    new StudentTLinearVaRCalculator<Double>(HORIZON, -PERIODS, QUANTILE, DOF, MEAN, STD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeQuantile() {
    new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, -QUANTILE, DOF, MEAN, STD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighQuantile() {
    new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, 1 + QUANTILE, DOF, MEAN, STD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDOF() {
    new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, -DOF, MEAN, STD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, DOF, null, STD);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, DOF, MEAN, null);
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
    assertEquals(STUDENT_T.getDegreesOfFreedom(), DOF, 0);
    assertEquals(STUDENT_T.getHorizon(), HORIZON, 0);
    assertEquals(STUDENT_T.getPeriods(), PERIODS, 0);
    assertEquals(STUDENT_T.getQuantile(), QUANTILE, 0);
    assertEquals(STUDENT_T.getMeanCalculator(), MEAN);
    assertEquals(STUDENT_T.getStandardDeviationCalculator(), STD);
    StudentTLinearVaRCalculator<Double> other = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, DOF, MEAN, STD);
    assertEquals(other, STUDENT_T);
    assertEquals(other.hashCode(), STUDENT_T.hashCode());
    other = new StudentTLinearVaRCalculator<Double>(HORIZON + 1, PERIODS, QUANTILE, DOF, MEAN, STD);
    assertFalse(other.equals(STUDENT_T));
    other = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS + 1, QUANTILE, DOF, MEAN, STD);
    assertFalse(other.equals(STUDENT_T));
    other = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE * 0.5, DOF, MEAN, STD);
    assertFalse(other.equals(STUDENT_T));
    other = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, DOF + 1, MEAN, STD);
    assertFalse(other.equals(STUDENT_T));
    other = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, DOF, STD, STD);
    assertFalse(other.equals(STUDENT_T));
    other = new StudentTLinearVaRCalculator<Double>(HORIZON, PERIODS, QUANTILE, DOF, MEAN, MEAN);
    assertFalse(other.equals(STUDENT_T));
  }
}
