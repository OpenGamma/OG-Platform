/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class NewtonDefaultUpdateFunctionTest {
  private static final NewtonDefaultUpdateFunction F = new NewtonDefaultUpdateFunction(new FiniteDifferenceJacobianCalculator());

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator() {
    new NewtonDefaultUpdateFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    F.getUpdatedMatrix(null, null, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVector() {
    F.getUpdatedMatrix(new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        return null;
      }
    }, null, null, null, null);
  }

}
