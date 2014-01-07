/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.parametric;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.HashMap;
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
public class DeltaGammaCovarianceMatrixMeanCalculatorTest {
  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final DeltaGammaCovarianceMatrixMeanCalculator F = new DeltaGammaCovarianceMatrixMeanCalculator(ALGEBRA);
  private static final DoubleMatrix1D DELTA_VECTOR = new DoubleMatrix1D(new double[] {1, 5});
  private static final DoubleMatrix2D GAMMA_MATRIX = new DoubleMatrix2D(new double[][] {new double[] {25, -7.5}, new double[] {-7.5, 125}});
  private static final DoubleMatrix2D COVARIANCE_MATRIX = new DoubleMatrix2D(new double[][] {new double[] {0.0036, -0.0006}, new double[] {-0.0006, 0.0016}});

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    new DeltaGammaCovarianceMatrixMeanCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate((Map<Integer, ParametricVaRDataBundle>) null);
  }

  @Test
  public void testEqualsAndHashCode() {
    final DeltaGammaCovarianceMatrixMeanCalculator f1 = new DeltaGammaCovarianceMatrixMeanCalculator(ALGEBRA);
    final DeltaGammaCovarianceMatrixMeanCalculator f2 = new DeltaGammaCovarianceMatrixMeanCalculator(new ColtMatrixAlgebra());
    assertEquals(f1, F);
    assertEquals(f1.hashCode(), F.hashCode());
    assertFalse(f1.equals(f2));
  }

  @Test
  public void test() {
    final ParametricVaRDataBundle deltaData = new ParametricVaRDataBundle(DELTA_VECTOR, COVARIANCE_MATRIX, 1);
    final ParametricVaRDataBundle gammaData = new ParametricVaRDataBundle(GAMMA_MATRIX, COVARIANCE_MATRIX, 2);
    final Map<Integer, ParametricVaRDataBundle> m = new HashMap<>();
    m.put(1, deltaData);
    assertEquals(F.evaluate(m), 0, 0);
    m.put(2, new ParametricVaRDataBundle(new DoubleMatrix2D(new double[0][0]), new DoubleMatrix2D(new double[0][0]), 2));
    assertEquals(F.evaluate(m), 0, 0);
    m.put(2, gammaData);
    assertEquals(F.evaluate(m), 0.1495, 1e-4);
  }
}
