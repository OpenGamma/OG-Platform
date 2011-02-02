/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math;

import static com.opengamma.math.UtilFunctions.fromTenorIndex;
import static com.opengamma.math.UtilFunctions.toTenorIndex;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class UtilFunctionsTest {
  private static final double EPS = 1e-15;

  @Test
  public void testSquare() {
    for (int i = 0; i < 100; i++) {
      final double x = Math.random();
      assertEquals(UtilFunctions.square(x), x * x, EPS);
    }
  }

  @Test
  public void testCube() {
    for (int i = 0; i < 100; i++) {
      final double x = Math.random();
      assertEquals(UtilFunctions.cube(x), x * x * x, EPS);
    }
  }

  @Test
  public void tenorIndexTest1() {

    int[] indicies = new int[] {2};
    int[] dimensions = new int[] {5};
    int index = toTenorIndex(indicies, dimensions);
    assertEquals(indicies[0], index, 0);

    int[] res = fromTenorIndex(index, dimensions);
    assertEquals(indicies[0], res[0], 0);

  }

  @Test
  public void tenorIndexTest2() {

    int[] indicies = new int[] {2, 3};
    int[] dimensions = new int[] {5, 7};
    int index = toTenorIndex(indicies, dimensions);
    int[] res = fromTenorIndex(index, dimensions);
    assertEquals(indicies[0], res[0], 0);
    assertEquals(indicies[1], res[1], 0);
  }

  @Test
  public void tenorIndexTest3() {

    int[] indicies = new int[] {2, 3, 1};
    int[] dimensions = new int[] {5, 7, 3};
    int index = toTenorIndex(indicies, dimensions);
    int[] res = fromTenorIndex(index, dimensions);
    assertEquals(indicies[0], res[0], 0);
    assertEquals(indicies[1], res[1], 0);
    assertEquals(indicies[2], res[2], 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOutOfBounds() {
    int[] indicies = new int[] {2, 7, 1};
    int[] dimensions = new int[] {5, 7, 3};
    toTenorIndex(indicies, dimensions);
  }
}
