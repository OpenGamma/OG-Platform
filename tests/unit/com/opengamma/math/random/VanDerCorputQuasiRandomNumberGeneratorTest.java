/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * 
 */
public class VanDerCorputQuasiRandomNumberGeneratorTest {
  private static final VanDerCorputQuasiRandomNumberGenerator GENERATOR = new VanDerCorputQuasiRandomNumberGenerator(10);
  private static final double EPS = 1e-15;

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() {
    new VanDerCorputQuasiRandomNumberGenerator(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetBase() {
    GENERATOR.setBase(-1);
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
    final double[] values = new double[] {.1, .2, .3, .4, .5, .6, .7, .8, .9, .01, .11, .21, .31, .41, .51, .61, .71, .81, .91, .02, .12, .22, .32};
    final List<double[]> data = new ArrayList<double[]>();
    for (final double value : values) {
      data.add(new double[] {value});
    }
    test(GENERATOR.getVectors(1, values.length), data);
  }

  private void test(final List<double[]> result, final List<double[]> data) {
    assertEquals(result.size(), data.size());
    double[] d1, d2;
    for (int i = 0; i < result.size(); i++) {
      d1 = result.get(i);
      d2 = data.get(i);
      assertEquals(d1.length, d2.length);
      assertEquals(d1.length, 1);
      assertEquals(d1[0], d2[0], EPS);
    }
  }
}
