/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SVDecompositionCommonsResultTest {
  static final double CONDITION = 0.5;
  static final double NORM = 0.2;
  static final int RANK = 6;
  static final RealMatrix S = new Array2DRowRealMatrix(new double[][] {new double[] {0.1, 0.2}, new double[] {0.3, 0.4}});
  static final RealMatrix U = new Array2DRowRealMatrix(new double[][] {new double[] {1.1, 1.2}, new double[] {1.3, 1.4}});
  static final RealMatrix UT = new Array2DRowRealMatrix(new double[][] {new double[] {1.1, 1.3}, new double[] {1.2, 1.4}});
  static final RealMatrix V = new Array2DRowRealMatrix(new double[][] {new double[] {2.1, 2.2}, new double[] {2.3, 2.4}});
  static final RealMatrix VT = new Array2DRowRealMatrix(new double[][] {new double[] {2.1, 2.3}, new double[] {2.2, 2.4}});
  static final RealMatrix M = new Array2DRowRealMatrix(new double[][] {new double[] {3.1, 3.2}, new double[] {3.3, 3.4}});
  static final double[] SINGULAR_VALUES = new double[] {6, 7};
  static final RealMatrix RESULT_2D = new Array2DRowRealMatrix(new double[][] {new double[] {3.5, 4.5}, new double[] {5.5, 6.5}});
  static final RealVector RESULT_1D = new ArrayRealVector(new double[] {7.5, 8.5});
  static final DecompositionSolver SOLVER = new MyDecompositionSolver();
  private static final SVDecompositionResult SVD = new SVDecompositionCommonsResult(new MySingularValueDecomposition());
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSVD() {
    new SVDecompositionCommonsResult(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    SVD.solve((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVector() {
    SVD.solve((DoubleMatrix1D) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMatrix() {
    SVD.solve((DoubleMatrix2D) null);
  }

  @Test
  public void testGetters() {
    assertEquals(CONDITION, SVD.getConditionNumber(), 0);
    assertEquals(RANK, SVD.getRank());
    assertEquals(NORM, SVD.getNorm(), 0);
    assertRealMatrixEquals(S, SVD.getS());
    assertRealMatrixEquals(U, SVD.getU());
    assertRealMatrixEquals(UT, SVD.getUT());
    assertRealMatrixEquals(V, SVD.getV());
    assertRealMatrixEquals(VT, SVD.getVT());
    assertArrayEquals(SINGULAR_VALUES, SVD.getSingularValues(), 1e-12);
  }

  @Test
  public void testSolvers() {
    assertTrue(Arrays.equals(RESULT_1D.getData(), SVD.solve(new double[] {0.1, 0.2})));
    assertRealVectorEquals(RESULT_1D, SVD.solve(new DoubleMatrix1D(new double[] {0.1, 0.2})));
    assertRealMatrixEquals(RESULT_2D, SVD.solve(new DoubleMatrix2D(new double[][] {new double[] {0.1, 0.2}, new double[] {0.1, 0.2}})));
  }

  private void assertRealVectorEquals(final RealVector v1, final DoubleMatrix1D v2) {
    final int n = v1.getDimension();
    assertEquals(n, v2.getNumberOfElements());
    for (int i = 0; i < n; i++) {
      assertEquals(v1.getEntry(i), v2.getEntry(i), EPS);
    }
  }

  private void assertRealMatrixEquals(final RealMatrix m1, final DoubleMatrix2D m2) {
    final int m = m1.getRowDimension();
    final int n = m1.getColumnDimension();
    assertEquals(m, m2.getNumberOfRows());
    assertEquals(n, m2.getNumberOfColumns());
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        assertEquals(m1.getEntry(i, j), m2.getEntry(i, j), EPS);
      }
    }
  }

  protected static class MySingularValueDecomposition implements SingularValueDecomposition {

    @Override
    public double getConditionNumber() {
      return CONDITION;
    }

    @Override
    public RealMatrix getCovariance(final double minSingularValue) throws IllegalArgumentException {
      return null;
    }

    @Override
    public double getNorm() {
      return NORM;
    }

    @Override
    public int getRank() {
      return RANK;
    }

    @Override
    public RealMatrix getS() {
      return S;
    }

    @Override
    public double[] getSingularValues() {
      return SINGULAR_VALUES;
    }

    @Override
    public DecompositionSolver getSolver() {
      return SOLVER;
    }

    @Override
    public RealMatrix getU() {
      return U;
    }

    @Override
    public RealMatrix getUT() {
      return UT;
    }

    @Override
    public RealMatrix getV() {
      return V;
    }

    @Override
    public RealMatrix getVT() {
      return VT;
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
