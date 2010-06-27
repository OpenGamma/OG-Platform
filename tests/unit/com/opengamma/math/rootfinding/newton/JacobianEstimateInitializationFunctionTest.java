/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.FiniteDifferenceJacobianCalculator;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.math.rootfinding.newton.JacobianEstimateInitializationFunction;

/**
 * 
 */
public class JacobianEstimateInitializationFunctionTest {
  private static final JacobianCalculator CALCULATOR = new FiniteDifferenceJacobianCalculator();
  private static final JacobianEstimateInitializationFunction ESTIMATE = new JacobianEstimateInitializationFunction(CALCULATOR);
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> F = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D v) {
      double[] x = v.getData();
      return new DoubleMatrix1D(new double[] {x[0] * x[0] + 1, x[1] * x[2], 3 * x[2] * x[1], x[3] * x[3]});
    }

  };
  private static final DoubleMatrix1D X = new DoubleMatrix1D(new double[] {1, 2, 3, 4});

  @Test(expected = IllegalArgumentException.class)
  public void testNullJacobianCalculator() {
    new JacobianEstimateInitializationFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    ESTIMATE.getInitializedMatrix(null, X);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVector() {
    ESTIMATE.getInitializedMatrix(F, null);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test() {
    DoubleMatrix2D m1 = ESTIMATE.getInitializedMatrix(F, X);
    DoubleMatrix2D m2 = CALCULATOR.evaluate(X, F);
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        assertEquals(m1.getEntry(i, j), m2.getEntry(i, j), 1e-9);
      }
    }
  }
}
