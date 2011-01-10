/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class VolatilityAnnualizingFunctionTest {
  private static final double DAYS_PER_YEAR = 360;
  private static final double WORKING_DAYS_PER_YEAR = 250;

  @Test(expected = IllegalArgumentException.class)
  public void testNegative() {
    new VolatilityAnnualizingFunction(-DAYS_PER_YEAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull1() {
    new VolatilityAnnualizingFunction(DAYS_PER_YEAR).evaluate((Double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull2() {
    new VolatilityAnnualizingFunction(DAYS_PER_YEAR).evaluate((Double) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    new VolatilityAnnualizingFunction(DAYS_PER_YEAR).evaluate(new Double[0]);
  }

  @Test
  public void test() {
    final VolatilityAnnualizingFunction f1 = new VolatilityAnnualizingFunction(DAYS_PER_YEAR);
    final VolatilityAnnualizingFunction f2 = new VolatilityAnnualizingFunction(WORKING_DAYS_PER_YEAR);
    final double eps = 1e-12;
    assertEquals(f1.evaluate(10.), 6, eps);
    assertEquals(f2.evaluate(10.), 5., eps);
  }
}
