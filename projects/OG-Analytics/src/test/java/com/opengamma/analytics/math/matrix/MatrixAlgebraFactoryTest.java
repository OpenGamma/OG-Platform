/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.matrix;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MatrixAlgebraFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadName() {
    MatrixAlgebraFactory.getMatrixAlgebra("X");
  }

  @Test
  public void testBadClass() {
    assertNull(MatrixAlgebraFactory.getMatrixAlgebraName(new MatrixAlgebra() {

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
      public DoubleMatrix2D getPower(final Matrix<?> m, final double p) {
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

    }));
  }

  @Test
  public void test() {
    assertEquals(MatrixAlgebraFactory.getMatrixAlgebra(MatrixAlgebraFactory.COLT), MatrixAlgebraFactory.COLT_ALGEBRA);
    assertEquals(MatrixAlgebraFactory.getMatrixAlgebra(MatrixAlgebraFactory.COMMONS), MatrixAlgebraFactory.COMMONS_ALGEBRA);
    assertEquals(MatrixAlgebraFactory.getMatrixAlgebra(MatrixAlgebraFactory.OG), MatrixAlgebraFactory.OG_ALGEBRA);
    assertEquals(MatrixAlgebraFactory.getMatrixAlgebraName(MatrixAlgebraFactory.COLT_ALGEBRA), MatrixAlgebraFactory.COLT);
    assertEquals(MatrixAlgebraFactory.getMatrixAlgebraName(MatrixAlgebraFactory.COMMONS_ALGEBRA), MatrixAlgebraFactory.COMMONS);
    assertEquals(MatrixAlgebraFactory.getMatrixAlgebraName(MatrixAlgebraFactory.OG_ALGEBRA), MatrixAlgebraFactory.OG);
  }
}
