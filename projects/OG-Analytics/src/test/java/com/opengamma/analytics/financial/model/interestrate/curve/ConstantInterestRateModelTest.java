/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.InterestRateModel;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ConstantInterestRateModelTest {
  private static final double EPS = 1e-15;
  private static final double RATE = 0.05;
  private static final InterestRateModel<Double> CURVE = new ConstantInterestRateModel(RATE);

  @Test
  public void test() {
    assertEquals(RATE, CURVE.getInterestRate(0.5), EPS);
  }

  @Test
  public void testHashCodeAndEquals() {
    ConstantInterestRateModel curve = new ConstantInterestRateModel(RATE);
    assertEquals(CURVE, curve);
    assertEquals(CURVE.hashCode(), curve.hashCode());
    curve = new ConstantInterestRateModel(RATE + 0.01);
    assertFalse(CURVE.equals(curve));
  }
}
