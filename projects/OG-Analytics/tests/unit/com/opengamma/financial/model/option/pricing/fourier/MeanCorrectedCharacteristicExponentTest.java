/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class MeanCorrectedCharacteristicExponentTest {
  private static final double SMALL_ALPHA = 1;
  private static final double LARGE_ALPHA = 5;
  private static final CharacteristicExponent BASE = new MyCharacteristicExponent(SMALL_ALPHA, LARGE_ALPHA);
  private static final MeanCorrectedCharacteristicExponent EXPONENT = new MeanCorrectedCharacteristicExponent(BASE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirst() {
    new MeanCorrectedCharacteristicExponent(null);
  }

  @Test
  public void test() {
    assertEquals(BASE, EXPONENT.getBase());
    MeanCorrectedCharacteristicExponent other = new MeanCorrectedCharacteristicExponent(BASE);
    assertEquals(other, EXPONENT);
    assertEquals(other.hashCode(), EXPONENT.hashCode());
    other = new MeanCorrectedCharacteristicExponent(new MyCharacteristicExponent(SMALL_ALPHA, LARGE_ALPHA));
    assertFalse(other.equals(EXPONENT));
  }

  @Test
  public void testAlpha() {
    assertEquals(EXPONENT.getSmallestAlpha(), SMALL_ALPHA, 0);
    assertEquals(EXPONENT.getLargestAlpha(), LARGE_ALPHA, 0);
  }

  private static class MyCharacteristicExponent implements CharacteristicExponent {
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
