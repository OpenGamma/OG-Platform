/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.number;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class ComplexNumberTest {

  @Test
  public void test() {
    final ComplexNumber z1 = new ComplexNumber(1, 2);
    final ComplexNumber z2 = new ComplexNumber(1, 2);
    assertEquals(Double.valueOf(1), Double.valueOf(z1.getReal()));
    assertEquals(Double.valueOf(2), Double.valueOf(z1.getImaginary()));
    assertEquals(z1, z2);
    try {
      z1.byteValue();
      fail();
    } catch (final UnsupportedOperationException e) {
      // Expected
    }
    try {
      z1.intValue();
      fail();
    } catch (final UnsupportedOperationException e) {
      // Expected
    }
    try {
      z1.longValue();
      fail();
    } catch (final UnsupportedOperationException e) {
      // Expected
    }
    try {
      z1.floatValue();
      fail();
    } catch (final UnsupportedOperationException e) {
      // Expected
    }
    try {
      z1.doubleValue();
      fail();
    } catch (final UnsupportedOperationException e) {
      // Expected
    }
    assertEquals("1.0 + 2.0i", z1.toString());
    assertEquals("1.0", new ComplexNumber(1, 0).toString());
    assertEquals("2.3i", new ComplexNumber(0, 2.3).toString());
  }
}
