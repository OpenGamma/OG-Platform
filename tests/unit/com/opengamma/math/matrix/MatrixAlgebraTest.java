/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

/**
 * 
 */
public class MatrixAlgebraTest {
  private static final MatrixAlgebra ALGEBRA = new MyMatrixAlgebra();
  //private static final MatrixAlgebra comAlgebra = new CommonsMatrixAlgebra();
  //private static final MatrixAlgebra coltAlgebra = new ColtMatrixAlgebra();

  private static final MatrixAlgebra comAlgebra = MatrixAlgebraFactory.getMatrixAlgebra("Commons");
  private static final MatrixAlgebra coltAlgebra = MatrixAlgebraFactory.getMatrixAlgebra("Colt");

  private static final Matrix<?> M1 = new DoubleMatrix1D(new double[] { 1, 2 });
  private static final Matrix<?> M2 = new DoubleMatrix1D(new double[] { 3, 4 });
  private static final Matrix<?> M3 = new DoubleMatrix2D(
      new double[][] { new double[] { 1, 2 }, new double[] { 3, 4 } });
  private static final Matrix<?> M4 = new DoubleMatrix2D(
      new double[][] { new double[] { 5, 6 }, new double[] { 7, 8 } });
  private final double EPS = 1e-10;

  @Test(expected = NotImplementedException.class)
  public void testAddWrongSize() {
    ALGEBRA.add(M1, M3);
  }

  @Test
  public void testAdd() {
    final Matrix<?> m5 = ALGEBRA.add(M1, M2);
    assertTrue(m5 instanceof DoubleMatrix1D);
    final double[] sum1 = ((DoubleMatrix1D) m5).getData();
    assertEquals(sum1.length, 2);
    assertEquals(sum1[0], 4, EPS);
    assertEquals(sum1[1], 6, EPS);
    final Matrix<?> m6 = ALGEBRA.add(M3, M4);
    assertTrue(m6 instanceof DoubleMatrix2D);
    final double[][] sum2 = ((DoubleMatrix2D) m6).getData();
    assertEquals(sum2.length, 2);
    assertEquals(sum2[0].length, 2);
    assertEquals(sum2[0][0], 6, EPS);
    assertEquals(sum2[0][1], 8, EPS);
    assertEquals(sum2[1][0], 10, EPS);
    assertEquals(sum2[1][1], 12, EPS);
  }

  @Test(expected = NotImplementedException.class)
  public void testSubtractWrongSize() {
    ALGEBRA.add(M1, M3);
  }

  @Test
  public void testSubtract() {
    final Matrix<?> m5 = ALGEBRA.subtract(M1, M2);
    assertTrue(m5 instanceof DoubleMatrix1D);
    final double[] r1 = ((DoubleMatrix1D) m5).getData();
    assertEquals(r1.length, 2);
    assertEquals(r1[0], -2, EPS);
    assertEquals(r1[1], -2, EPS);
    final Matrix<?> m6 = ALGEBRA.subtract(M3, M4);
    assertTrue(m6 instanceof DoubleMatrix2D);
    final double[][] r2 = ((DoubleMatrix2D) m6).getData();
    assertEquals(r2.length, 2);
    assertEquals(r2[0].length, 2);
    assertEquals(r2[0][0], -4, EPS);
    assertEquals(r2[0][1], -4, EPS);
    assertEquals(r2[1][0], -4, EPS);
    assertEquals(r2[1][1], -4, EPS);
  }

  @Test
  public void testScale() {
    final Matrix<?> m5 = ALGEBRA.scale(M1, 10);
    assertTrue(m5 instanceof DoubleMatrix1D);
    final double[] r1 = ((DoubleMatrix1D) m5).getData();
    assertEquals(r1.length, 2);
    assertEquals(r1[0], 10, EPS);
    assertEquals(r1[1], 20, EPS);
    final Matrix<?> m6 = ALGEBRA.scale(M3, 10);
    assertTrue(m6 instanceof DoubleMatrix2D);
    final double[][] r2 = ((DoubleMatrix2D) m6).getData();
    assertEquals(r2.length, 2);
    assertEquals(r2[0].length, 2);
    assertEquals(r2[0][0], 10, EPS);
    assertEquals(r2[0][1], 20, EPS);
    assertEquals(r2[1][0], 30, EPS);
    assertEquals(r2[1][1], 40, EPS);
  }

  static class MyMatrixAlgebra extends MatrixAlgebra {

    @Override
    public double getCondition(final Matrix<?> m) {
      return 0;
    }

    @Override
    public double getDeterminant(final Matrix<?> m) {
      return 0;
    }

    @Override
    public double getInnerProduct(final Matrix<?> m1, final Matrix<?> m2) {
      return 0;
    }

    @Override
    public DoubleMatrix2D getInverse(final Matrix<?> m) {
      return null;
    }

    @Override
    public double getNorm1(final Matrix<?> m) {
      return 0;
    }

    @Override
    public double getNorm2(final Matrix<?> m) {
      return 0;
    }

    @Override
    public double getNormInfinity(final Matrix<?> m) {
      return 0;
    }

    @Override
    public DoubleMatrix2D getOuterProduct(final Matrix<?> m1, final Matrix<?> m2) {
      return null;
    }

    @Override
    public DoubleMatrix2D getPower(final Matrix<?> m, final int p) {
      return null;
    }

    @Override
    public double getTrace(final Matrix<?> m) {
      return 0;
    }

    @Override
    public DoubleMatrix2D getTranspose(final Matrix<?> m) {
      return null;
    }

    @Override
    public Matrix<?> multiply(final Matrix<?> m1, final Matrix<?> m2) {
      return null;
    }

  }

  @Test
  public void testCondition() {
    assertEquals(comAlgebra.getCondition(M4), coltAlgebra.getCondition(M4), EPS);
  }

  @Test
  public void testDeterminant() {
    assertEquals(comAlgebra.getDeterminant(M4), coltAlgebra.getDeterminant(M4), EPS);
  }

  @Test
  public void testNormL1() {
    assertEquals(comAlgebra.getNorm1(M1), coltAlgebra.getNorm1(M1), EPS);
    assertEquals(comAlgebra.getNorm1(M4), coltAlgebra.getNorm1(M4), EPS);
  }

  @Test
  public void testNormL2() {
    assertEquals(comAlgebra.getNorm2(M1), coltAlgebra.getNorm2(M1), EPS);
    assertEquals(comAlgebra.getNorm2(M4), coltAlgebra.getNorm2(M4), EPS);
  }

  @Test
  public void testNormLInf() {
    assertEquals(comAlgebra.getNormInfinity(M1), coltAlgebra.getNormInfinity(M1), EPS);
    assertEquals(comAlgebra.getNormInfinity(M4), coltAlgebra.getNormInfinity(M4), EPS);
  }

  @Test
  public void testTrace() {
    assertEquals(comAlgebra.getTrace(M4), coltAlgebra.getTrace(M4), EPS);
  }

  @Test
  public void testInnerProduct() {
    assertEquals(comAlgebra.getInnerProduct(M1, M2), coltAlgebra.getInnerProduct(M1, M2), EPS);
  }

  @Test
  public void testInverse() {
    assertTrue(MatrixEquals(comAlgebra.getInverse(M3), coltAlgebra.getInverse(M3), EPS));
  }

  @Test
  public void testMultiply() {
    assertTrue(MatrixEquals((DoubleMatrix2D) comAlgebra.multiply(M3, M4),
        (DoubleMatrix2D) coltAlgebra.multiply(M3, M4), EPS));
  }

  @Test
  public void testOuterProduct() {
    assertTrue(MatrixEquals(comAlgebra.getOuterProduct(M1, M2), coltAlgebra.getOuterProduct(M1, M2), EPS));
  }

  @Test
  public void testPower() {
    //this will pass by definition as the Commons calls colt
    assertTrue(MatrixEquals(comAlgebra.getPower(M3, 3), coltAlgebra.getPower(M3, 3), EPS));
    assertTrue(MatrixEquals(coltAlgebra.getPower(M3, 3), (DoubleMatrix2D) coltAlgebra.multiply(M3, coltAlgebra
        .multiply(M3, M3)), EPS));
  }

  private boolean MatrixEquals(final DoubleMatrix2D m1, final DoubleMatrix2D m2, final double tol) {
    final int rows = m1.getNumberOfRows();
    final int cols = m1.getNumberOfColumns();
    if (m2.getNumberOfRows() != rows || m2.getNumberOfColumns() != cols)
      return false;
    int i, j;
    for (i = 0; i < rows; i++)
      for (j = 0; j < rows; j++)
        if (Math.abs(m1.getEntry(i, j) - m2.getEntry(i, j)) > tol)
          return false;
    return true;
  }
}
