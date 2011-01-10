/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.matrix.ColtMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaCovarianceMatrixStandardDeviationCalculatorTest {
  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final DeltaCovarianceMatrixStandardDeviationCalculator F = new DeltaCovarianceMatrixStandardDeviationCalculator(ALGEBRA);
  private static final DoubleMatrix1D VECTOR = new DoubleMatrix1D(new double[] {3});
  private static final DoubleMatrix2D MATRIX = new DoubleMatrix2D(new double[][] {new double[] {5}});

  @Test(expected = IllegalArgumentException.class)
  public void testNullAlgebra() {
    new DeltaCovarianceMatrixStandardDeviationCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate((Map<Integer, ParametricVaRDataBundle>) null);
  }

  @Test
  public void testEqualsAndHashCode() {
    final DeltaCovarianceMatrixStandardDeviationCalculator f1 = new DeltaCovarianceMatrixStandardDeviationCalculator(ALGEBRA);
    final DeltaCovarianceMatrixStandardDeviationCalculator f2 = new DeltaCovarianceMatrixStandardDeviationCalculator(new ColtMatrixAlgebra());
    assertEquals(f1, F);
    assertEquals(f1.hashCode(), F.hashCode());
    assertFalse(f1.equals(f2));
  }

  @Test
  public void test() {
    final ParametricVaRDataBundle data = new ParametricVaRDataBundle(VECTOR, MATRIX, 1);
    final Map<Integer, ParametricVaRDataBundle> m = Collections.<Integer, ParametricVaRDataBundle> singletonMap(1, data);
    assertEquals(F.evaluate(m), Math.sqrt(45), 1e-9);
  }
}
