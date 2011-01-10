/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class TopHatFunctionTest {
  private static final double X1 = 2;
  private static final double X2 = 2.5;
  private static final double Y = 10;
  private static final Function1D<Double, Double> F = new TopHatFunction(X1, X2, Y);

  @Test(expected = IllegalArgumentException.class)
  public void testWrongOrder() {
    new TopHatFunction(X2, X1, Y);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    F.evaluate((Double) null);
  }

  @Test
  public void test() {
    assertEquals(F.evaluate(X1 - 1e-15), 0, 0);
    assertEquals(F.evaluate(X2 + 1e-15), 0, 0);
    assertEquals(F.evaluate(X1), Y, 0);
    assertEquals(F.evaluate(X2), Y, 0);
    assertEquals(F.evaluate((X1 + X2) / 2), Y, 0);
  }
}
