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
import org.apache.commons.math.linear.LUDecomposition;
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
public class LUDecompositionCommonsResultTest {
  static final double DETERMINANT = 3;
  static final int[] PIVOT = new int[] {1, 2, 3};
  static final RealMatrix L = new Array2DRowRealMatrix(new double[][] {new double[] {1, 2, 3}, new double[] {4, 5, 6}, new double[] {7, 8, 9}});
  static final RealMatrix U = new Array2DRowRealMatrix(new double[][] {new double[] {10, 11, 12}, new double[] {13, 14, 15}, new double[] {16, 17, 18}});
  static final RealMatrix P = new Array2DRowRealMatrix(new double[][] {new double[] {19, 20, 21}, new double[] {22, 23, 24}, new double[] {25, 26, 27}});
  static final RealMatrix RESULT_2D = new Array2DRowRealMatrix(new double[][] {new double[] {1, 2}, new double[] {3, 4}});
  static final RealVector RESULT_1D = new ArrayRealVector(new double[] {1, 2});
  static final DecompositionSolver SOLVER = new MyDecompositionSolver();
  private static final LUDecompositionResult LU = new LUDecompositionCommonsResult(new MyLUDecomposition());

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullQR() {
    new QRDecompositionCommonsResult(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    LU.solve((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVector() {
    LU.solve((DoubleMatrix1D) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMatrix() {
    LU.solve((DoubleMatrix2D) null);
  }

  @Test
  public void testGetters() {
    assertRealMatrixEquals(L, LU.getL());
    assertRealMatrixEquals(U, LU.getU());
    assertRealMatrixEquals(P, LU.getP());
    assertEquals(DETERMINANT, LU.getDeterminant(), 0);
    assertTrue(Arrays.equals(PIVOT, LU.getPivot()));
  }

  @Test
  public void testSolvers() {
    assertTrue(Arrays.equals(RESULT_1D.getData(), LU.solve(RESULT_1D.getData())));
    assertTrue(Arrays.equals(RESULT_1D.getData(), LU.solve(new DoubleMatrix1D(RESULT_1D.getData())).getData()));
    assertRealMatrixEquals(RESULT_2D, LU.solve(new DoubleMatrix2D(RESULT_2D.getData())));
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

  protected static class MyLUDecomposition implements LUDecomposition {

    @Override
    public double getDeterminant() {
      return DETERMINANT;
    }

    @Override
    public RealMatrix getL() {
      return L;
    }

    @Override
    public RealMatrix getP() {
      return P;
    }

    @Override
    public int[] getPivot() {
      return PIVOT;
    }

    @Override
    public DecompositionSolver getSolver() {
      return SOLVER;
    }

    @Override
    public RealMatrix getU() {
      return U;
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
