/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class MeanCorrectedCharacteristicExponentTest {
  private static final double SMALL_ALPHA = 1;
  private static final double LARGE_ALPHA = 5;
  private static final CharacteristicExponent1 BASE = new MyCharacteristicExponent(SMALL_ALPHA, LARGE_ALPHA);
  private static final MeanCorrectedCharacteristicExponent1 EXPONENT = new MeanCorrectedCharacteristicExponent1(BASE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullFirst() {
    new MeanCorrectedCharacteristicExponent1(null);
  }

  @Test
  public void test() {
    assertEquals(BASE, EXPONENT.getBase());
    MeanCorrectedCharacteristicExponent1 other = new MeanCorrectedCharacteristicExponent1(BASE);
    assertEquals(other, EXPONENT);
    assertEquals(other.hashCode(), EXPONENT.hashCode());
    other = new MeanCorrectedCharacteristicExponent1(new MyCharacteristicExponent(SMALL_ALPHA, LARGE_ALPHA));
    assertFalse(other.equals(EXPONENT));
  }

  @Test
  public void testAlpha() {
    assertEquals(EXPONENT.getSmallestAlpha(), SMALL_ALPHA, 0);
    assertEquals(EXPONENT.getLargestAlpha(), LARGE_ALPHA, 0);
  }

  private static class MyCharacteristicExponent implements CharacteristicExponent1 {
    private final double _small;
    private final double _large;

    public MyCharacteristicExponent(final double small, final double large) {
      _small = small;
      _large = large;
    }

    @Override
    public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {
      return null;
    }

    @Override
    public double getLargestAlpha() {
      return _large;
    }

    @Override
    public double getSmallestAlpha() {
      return _small;
    }

  }
}
