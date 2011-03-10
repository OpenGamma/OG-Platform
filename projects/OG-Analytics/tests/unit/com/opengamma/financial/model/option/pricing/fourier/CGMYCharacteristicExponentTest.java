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
public class CGMYCharacteristicExponentTest {
  private static final double C = 3;
  private static final double G = 2;
  private static final double M = 9;
  private static final double Y = 1;
  private static final CGMYCharacteristicExponent1 EXPONENT = new CGMYCharacteristicExponent1(C, G, M, Y);

  @Test(expected = IllegalArgumentException.class)
  public void testWrongC() {
    new CGMYCharacteristicExponent1(-C, G, M, Y);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongG() {
    new CGMYCharacteristicExponent1(C, -G, M, Y);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongM() {
    new CGMYCharacteristicExponent1(C, G, -M, Y);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongY() {
    new CGMYCharacteristicExponent1(C, G, M, Y + 10);
  }

  @Test
  public void test() {
    assertEquals(EXPONENT.getC(), C, 0);
    assertEquals(EXPONENT.getG(), G, 0);
    assertEquals(EXPONENT.getM(), M, 0);
    assertEquals(EXPONENT.getY(), Y, 0);
    CGMYCharacteristicExponent1 other = new CGMYCharacteristicExponent1(C, G, M, Y);
    assertEquals(other, EXPONENT);
    assertEquals(other.hashCode(), EXPONENT.hashCode());
    other = new CGMYCharacteristicExponent1(C + 1, G, M, Y);
    assertFalse(other.equals(EXPONENT));
    other = new CGMYCharacteristicExponent1(C, G + 1, M, Y);
    assertFalse(other.equals(EXPONENT));
    other = new CGMYCharacteristicExponent1(C, G, M + 1, Y);
    assertFalse(other.equals(EXPONENT));
    other = new CGMYCharacteristicExponent1(C, G, M, Y - 10);
    assertFalse(other.equals(EXPONENT));
  }
}
