/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.SVDecompositionColt;
import com.opengamma.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.DoubleMatrixUtils;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.rootfinding.newton.FiniteDifferenceJacobianCalculator;
import com.opengamma.math.rootfinding.newton.InverseJacobianEstimateInitializationFunction;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;

/**
 * 
 */
public class InverseJacobianEstimateInitializationFunctionTest {
  private static final MatrixAlgebra ALGEBRA = new CommonsMatrixAlgebra();
  private static final JacobianCalculator CALCULATOR = new FiniteDifferenceJacobianCalculator();
  private static final Decomposition<?> SV = new SVDecompositionColt();
  private static final InverseJacobianEstimateInitializationFunction ESTIMATE = new InverseJacobianEstimateInitializationFunction(SV, CALCULATOR);
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> F = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D v) {
      double[] x = v.getData();
      return new DoubleMatrix1D(new double[] {x[0] * x[0] + 1, x[1] * x[2], 3 * x[2] * x[1], x[3] * x[3]});
    }

  };
  private static final DoubleMatrix1D X = new DoubleMatrix1D(new double[] {1, 2, 3, 4});

  @Test(expected = IllegalArgumentException.class)
  public void testNullDecomposition() {
    new InverseJacobianEstimateInitializationFunction(null, CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator() {
    new InverseJacobianEstimateInitializationFunction(SV, null);
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
    DoubleMatrix2D m3 = (DoubleMatrix2D) (ALGEBRA.multiply(m1, m2));
    DoubleMatrix2D identity = DoubleMatrixUtils.getIdentityMatrix2D(4);
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        assertEquals(m3.getEntry(i, j), identity.getEntry(i, j), 1e-6);
      }
    }
  }
}
