/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.libor.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.financial.interestrate.libor.Libor;

/**
 * 
 */
public class LiborTest {
  public static final String CURVE_NAME = "test";

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTime() {
    new Libor(-3, CURVE_NAME);
  }

  @Test
  public void test() {
    final double time = 12;
    final Libor libor = new Libor(time, CURVE_NAME);
    assertEquals(libor.getPaymentTime(), time, 0);
    Libor other = new Libor(time, CURVE_NAME);
    assertEquals(other, libor);
    assertEquals(other.hashCode(), libor.hashCode());
    other = new Libor(time + 1, CURVE_NAME);
    assertFalse(other.equals(libor));
  }

}
