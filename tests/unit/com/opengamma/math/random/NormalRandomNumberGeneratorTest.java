/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * 
 */
public class NormalRandomNumberGeneratorTest {
  private static final NormalRandomNumberGenerator GENERATOR = new NormalRandomNumberGenerator(0, 1);

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() {
    new NormalRandomNumberGenerator(0, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadDimension() {
    GENERATOR.getVectors(-1, 4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadN() {
    GENERATOR.getVectors(1, -5);
  }

  @Test
  public void test() {
    final List<Double[]> result = GENERATOR.getVectors(10, 50);
    assertEquals(result.size(), 50);
    for (final Double[] d : result) {
      assertEquals(d.length, 10);
    }
  }
}
