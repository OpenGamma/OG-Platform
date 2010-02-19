/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.ColtMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * @author emcleod
 * 
 */
public class DeltaGammaCovarianceMatrixSkewnessCalculatorTest {
  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final Function1D<ParametricVaRDataBundle, Double> F = new DeltaGammaCovarianceMatrixSkewnessCalculator(ALGEBRA);
  private static final DoubleMatrix1D DELTA_VECTOR = new DoubleMatrix1D(new double[] { 1, 5 });
  private static final DoubleMatrix2D GAMMA_MATRIX = new DoubleMatrix2D(new double[][] { new double[] { 25, -7.5 }, new double[] { -7.5, 125 } });
  private static final DoubleMatrix2D COVARIANCE_MATRIX = new DoubleMatrix2D(new double[][] { new double[] { 0.0036, -0.0006 }, new double[] { -0.0006, 0.0016 } });
  private static final Map<Sensitivity, Matrix<?>> SENSITIVITIES = new HashMap<Sensitivity, Matrix<?>>();
  private static final Map<Sensitivity, DoubleMatrix2D> COVARIANCES = new HashMap<Sensitivity, DoubleMatrix2D>();

  static {
    SENSITIVITIES.put(Sensitivity.VALUE_DELTA, DELTA_VECTOR);
    SENSITIVITIES.put(Sensitivity.VALUE_GAMMA, GAMMA_MATRIX);
    COVARIANCES.put(Sensitivity.VALUE_DELTA, COVARIANCE_MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullAlgebra() {
    new DeltaGammaCovarianceMatrixSkewnessCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate((ParametricVaRDataBundle) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGammaMatrixSize() {
    final Map<Sensitivity, Matrix<?>> m = new HashMap<Sensitivity, Matrix<?>>();
    m.put(Sensitivity.VALUE_DELTA, DELTA_VECTOR);
    m.put(Sensitivity.VALUE_GAMMA, new DoubleMatrix2D(new double[][] { new double[] { 1, 2, 3 }, new double[] { 4, 5, 6 }, new double[] { 7, 8, 9 } }));
    F.evaluate(new ParametricVaRDataBundle(m, COVARIANCES));
  }

  @Test
  public void testNoGamma() {
    final Map<Sensitivity, Matrix<?>> m = Collections.<Sensitivity, Matrix<?>> singletonMap(Sensitivity.VALUE_DELTA, DELTA_VECTOR);
    final ParametricVaRDataBundle data = new ParametricVaRDataBundle(m, COVARIANCES);
    assertEquals(F.evaluate(data), 0, 1e-15);
  }

  @Test
  public void test() {
    assertEquals(F.evaluate(new ParametricVaRDataBundle(SENSITIVITIES, COVARIANCES)), 1.913, 1e-3);
  }
}
