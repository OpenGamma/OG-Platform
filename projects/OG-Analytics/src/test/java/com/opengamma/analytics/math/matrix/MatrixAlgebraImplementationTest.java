/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.matrix;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MatrixAlgebraImplementationTest {
  private static final MatrixAlgebra COMMONS = MatrixAlgebraFactory.COMMONS_ALGEBRA;
  private static final MatrixAlgebra COLT = MatrixAlgebraFactory.COLT_ALGEBRA;
  private static final MatrixAlgebra OG = MatrixAlgebraFactory.OG_ALGEBRA;
  private static final DoubleMatrix1D M1 = new DoubleMatrix1D(new double[] {1, 2});
  private static final DoubleMatrix1D M2 = new DoubleMatrix1D(new double[] {3, 4});
  private static final DoubleMatrix2D M3 = new DoubleMatrix2D(new double[][] {new double[] {1, 2}, new double[] {2, 1}});
  private static final DoubleMatrix2D M4 = new DoubleMatrix2D(new double[][] {new double[] {5, 6}, new double[] {7, 8}});
  private static final Matrix<?> M5 = new Matrix<Double>() {

    @Override
    public Double getEntry(final int... indices) {
      return null;
    }

    @Override
    public int getNumberOfElements() {
      return 0;
    }

  };
  private static final double EPS = 1e-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsCondition() {
    COMMONS.getCondition(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtCondition() {
    COLT.getCondition(M1);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testOGCondition() {
    OG.getCondition(M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsDeterminant() {
    COMMONS.getCondition(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtDeterminant() {
    COLT.getCondition(M1);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testOGDeterminant() {
    OG.getDeterminant(M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsInnerProduct() {
    COMMONS.getInnerProduct(M1, M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtInnerProduct() {
    COLT.getInnerProduct(M1, M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGInnerProduct1() {
    OG.getInnerProduct(M1, new DoubleMatrix1D(new double[] {1, 2, 3}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGInnerProduct2() {
    OG.getInnerProduct(M1, M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsInverse() {
    COMMONS.getInverse(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtInverse() {
    COLT.getInverse(M1);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testOGInverse() {
    OG.getInverse(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsNorm1() {
    COMMONS.getNorm1(M5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtNorm1() {
    COLT.getNorm1(M5);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testOGNorm1() {
    OG.getNorm1(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsNorm2() {
    COMMONS.getNorm2(M5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtNorm2() {
    COLT.getNorm2(M5);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testOGNorm2_1() {
    OG.getNorm2(M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGNorm2_2() {
    OG.getNorm2(M5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsNormInfinity() {
    COMMONS.getNormInfinity(M5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtNormInfinity() {
    COLT.getNormInfinity(M5);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testOGNormInfinity() {
    OG.getNormInfinity(M5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsOuterProduct() {
    COMMONS.getOuterProduct(M3, M4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtOuterProduct() {
    COLT.getOuterProduct(M3, M4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGOuterProduct() {
    OG.getOuterProduct(M3, M4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsPower() {
    COMMONS.getPower(M1, 2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtPower() {
    COLT.getPower(M2, 2);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testOGPower1() {
    OG.getPower(M2, 2);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testOGPower2() {
    OG.getPower(M2, 2.3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsTrace() {
    COMMONS.getTrace(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtTrace() {
    COLT.getTrace(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGTrace1() {
    OG.getTrace(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGTrace2() {
    OG.getTrace(new DoubleMatrix2D(new double[][] {new double[] {1, 2, 3}, new double[] {4, 5, 6}}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsTranspose() {
    COMMONS.getTranspose(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtTranspose() {
    COLT.getTranspose(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGTranspose() {
    OG.getTranspose(M1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsMultiply1() {
    COMMONS.multiply(M1, M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsMultiply2() {
    COMMONS.multiply(M3, M5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtMultiply2() {
    COLT.multiply(M3, M5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCommonsMultiply3() {
    COMMONS.multiply(M5, M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testColtMultiply3() {
    COLT.multiply(M5, M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGMultiply1() {
    OG.multiply(M5, M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGMultiply2() {
    OG.multiply(new DoubleMatrix1D(new double[] {1, 2, 3}), M3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGMultiply3() {
    OG.multiply(M3, new DoubleMatrix1D(new double[] {1, 2, 3}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOGMultiply4() {
    OG.multiply(new DoubleMatrix2D(new double[][] {new double[] {1, 2, 3}, new double[] {4, 5, 6}}), M3);
  }

  @Test
  public void testCondition() {
    assertEquals(COMMONS.getCondition(M4), COLT.getCondition(M4), EPS);
  }

  @Test
  public void testDeterminant() {
    assertEquals(COMMONS.getDeterminant(M4), COLT.getDeterminant(M4), EPS);
  }

  @Test
  public void testNormL1() {
    assertEquals(COMMONS.getNorm1(M1), COLT.getNorm1(M1), EPS);
    assertEquals(COMMONS.getNorm1(M4), COLT.getNorm1(M4), EPS);
  }

  @Test
  public void testNormL2() {
    assertEquals(COMMONS.getNorm2(M1), COLT.getNorm2(M1), EPS);
    assertEquals(COMMONS.getNorm2(M4), COLT.getNorm2(M4), EPS);
  }

  @Test
  public void testNormLInf() {
    assertEquals(COMMONS.getNormInfinity(M1), COLT.getNormInfinity(M1), EPS);
    assertEquals(COMMONS.getNormInfinity(M4), COLT.getNormInfinity(M4), EPS);
  }

  @Test
  public void testTrace() {
    assertEquals(COMMONS.getTrace(M4), COLT.getTrace(M4), EPS);
  }

  @Test
  public void testInnerProduct() {
    assertEquals(COMMONS.getInnerProduct(M1, M2), COLT.getInnerProduct(M1, M2), EPS);
  }

  @Test
  public void testInverse() {
    assertMatrixEquals(COMMONS.getInverse(M3), COLT.getInverse(M3));
  }

  @Test
  public void testMultiply() {
    assertMatrixEquals(COMMONS.multiply(DoubleMatrixUtils.getIdentityMatrix2D(2), M3), M3);
    assertMatrixEquals(COMMONS.multiply(M3, M4), COLT.multiply(M3, M4));
  }

  @Test
  public void testOuterProduct() {
    assertMatrixEquals(COMMONS.getOuterProduct(M1, M2), COLT.getOuterProduct(M1, M2));
  }

  @Test
  public void testPower() {
    assertMatrixEquals(COMMONS.getPower(M3, 3), COLT.getPower(M3, 3));
    assertMatrixEquals(COLT.getPower(M3, 3), COLT.multiply(M3, COLT.multiply(M3, M3)));
  }

  private void assertMatrixEquals(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D) {
      assertTrue(m2 instanceof DoubleMatrix1D);
      final DoubleMatrix1D m3 = (DoubleMatrix1D) m1;
      final DoubleMatrix1D m4 = (DoubleMatrix1D) m2;
      assertEquals(m3.getNumberOfElements(), m4.getNumberOfElements());
      for (int i = 0; i < m3.getNumberOfElements(); i++) {
        assertEquals(m3.getEntry(i), m4.getEntry(i), EPS);
      }
      return;
    }
    if (m2 instanceof DoubleMatrix2D) {
      final DoubleMatrix2D m3 = (DoubleMatrix2D) m1;
      final DoubleMatrix2D m4 = (DoubleMatrix2D) m2;
      assertEquals(m3.getNumberOfElements(), m4.getNumberOfElements());
      assertEquals(m3.getNumberOfRows(), m4.getNumberOfRows());
      assertEquals(m3.getNumberOfColumns(), m4.getNumberOfColumns());
      for (int i = 0; i < m3.getNumberOfRows(); i++) {
        for (int j = 0; j < m3.getNumberOfColumns(); j++) {
          assertEquals(m3.getEntry(i, j), m4.getEntry(i, j), EPS);
        }
      }
    }
  }
}
