/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.number.ComplexNumber;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TimeChangeCharacteristicExponentTest {
  private static final double SMALL_ALPHA1 = -1;
  private static final double SMALL_ALPHA2 = -6;
  private static final double LARGE_ALPHA1 = 7;
  private static final double LARGE_ALPHA2 = 5;
  private static final CharacteristicExponent BASE = new MyCharacteristicExponent(SMALL_ALPHA1, LARGE_ALPHA1);
  private static final StocasticClockCharcteristicExponent TIME_CHANGE = new MyCharacteristicExponent(SMALL_ALPHA2, LARGE_ALPHA2);
  private static final TimeChangedCharacteristicExponent EXPONENT = new TimeChangedCharacteristicExponent(BASE, TIME_CHANGE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirst() {
    new TimeChangedCharacteristicExponent(null, TIME_CHANGE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecond() {
    new TimeChangedCharacteristicExponent(BASE, null);
  }

  @Test
  public void test() {
    assertEquals(BASE, EXPONENT.getBase());
    assertEquals(TIME_CHANGE, EXPONENT.getTimeChange());
    TimeChangedCharacteristicExponent other = new TimeChangedCharacteristicExponent(BASE, TIME_CHANGE);
    assertEquals(other, EXPONENT);
    assertEquals(other.hashCode(), EXPONENT.hashCode());
    other = new TimeChangedCharacteristicExponent(TIME_CHANGE, TIME_CHANGE);
    assertFalse(other.equals(EXPONENT));
    other = new TimeChangedCharacteristicExponent(BASE, (StocasticClockCharcteristicExponent)BASE);
    assertFalse(other.equals(EXPONENT));
  }

  @Test
  public void testAlpha() {
    assertEquals(EXPONENT.getSmallestAlpha(), SMALL_ALPHA1, 0);
    assertEquals(EXPONENT.getLargestAlpha(), LARGE_ALPHA2, 0);
  }

  private static class MyCharacteristicExponent implements StocasticClockCharcteristicExponent{
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

    @Override
    public ComplexNumber getValue(ComplexNumber u, double t) {
      return null;
    }

    @Override
    public ComplexNumber[] getCharacteristicExponentAdjoint(ComplexNumber u, double t) {
      throw new NotImplementedException();
    }

    @Override
    public Function1D<ComplexNumber, ComplexNumber[]> getAdjointFunction(double t) {
      throw new NotImplementedException();
    }

  }
}
