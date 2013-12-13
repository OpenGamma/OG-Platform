/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.QRDecomposition;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class QRDecompositionCommonsResultTest {
  protected static final RealMatrix H = new Array2DRowRealMatrix(new double[][] {new double[] {11, 12}, new double[] {13, 14}});
  protected static final RealMatrix Q = new Array2DRowRealMatrix(new double[][] {new double[] {15, 16}, new double[] {17, 18}});
  protected static final RealMatrix R = new Array2DRowRealMatrix(new double[][] {new double[] {19, 20}, new double[] {21, 22}});
  protected static final RealMatrix Q_T = new Array2DRowRealMatrix(new double[][] {new double[] {15, 17}, new double[] {16, 18}});
  protected static final RealMatrix RESULT_2D = new Array2DRowRealMatrix(new double[][] {new double[] {1, 2}, new double[] {3, 4}});
  protected static final RealVector RESULT_1D = new ArrayRealVector(new double[] {1, 2});
  protected static final DecompositionSolver SOLVER = new MyDecompositionSolver();
  private static final QRDecompositionResult QR = new QRDecompositionCommonsResult(new MyQRDecomposition());

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullQR() {
    new QRDecompositionCommonsResult(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    QR.solve((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVector() {
    QR.solve((DoubleMatrix1D) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMatrix() {
    QR.solve((DoubleMatrix2D) null);
  }

  @Test
  public void testGetters() {
    assertRealMatrixEquals(H, QR.getH());
    assertRealMatrixEquals(Q, QR.getQ());
    assertRealMatrixEquals(R, QR.getR());
    assertRealMatrixEquals(Q_T, QR.getQT());
  }

  @Test
  public void testSolvers() {
    assertTrue(Arrays.equals(RESULT_1D.getData(), QR.solve(RESULT_1D.getData())));
    assertTrue(Arrays.equals(RESULT_1D.getData(), QR.solve(new DoubleMatrix1D(RESULT_1D.getData())).getData()));
    assertRealMatrixEquals(RESULT_2D, QR.solve(new DoubleMatrix2D(RESULT_2D.getData())));
  }

  private void assertRealMatrixEquals(final RealMatrix m1, final DoubleMatrix2D m2) {
    final int m = m1.getRowDimension();
    final int n = m1.getColumnDimension();
    assertEquals(m, m2.getNumberOfRows());
    assertEquals(n, m2.getNumberOfColumns());
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        assertEquals(m1.getEntry(i, j), m2.getEntry(i, j), 0);
      }
    }
  }

  protected static class MyQRDecomposition implements QRDecomposition {

    @Override
    public RealMatrix getH() {
      return H;
    }

    @Override
    public RealMatrix getQ() {
      return Q;
    }

    @Override
    public RealMatrix getQT() {
      return Q_T;
    }

    @Override
    public RealMatrix getR() {
      return R;
    }

    @Override
    public DecompositionSolver getSolver() {
      return SOLVER;
    }
  }

  protected static class MyDecompositionSolver implements DecompositionSolver {
    @Override
    public RealMatrix getInverse() throws InvalidMatrixException {
      return null;
    }

    @Override
    public boolean isNonSingular() {
      return false;
    }

    @Override
    public double[] solve(final double[] b) throws IllegalArgumentException, InvalidMatrixException {
      return RESULT_1D.toArray();
    }

    @Override
    public RealVector solve(final RealVector b) throws IllegalArgumentException, InvalidMatrixException {
      return RESULT_1D;
    }

    @Override
    public RealMatrix solve(final RealMatrix b) throws IllegalArgumentException, InvalidMatrixException {
      return RESULT_2D;
    }
  }
}
