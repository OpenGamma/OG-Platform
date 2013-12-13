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
public class GaussianCharacteristicExponentTest {
  private static final double MU = 0.4;
  private static final double SIGMA = 0.8;
  private static final GaussianCharacteristicExponent EXPONENT = new GaussianCharacteristicExponent(MU, SIGMA);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSigma() {
    new GaussianCharacteristicExponent(MU, -SIGMA);
  }

  @Test
  public void test() {
    assertEquals(EXPONENT.getMu(), MU, 0);
    assertEquals(EXPONENT.getSigma(), SIGMA, 0);
    GaussianCharacteristicExponent other = new GaussianCharacteristicExponent(MU, SIGMA);
    assertEquals(other, EXPONENT);
    assertEquals(other.hashCode(), EXPONENT.hashCode());
    other = new GaussianCharacteristicExponent(MU + 1, SIGMA);
    assertFalse(other.equals(EXPONENT));
    other = new GaussianCharacteristicExponent(MU, SIGMA + 1);
    assertFalse(other.equals(EXPONENT));
  }

  @Test
  public void testAlphas() {
    assertEquals(EXPONENT.getSmallestAlpha(), Double.NEGATIVE_INFINITY, 0);
    assertEquals(EXPONENT.getLargestAlpha(), Double.POSITIVE_INFINITY, 0);
  }
}
