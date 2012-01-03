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
public class StudentTLinearVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final double DOF = 4;
  private static final NormalVaRParameters NORMAL_PARAMETERS = new NormalVaRParameters(HORIZON, PERIODS, QUANTILE);
  private static final StudentTVaRParameters HIGH_DOF_PARAMETERS = new StudentTVaRParameters(HORIZON, PERIODS, QUANTILE, 1000000);
  private static final StudentTVaRParameters STUDENT_T_PARAMETERS = new StudentTVaRParameters(HORIZON, PERIODS, QUANTILE, DOF);
  private static final Function1D<Double, Double> MEAN = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 0.4;
    }

  };
  private static final Function1D<Double, Double> STD = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 1.;
    }

  };
  private static final NormalLinearVaRCalculator<Double> NORMAL_VAR = new NormalLinearVaRCalculator<Double>(MEAN, STD);
  private static final StudentTLinearVaRCalculator<Double> STUDENT_T_VAR = new StudentTLinearVaRCalculator<Double>(MEAN, STD);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new StudentTLinearVaRCalculator<Double>(null, STD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new StudentTLinearVaRCalculator<Double>(MEAN, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullParameters() {
    STUDENT_T_VAR.evaluate(null, 0.);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    STUDENT_T_VAR.evaluate(STUDENT_T_PARAMETERS, (Double[]) null);
  }

  @Test
  public void test() {
    final Double data = 0.;
    assertEquals(NORMAL_VAR.evaluate(NORMAL_PARAMETERS, data), STUDENT_T_VAR.evaluate(HIGH_DOF_PARAMETERS, data), 1e-6);
    assertTrue(STUDENT_T_VAR.evaluate(STUDENT_T_PARAMETERS, data) > NORMAL_VAR.evaluate(NORMAL_PARAMETERS, data));
  }

  @Test
  public void testEqualsAndHashCode() {
    assertEquals(STUDENT_T_VAR.getMeanCalculator(), MEAN);
    assertEquals(STUDENT_T_VAR.getStandardDeviationCalculator(), STD);
    StudentTLinearVaRCalculator<Double> other = new StudentTLinearVaRCalculator<Double>(MEAN, STD);
    assertEquals(other, STUDENT_T_VAR);
    assertEquals(other.hashCode(), STUDENT_T_VAR.hashCode());
    other = new StudentTLinearVaRCalculator<Double>(STD, STD);
    assertFalse(other.equals(STUDENT_T_VAR));
    other = new StudentTLinearVaRCalculator<Double>(MEAN, MEAN);
    assertFalse(other.equals(STUDENT_T_VAR));
  }
}
