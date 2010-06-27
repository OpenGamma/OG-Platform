/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class NewtonDefaultUpdateFunctionTest {
  private static final NewtonDefaultUpdateFunction F = new NewtonDefaultUpdateFunction();

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    F.getUpdatedMatrix(null, null, null, null);
  }

  @Test
  public void test() {
    DoubleMatrix2D m = new DoubleMatrix2D(new double[][] {new double[] {1, 2, 3}, new double[] {4, 5, 6}, new double[] {7, 8, 9}});
    assertEquals(m, F.getUpdatedMatrix(null, null, null, m));
  }
}
