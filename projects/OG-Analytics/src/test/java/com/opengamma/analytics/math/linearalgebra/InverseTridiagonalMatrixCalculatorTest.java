/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import static com.opengamma.analytics.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InverseTridiagonalMatrixCalculatorTest {
  private static final InverseTridiagonalMatrixCalculator CALCULATOR = new InverseTridiagonalMatrixCalculator();
  private static final double[] A = new double[] {1.0, 2.4, -0.4, -0.8, 1.5, 7.8, -5.0, 1.0, 2.4, -0.4, 3.14};
  private static final double[] B = new double[] {1.56, 0.33, 0.42, -0.23, 0.276, 4.76, 1.0, 2.4, -0.4, 0.2355};
  private static final double[] C = new double[] {0.56, 0.63, -0.42, -0.23, 0.76, 1.76, 1.0, 2.4, -0.4, 2.4234};

  private static final TridiagonalMatrix MATRIX = new TridiagonalMatrix(A, B, C);
  private static final DoubleMatrix2D TRI = MATRIX.toDoubleMatrix2D();
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((TridiagonalMatrix) null);
  }

  @Test
  public void testInvertIdentity() {
    final int n = 11;
    final double[] a = new double[n];
    final double[] b = new double[n - 1];
    final double[] c = new double[n - 1];
    int i, j;

    for (i = 0; i < n; i++) {
      a[i] = 1.0;
    }
    final DoubleMatrix2D res = CALCULATOR.evaluate(new TridiagonalMatrix(a, b, c));
    for (i = 0; i < n; i++) {
      for (j = 0; j < n; j++) {
        assertEquals((i == j ? 1.0 : 0.0), res.getEntry(i, j), EPS);
      }
    }

  }

  @Test
  public void testInvert() {
    final DoubleMatrix2D res = CALCULATOR.evaluate(MATRIX);
    final DoubleMatrix2D idet = (DoubleMatrix2D) OG_ALGEBRA.multiply(TRI, res);

    final int n = idet.getNumberOfRows();
    int i, j;
    for (i = 0; i < n; i++) {
      for (j = 0; j < n; j++) {
        assertEquals((i == j ? 1.0 : 0.0), idet.getEntry(i, j), EPS);
      }
    }

  }

}
