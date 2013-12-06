/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class IntegratedCIRTimeChangeCharacteristicExponentTest {
  private static final double KAPPA = 0.5;
  private static final double THETA = 0.8;
  private static final double LAMBDA = 1;
  private static final IntegratedCIRTimeChangeCharacteristicExponent EXPONENT = new IntegratedCIRTimeChangeCharacteristicExponent(KAPPA, THETA, LAMBDA);

  @Test
  public void test() {
    assertEquals(EXPONENT.getKappa(), KAPPA, 0);
    assertEquals(EXPONENT.getLambda(), LAMBDA, 0);
    assertEquals(EXPONENT.getTheta(), THETA, 0);
    IntegratedCIRTimeChangeCharacteristicExponent other = new IntegratedCIRTimeChangeCharacteristicExponent(KAPPA, THETA, LAMBDA);
    assertEquals(other, EXPONENT);
    assertEquals(other.hashCode(), EXPONENT.hashCode());
    other = new IntegratedCIRTimeChangeCharacteristicExponent(KAPPA + 1, THETA, LAMBDA);
    assertFalse(other.equals(EXPONENT));
    other = new IntegratedCIRTimeChangeCharacteristicExponent(KAPPA, THETA + 1, LAMBDA);
    assertFalse(other.equals(EXPONENT));
    other = new IntegratedCIRTimeChangeCharacteristicExponent(KAPPA, THETA, LAMBDA + 1);
    assertFalse(other.equals(EXPONENT));
  }

  @Test
  public void testAlphas() {
    assertEquals(EXPONENT.getSmallestAlpha(), Double.NEGATIVE_INFINITY, 0);
  }
}
