/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.InterestRateModel;

/**
 * 
 */
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
