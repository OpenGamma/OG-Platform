/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * 
 */
public class ParametricVaRDataBundleTest {
  private static final Sensitivity<Greek> VALUE_DELTA = new ValueGreek(Greek.DELTA);
  private static final Sensitivity<Greek> VALUE_GAMMA = new ValueGreek(Greek.GAMMA);
  private final Map<Sensitivity<?>, Matrix<?>> VECTOR = Collections.<Sensitivity<?>, Matrix<?>> singletonMap(VALUE_DELTA, new DoubleMatrix1D(new double[] { 4 }));
  private final Map<Sensitivity<?>, DoubleMatrix2D> MATRIX = Collections.<Sensitivity<?>, DoubleMatrix2D> singletonMap(VALUE_DELTA, new DoubleMatrix2D(
      new double[][] { new double[] { 2 } }));
  private final ParametricVaRDataBundle DATA = new ParametricVaRDataBundle(VECTOR, MATRIX);

  @Test(expected = IllegalArgumentException.class)
  public void testNullSensitivities() {
    new ParametricVaRDataBundle(null, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCovariances() {
    new ParametricVaRDataBundle(VECTOR, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMoreCovariances() {
    final Map<Sensitivity<?>, DoubleMatrix2D> covariances = new HashMap<Sensitivity<?>, DoubleMatrix2D>();
    covariances.put(VALUE_DELTA, new DoubleMatrix2D(new double[][] { new double[] { 2 } }));
    covariances.put(VALUE_GAMMA, new DoubleMatrix2D(new double[][] { new double[] { 2 } }));
    new ParametricVaRDataBundle(VECTOR, covariances);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSensitivity() {
    final Map<Sensitivity<?>, Matrix<?>> sensitivities = Collections.<Sensitivity<?>, Matrix<?>> singletonMap(VALUE_DELTA, null);
    new ParametricVaRDataBundle(sensitivities, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadFirstOrderSensitivity() {
    final Map<Sensitivity<?>, Matrix<?>> sensitivities = Collections.<Sensitivity<?>, Matrix<?>> singletonMap(VALUE_DELTA,
        new DoubleMatrix2D(new double[][] { new double[] { 2 } }));
    new ParametricVaRDataBundle(sensitivities, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonSquareSensitivityMatrix() {
    final Map<Sensitivity<?>, Matrix<?>> sensitivities = Collections.<Sensitivity<?>, Matrix<?>> singletonMap(VALUE_GAMMA, new DoubleMatrix2D(
        new double[][] { new double[] { 2, 1 } }));
    new ParametricVaRDataBundle(sensitivities, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test3DMatrix() {
    final Matrix<?> m = new Matrix<double[][][]>() {

      @Override
      public int getNumberOfElements() {
        return 0;
      }

    };
    final Map<Sensitivity<?>, Matrix<?>> sensitivities = Collections.<Sensitivity<?>, Matrix<?>> singletonMap(VALUE_DELTA, m);
    new ParametricVaRDataBundle(sensitivities, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCovarianceMatrix() {
    final Map<Sensitivity<?>, Matrix<?>> sensitivities = Collections.<Sensitivity<?>, Matrix<?>> singletonMap(VALUE_DELTA, null);
    new ParametricVaRDataBundle(sensitivities, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonSquareCovarianceMatrix() {
    final Map<Sensitivity<?>, DoubleMatrix2D> m = Collections.<Sensitivity<?>, DoubleMatrix2D> singletonMap(VALUE_DELTA, new DoubleMatrix2D(
        new double[][] { new double[] { 1, 2 } }));
    new ParametricVaRDataBundle(VECTOR, m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingSensitivityAndCovarianceMatrices() {
    final Map<Sensitivity<?>, DoubleMatrix2D> m = Collections.<Sensitivity<?>, DoubleMatrix2D> singletonMap(VALUE_DELTA, new DoubleMatrix2D(new double[][] { new double[] { 1, 2 },
        new double[] { 3, 4 } }));
    new ParametricVaRDataBundle(VECTOR, m);
  }

  @Test
  public void testConversionToMatrix() {
    final Map<Sensitivity<?>, Matrix<?>> sensitivities = Collections.<Sensitivity<?>, Matrix<?>> singletonMap(VALUE_GAMMA, new DoubleMatrix1D(new double[] { 2, 1 }));
    final ParametricVaRDataBundle data = new ParametricVaRDataBundle(sensitivities, MATRIX);
    final Matrix<?> m = data.getSensitivityData(VALUE_GAMMA);
    assertTrue(m instanceof DoubleMatrix2D);
    final double[][] diagonal = new double[][] { new double[] { 2, 0 }, new double[] { 0, 1 } };
    final double[][] converted = ((DoubleMatrix2D) m).getDataAsPrimitiveArray();
    assertTrue(converted.length == 2);
    assertTrue(converted[0].length == 2);
    final double eps = 1e-15;
    assertEquals(diagonal[0][0], converted[0][0], eps);
    assertEquals(diagonal[0][1], converted[0][1], eps);
    assertEquals(diagonal[1][0], converted[1][0], eps);
    assertEquals(diagonal[1][1], converted[1][1], eps);
  }

  @Test
  public void test() {
    assertEquals(VECTOR.get(VALUE_DELTA), DATA.getSensitivityData(VALUE_DELTA));
    assertEquals(MATRIX.get(VALUE_DELTA), DATA.getCovarianceMatrix(VALUE_DELTA));
  }
}
