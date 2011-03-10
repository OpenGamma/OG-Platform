/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class GaussianCharacteristicExponentTest {
  private static final double MU = 0.4;
  private static final double SIGMA = 0.8;
  private static final GaussianCharacteristicExponent1 EXPONENT = new GaussianCharacteristicExponent1(MU, SIGMA);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSigma() {
    new GaussianCharacteristicExponent1(MU, -SIGMA);
  }

  @Test
  public void test() {
    assertEquals(EXPONENT.getMu(), MU, 0);
    assertEquals(EXPONENT.getSigma(), SIGMA, 0);
    GaussianCharacteristicExponent1 other = new GaussianCharacteristicExponent1(MU, SIGMA);
    assertEquals(other, EXPONENT);
    assertEquals(other.hashCode(), EXPONENT.hashCode());
    other = new GaussianCharacteristicExponent1(MU + 1, SIGMA);
    assertFalse(other.equals(EXPONENT));
    other = new GaussianCharacteristicExponent1(MU, SIGMA + 1);
    assertFalse(other.equals(EXPONENT));
  }

  @Test
  public void testAlphas() {
    assertEquals(EXPONENT.getSmallestAlpha(), Double.NEGATIVE_INFINITY, 0);
    assertEquals(EXPONENT.getLargestAlpha(), Double.POSITIVE_INFINITY, 0);
  }
}
