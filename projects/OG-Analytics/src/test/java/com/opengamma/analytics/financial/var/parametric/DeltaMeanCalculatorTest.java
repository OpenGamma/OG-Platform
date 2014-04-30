/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.parametric;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DeltaMeanCalculatorTest {
  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final DeltaMeanCalculator F = new DeltaMeanCalculator(ALGEBRA);
  private static final DoubleMatrix1D EMPTY_VECTOR = new DoubleMatrix1D(new double[0]);
  private static final DoubleMatrix2D EMPTY_MATRIX = new DoubleMatrix2D(new double[0][0]);
  private static final DoubleMatrix1D VECTOR = new DoubleMatrix1D(new double[] {3});
  private static final DoubleMatrix2D MATRIX = new DoubleMatrix2D(new double[][] {new double[] {1}});
  private static final DoubleMatrix1D ZERO = new DoubleMatrix1D(new double[] {0});

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAlgebra() {
    new DeltaMeanCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate((Map<Integer, ParametricVaRDataBundle>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyValueDeltaVector() {
    final ParametricVaRDataBundle data = new ParametricVaRDataBundle(null, null, EMPTY_VECTOR, EMPTY_MATRIX, 1);
    final Map<Integer, ParametricVaRDataBundle> bundle = Collections.singletonMap(1, data);
    F.evaluate(bundle);
  }

  @Test
  public void testEqualsAndHashCode() {
    final DeltaMeanCalculator f1 = new DeltaMeanCalculator(ALGEBRA);
    final DeltaMeanCalculator f2 = new DeltaMeanCalculator(new ColtMatrixAlgebra());
    assertEquals(f1, F);
    assertEquals(f1.hashCode(), F.hashCode());
    assertFalse(f1.equals(f2));
  }

  @Test
  public void test() {
    ParametricVaRDataBundle data = new ParametricVaRDataBundle(null, ZERO, VECTOR, MATRIX, 1);
    Map<Integer, ParametricVaRDataBundle> bundle = Collections.singletonMap(1, data);
    assertEquals(F.evaluate(bundle), 0, 1e-9);
    data = new ParametricVaRDataBundle(null, VECTOR, VECTOR, MATRIX, 1);
    bundle = Collections.singletonMap(1, data);
    assertEquals(F.evaluate(bundle), 9, 1e-9);
  }
}
