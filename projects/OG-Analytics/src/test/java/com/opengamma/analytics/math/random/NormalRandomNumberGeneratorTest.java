/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.random;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NormalRandomNumberGeneratorTest {
  private static final NormalRandomNumberGenerator GENERATOR = new NormalRandomNumberGenerator(0, 1);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructor1() {
    new NormalRandomNumberGenerator(0, -1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructor2() {
    new NormalRandomNumberGenerator(0, -1, new MersenneTwister64());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructor3() {
    new NormalRandomNumberGenerator(0, 1, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadDimension() {
    GENERATOR.getVectors(-1, 4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
