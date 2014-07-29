/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.SVDecompositionColt;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDecomposition() {
    new InverseJacobianEstimateInitializationFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    ESTIMATE.getInitializedMatrix(null, X);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
