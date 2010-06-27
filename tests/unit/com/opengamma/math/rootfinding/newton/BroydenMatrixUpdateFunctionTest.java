/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class BroydenMatrixUpdateFunctionTest {
  private static final BroydenMatrixUpdateFunction UPDATE = new BroydenMatrixUpdateFunction();
  private static final DoubleMatrix1D V = new DoubleMatrix1D(new double[] {1, 2});
  private static final DoubleMatrix2D M = new DoubleMatrix2D(new double[][] {new double[] {3, 4}, new double[] {5, 6}});
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> F = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
      return x;
    }

  };

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    UPDATE.getUpdatedMatrix(null, V, V, M);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDeltaX() {
    UPDATE.getUpdatedMatrix(F, null, V, M);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDeltaY() {
    UPDATE.getUpdatedMatrix(F, V, null, M);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMatrix() {
    UPDATE.getUpdatedMatrix(F, V, V, null);
  }
}
