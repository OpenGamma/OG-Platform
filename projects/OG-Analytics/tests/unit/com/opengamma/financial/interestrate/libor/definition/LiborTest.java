/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.libor.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class LiborTest {
  public static final String CURVE_NAME = "test";
  public static final double RATE = 0.01;

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeMaturity() {
    new Libor(-0.25, RATE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeValuationDate() {
    new Libor(-0.01, 0.25, 0.3, RATE, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeYearFraction() {
    new Libor(0.01, 0.25, -0.3, RATE, CURVE_NAME);
  }

  @Test
  public void test() {
    final double time = 1;
    final Libor libor = new Libor(time, RATE, CURVE_NAME);
    assertEquals(libor.getMaturity(), time, 0);
    assertEquals(libor.getTradeTime(), 0, 0);
    assertEquals(libor.getYearFraction(), time, 0);
    assertEquals(libor.getRate(), RATE, 0);
    assertEquals(libor.getYieldCurveName(), CURVE_NAME);
    Libor other = new Libor(time, RATE, CURVE_NAME);
    assertEquals(other, libor);
    assertEquals(other.hashCode(), libor.hashCode());
    // //
    other = new Libor(time, RATE, 0, time, CURVE_NAME);
    assertEquals(other, libor);
    assertEquals(other.hashCode(), libor.hashCode());
    // //
    other = new Libor(time + 0.01, RATE, CURVE_NAME);
    assertFalse(other.equals(libor));
    other = new Libor(time, RATE + 0.01, CURVE_NAME);
    assertFalse(other.equals(libor));
    other = new Libor(time, RATE, "");
    assertFalse(other.equals(libor));
  }

}
