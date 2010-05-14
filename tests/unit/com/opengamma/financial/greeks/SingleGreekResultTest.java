/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class SingleGreekResultTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() {
    new SingleGreekResult(null);
  }

  @Test
  public void test() {
    final double value = 0.12;
    final GreekResult<Double> result = new SingleGreekResult(value);
    assertFalse(result.isMultiValued());
    assertEquals(value, result.getResult(), 0);
  }
}
