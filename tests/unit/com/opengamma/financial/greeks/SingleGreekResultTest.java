/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SingleGreekResultTest {

  @Test(expected = NullPointerException.class)
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

  @Test
  public void testHashCode() {
    final SingleGreekResult sgr1 = new SingleGreekResult(1.);
    SingleGreekResult sgr2 = new SingleGreekResult(1.);
    assertTrue(sgr1.hashCode() == sgr2.hashCode());

    sgr2 = new SingleGreekResult(2.);
    assertFalse(sgr1.hashCode() == sgr2.hashCode());
  }

  @Test
  public void testEquals() {
    final SingleGreekResult sgr1 = new SingleGreekResult(1.);
    assertTrue(sgr1.equals(sgr1));
    assertFalse(sgr1.equals(null));
    assertFalse(sgr1.equals("foo"));

    SingleGreekResult sgr2 = new SingleGreekResult(1.);
    assertTrue(sgr1.equals(sgr2));

    sgr2 = new SingleGreekResult(2.);
    assertFalse(sgr1.equals(sgr2));
  }
}
