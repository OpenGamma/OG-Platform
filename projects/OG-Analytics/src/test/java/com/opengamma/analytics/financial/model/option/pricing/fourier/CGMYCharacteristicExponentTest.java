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
public class CGMYCharacteristicExponentTest {
  private static final double C = 3;
  private static final double G = 2;
  private static final double M = 9;
  private static final double Y = 1;
  private static final CGMYCharacteristicExponent EXPONENT = new CGMYCharacteristicExponent(C, G, M, Y);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongC() {
    new CGMYCharacteristicExponent(-C, G, M, Y);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongG() {
    new CGMYCharacteristicExponent(C, -G, M, Y);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongM() {
    new CGMYCharacteristicExponent(C, G, -M, Y);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongY() {
    new CGMYCharacteristicExponent(C, G, M, Y + 10);
  }

  @Test
  public void test() {
    assertEquals(EXPONENT.getC(), C, 0);
    assertEquals(EXPONENT.getG(), G, 0);
    assertEquals(EXPONENT.getM(), M, 0);
    assertEquals(EXPONENT.getY(), Y, 0);
    CGMYCharacteristicExponent other = new CGMYCharacteristicExponent(C, G, M, Y);
    assertEquals(other, EXPONENT);
    assertEquals(other.hashCode(), EXPONENT.hashCode());
    other = new CGMYCharacteristicExponent(C + 1, G, M, Y);
    assertFalse(other.equals(EXPONENT));
    other = new CGMYCharacteristicExponent(C, G + 1, M, Y);
    assertFalse(other.equals(EXPONENT));
    other = new CGMYCharacteristicExponent(C, G, M + 1, Y);
    assertFalse(other.equals(EXPONENT));
    other = new CGMYCharacteristicExponent(C, G, M, Y - 10);
    assertFalse(other.equals(EXPONENT));
  }
}
