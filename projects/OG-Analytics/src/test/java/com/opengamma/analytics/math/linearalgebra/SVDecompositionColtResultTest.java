/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SVDecompositionColtResultTest {
  static final double CONDITION = 3.9;
  static final double NORM = 2.4;
  static final int RANK = 4;
  static final DoubleMatrix2D S = DoubleFactory2D.dense.make(new double[][] {new double[] {1, 2, 3}, new double[] {4, 5, 6}, new double[] {7, 8, 9}});
  static final DoubleMatrix2D U = DoubleFactory2D.dense.make(new double[][] {new double[] {11, 12, 13}, new double[] {14, 15, 16}, new double[] {17, 18, 19}});
  static final DoubleMatrix2D V = DoubleFactory2D.dense.make(new double[][] {new double[] {21, 22, 23}, new double[] {24, 25, 26}, new double[] {27, 28, 29}});
  static final DoubleMatrix2D U_T = DoubleFactory2D.dense.make(new double[][] {new double[] {11, 14, 17}, new double[] {12, 15, 18}, new double[] {13, 16, 19}});
  static final DoubleMatrix2D V_T = DoubleFactory2D.dense.make(new double[][] {new double[] {21, 24, 27}, new double[] {22, 25, 28}, new double[] {23, 26, 29}});
  static final double[] SINGULAR_VALUES = new double[] {1, 2, 3};
  static final SingularValueDecomposition SVD = new MySingularValueDecomposition();
  private static final SVDecompositionColtResult RESULT = new SVDecompositionColtResult(SVD);
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSVD() {
    new SVDecompositionColtResult(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    RESULT.solve((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVector() {
    RESULT.solve((DoubleMatrix1D) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMatrix() {
    RESULT.solve((com.opengamma.analytics.math.matrix.DoubleMatrix2D) null);
  }

  @Test
  public void testGetters() {
    assertEquals(CONDITION, RESULT.getConditionNumber(), 0);
    assertEquals(RANK, RESULT.getRank());
    assertEquals(NORM, RESULT.getNorm(), 0);
    assertColtMatrixEquals(S, RESULT.getS());
    assertColtMatrixEquals(U, RESULT.getU());
    assertColtMatrixEquals(U_T, RESULT.getUT());
    assertColtMatrixEquals(V, RESULT.getV());
    assertColtMatrixEquals(V_T, RESULT.getVT());
    assertArrayEquals(SINGULAR_VALUES, RESULT.getSingularValues(), 1e-12);
  }

  private void assertColtMatrixEquals(final DoubleMatrix2D m1, final com.opengamma.analytics.math.matrix.DoubleMatrix2D m2) {
    final int m = m1.rows();
    final int n = m1.columns();
    assertEquals(m, m2.getNumberOfRows());
    assertEquals(n, m2.getNumberOfColumns());
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        assertEquals(m1.get(i, j), m2.getEntry(i, j), EPS);
      }
    }
  }

  private static class MySingularValueDecomposition extends SingularValueDecomposition {
    private static final long serialVersionUID = 1L;

    public MySingularValueDecomposition() {
      super(DoubleFactory2D.dense.make(new double[][] {new double[] {1}, new double[] {1}}));
    }

    @Override
    public double cond() {
      return CONDITION;
    }

    @Override
    public double norm2() {
      return NORM;
    }

    @Override
    public int rank() {
      return RANK;
    }

    @Override
    public cern.colt.matrix.DoubleMatrix2D getS() {
      return S;
    }

    @Override
    public cern.colt.matrix.DoubleMatrix2D getU() {
      return U;
    }

    @Override
    public cern.colt.matrix.DoubleMatrix2D getV() {
      return V;
    }

    @Override
    public double[] getSingularValues() {
      return SINGULAR_VALUES;
    }
  }

}
