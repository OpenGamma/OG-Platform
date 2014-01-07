/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class StudentTVaRParametersTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final double DOF = 4;
  private static final StudentTVaRParameters STUDENT_T = new StudentTVaRParameters(HORIZON, PERIODS, QUANTILE, DOF);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    new StudentTVaRParameters(-HORIZON, PERIODS, QUANTILE, DOF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePeriod() {
    new StudentTVaRParameters(HORIZON, -PERIODS, QUANTILE, DOF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeQuantile() {
    new StudentTVaRParameters(HORIZON, PERIODS, -QUANTILE, DOF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighQuantile() {
    new StudentTVaRParameters(HORIZON, PERIODS, 1 + QUANTILE, DOF);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeDOF() {
    new StudentTVaRParameters(HORIZON, PERIODS, QUANTILE, -DOF);
  }
  
  @Test
  public void testHashCodeAndEquals() {
    assertEquals(STUDENT_T.getDegreesOfFreedom(), DOF, 0);
    assertEquals(STUDENT_T.getHorizon(), HORIZON, 0);
    assertEquals(STUDENT_T.getPeriods(), PERIODS, 0);
    assertEquals(STUDENT_T.getQuantile(), QUANTILE, 0);
    StudentTVaRParameters other = new StudentTVaRParameters(HORIZON, PERIODS, QUANTILE, DOF);
    assertEquals(STUDENT_T, other);
    assertEquals(STUDENT_T.hashCode(), other.hashCode());
    other = new StudentTVaRParameters(HORIZON + 1, PERIODS, QUANTILE, DOF);
    assertFalse(other.equals(STUDENT_T));
    other = new StudentTVaRParameters(HORIZON, PERIODS + 1, QUANTILE, DOF);
    assertFalse(other.equals(STUDENT_T));
    other = new StudentTVaRParameters(HORIZON, PERIODS, QUANTILE * 0.5, DOF);
    assertFalse(other.equals(STUDENT_T));
    other = new StudentTVaRParameters(HORIZON, PERIODS, QUANTILE, DOF + 1);
    assertFalse(other.equals(STUDENT_T));

  }

}
