/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.ColtMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaGammaCovarianceMatrixFisherKurtosisCalculatorTest {
  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final Function1D<ParametricVaRDataBundle, Double> F = new DeltaGammaCovarianceMatrixFisherKurtosisCalculator(ALGEBRA);
  private static final DoubleMatrix1D DELTA_VECTOR = new DoubleMatrix1D(new double[] {1, 5});
  private static final DoubleMatrix2D GAMMA_MATRIX = new DoubleMatrix2D(new double[][] {new double[] {25, -7.5}, new double[] {-7.5, 125}});
  private static final DoubleMatrix2D COVARIANCE_MATRIX = new DoubleMatrix2D(new double[][] {new double[] {0.0036, -0.0006}, new double[] {-0.0006, 0.0016}});
  private static final Map<Integer, Matrix<?>> SENSITIVITIES = new HashMap<Integer, Matrix<?>>();
  private static final Map<Integer, DoubleMatrix2D> COVARIANCES = new HashMap<Integer, DoubleMatrix2D>();

  static {
    SENSITIVITIES.put(1, DELTA_VECTOR);
    SENSITIVITIES.put(2, GAMMA_MATRIX);
    COVARIANCES.put(1, COVARIANCE_MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAlgebra() {
    new DeltaGammaCovarianceMatrixFisherKurtosisCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate((ParametricVaRDataBundle) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGammaMatrixSize() {
    final Map<Integer, Matrix<?>> m = new HashMap<Integer, Matrix<?>>();
    m.put(1, DELTA_VECTOR);
    m.put(2, new DoubleMatrix2D(new double[][] {new double[] {1, 2, 3}, new double[] {4, 5, 6}, new double[] {7, 8, 9}}));
    F.evaluate(new ParametricVaRDataBundle(m, COVARIANCES));
  }

  @Test
  public void testEqualsAndHashCode() {
    Function1D<ParametricVaRDataBundle, Double> f1 = new DeltaGammaCovarianceMatrixFisherKurtosisCalculator(ALGEBRA);
    Function1D<ParametricVaRDataBundle, Double> f2 = new DeltaGammaCovarianceMatrixFisherKurtosisCalculator(new ColtMatrixAlgebra());
    assertEquals(F, f1);
    assertEquals(F.hashCode(), f1.hashCode());
    assertFalse(f1.equals(f2));
  }

  @Test
  public void testNoGamma() {
    final Map<Integer, Matrix<?>> m = Collections.<Integer, Matrix<?>>singletonMap(1, DELTA_VECTOR);
    final ParametricVaRDataBundle data = new ParametricVaRDataBundle(m, COVARIANCES);
    assertEquals(F.evaluate(data), 0, 1e-15);
  }

  @Test
  public void test() {
    assertEquals(F.evaluate(new ParametricVaRDataBundle(SENSITIVITIES, COVARIANCES)), 47.153, 1e-3);
  }
}
