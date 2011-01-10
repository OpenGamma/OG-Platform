/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

/**
 * 
 */
public class InverseJacobianEstimateInitializationFunctionTest {
  private static final MatrixAlgebra ALGEBRA = new CommonsMatrixAlgebra();
  private static final Decomposition<?> SV = new SVDecompositionColt();
  private static final InverseJacobianEstimateInitializationFunction ESTIMATE = new InverseJacobianEstimateInitializationFunction(SV);
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> J = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

    @Override
    public DoubleMatrix2D evaluate(DoubleMatrix1D v) {
      double[] x = v.getData();
      return new DoubleMatrix2D(new double[][] { {x[0] * x[0], x[0] * x[1]}, {x[0] - x[1], x[1] * x[1]}});
    }

  };

  private static final DoubleMatrix1D X = new DoubleMatrix1D(new double[] {3, 4});

  @Test(expected = IllegalArgumentException.class)
  public void testNullDecomposition() {
    new InverseJacobianEstimateInitializationFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    ESTIMATE.getInitializedMatrix(null, X);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVector() {
    ESTIMATE.getInitializedMatrix(J, null);
  }

  @Test
  public void test() {
    DoubleMatrix2D m1 = ESTIMATE.getInitializedMatrix(J, X);
    DoubleMatrix2D m2 = J.evaluate(X);
    DoubleMatrix2D m3 = (DoubleMatrix2D) (ALGEBRA.multiply(m1, m2));
    DoubleMatrix2D identity = DoubleMatrixUtils.getIdentityMatrix2D(2);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        assertEquals(m3.getEntry(i, j), identity.getEntry(i, j), 1e-6);
      }
    }
  }
}
