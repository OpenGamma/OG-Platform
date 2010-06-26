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
public class HaltonQuasiRandomNumberGeneratorTest {
  private static final HaltonQuasiRandomNumberGenerator GENERATOR = new HaltonQuasiRandomNumberGenerator();
  private static final double EPS = 1e-15;

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
    final double[] x1 = new double[] {1. / 2, 1. / 4, 3. / 4, 1. / 8, 5. / 8, 3. / 8, 7. / 8, 1. / 16, 9. / 16};
    final double[] x2 = new double[] {1. / 3, 2. / 3, 1. / 9, 4. / 9, 7. / 9, 2. / 9, 5. / 9, 8. / 9, 1. / 27};
    final List<Double[]> data = new ArrayList<Double[]>();
    for (int i = 0; i < x1.length; i++) {
      data.add(new Double[] {x1[i], x2[i]});
    }
    test(GENERATOR.getVectors(2, x1.length), data);
  }

  private void test(final List<Double[]> result, final List<Double[]> data) {
    assertEquals(result.size(), data.size());
    Double[] d1, d2;
    for (int i = 0; i < result.size(); i++) {
      d1 = result.get(i);
      d2 = data.get(i);
      assertEquals(d1.length, d2.length);
      assertEquals(d1.length, 2);
      assertEquals(d1[0], d2[0], EPS);
      assertEquals(d1[1], d2[1], EPS);
    }
  }

}
