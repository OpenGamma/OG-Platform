/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;

/**
 * 
 */
public class NormalRandomNumberGeneratorTest {
  private static final NormalRandomNumberGenerator GENERATOR = new NormalRandomNumberGenerator(0, 1);

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor1() {
    new NormalRandomNumberGenerator(0, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor2() {
    new NormalRandomNumberGenerator(0, -1, new MersenneTwister64());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor3() {
    new NormalRandomNumberGenerator(0, 1, null);
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
    final List<double[]> result = GENERATOR.getVectors(10, 50);
    assertEquals(result.size(), 50);
    for (final double[] d : result) {
      assertEquals(d.length, 10);
    }
  }
}
