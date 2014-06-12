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
public class DeltaCovarianceMatrixStandardDeviationCalculatorTest {
  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final DeltaCovarianceMatrixStandardDeviationCalculator F = new DeltaCovarianceMatrixStandardDeviationCalculator(ALGEBRA);
  private static final DoubleMatrix1D VECTOR = new DoubleMatrix1D(new double[] {3});
  private static final DoubleMatrix2D MATRIX = new DoubleMatrix2D(new double[][] {new double[] {5}});

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAlgebra() {
    new DeltaCovarianceMatrixStandardDeviationCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
