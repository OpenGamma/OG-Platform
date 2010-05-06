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

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.ColtMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class DeltaGammaCovarianceMatrixMeanCalculatorTest {
  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final Function1D<ParametricVaRDataBundle, Double> F = new DeltaGammaCovarianceMatrixMeanCalculator(ALGEBRA);
  private static final DoubleMatrix1D DELTA_VECTOR = new DoubleMatrix1D(new double[] { 1, 5 });
  private static final DoubleMatrix2D GAMMA_MATRIX = new DoubleMatrix2D(new double[][] { new double[] { 25, -7.5 }, new double[] { -7.5, 125 } });
  private static final DoubleMatrix2D COVARIANCE_MATRIX = new DoubleMatrix2D(new double[][] { new double[] { 0.0036, -0.0006 }, new double[] { -0.0006, 0.0016 } });
  private static final Map<Sensitivity<?>, Matrix<?>> SENSITIVITIES = new HashMap<Sensitivity<?>, Matrix<?>>();
  private static final Map<Sensitivity<?>, DoubleMatrix2D> COVARIANCES = new HashMap<Sensitivity<?>, DoubleMatrix2D>();
  private static final Sensitivity<Greek> VALUE_DELTA = new ValueGreek(Greek.DELTA);
  private static final Sensitivity<Greek> VALUE_GAMMA = new ValueGreek(Greek.GAMMA);

  static {
    SENSITIVITIES.put(VALUE_DELTA, DELTA_VECTOR);
    SENSITIVITIES.put(VALUE_GAMMA, GAMMA_MATRIX);
    COVARIANCES.put(VALUE_DELTA, COVARIANCE_MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    new DeltaGammaCovarianceMatrixMeanCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate((ParametricVaRDataBundle) null);
  }

  @Test
  public void test() {
    final Map<Sensitivity<?>, Matrix<?>> m = Collections.<Sensitivity<?>, Matrix<?>> singletonMap(VALUE_DELTA, DELTA_VECTOR);
    assertEquals(F.evaluate(new ParametricVaRDataBundle(m, COVARIANCES)), 0, 1e-15);
    assertEquals(F.evaluate(new ParametricVaRDataBundle(SENSITIVITIES, COVARIANCES)), 0.1495, 1e-4);
  }
}
