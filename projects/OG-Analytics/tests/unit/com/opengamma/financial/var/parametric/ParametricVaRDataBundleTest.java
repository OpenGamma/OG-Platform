/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * 
 */
public class ParametricVaRDataBundleTest {
  private final Map<Integer, Matrix<?>> VECTOR = Collections.<Integer, Matrix<?>>singletonMap(1, new DoubleMatrix1D(new double[] {4}));
  private final Map<Integer, DoubleMatrix2D> MATRIX = Collections.<Integer, DoubleMatrix2D>singletonMap(1, new DoubleMatrix2D(new double[][] {new double[] {2}}));
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
    final Map<Integer, DoubleMatrix2D> covariances = new HashMap<Integer, DoubleMatrix2D>();
    covariances.put(1, new DoubleMatrix2D(new double[][] {new double[] {2}}));
    covariances.put(2, new DoubleMatrix2D(new double[][] {new double[] {2}}));
    new ParametricVaRDataBundle(VECTOR, covariances);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSensitivity() {
    final Map<Integer, Matrix<?>> sensitivities = Collections.<Integer, Matrix<?>>singletonMap(1, null);
    new ParametricVaRDataBundle(sensitivities, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadFirstOrderSensitivity() {
    final Map<Integer, Matrix<?>> sensitivities = Collections.<Integer, Matrix<?>>singletonMap(1, new DoubleMatrix2D(new double[][] {new double[] {2}}));
    new ParametricVaRDataBundle(sensitivities, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonSquareSensitivityMatrix() {
    final Map<Integer, Matrix<?>> sensitivities = Collections.<Integer, Matrix<?>>singletonMap(2, new DoubleMatrix2D(new double[][] {new double[] {2, 1}}));
    new ParametricVaRDataBundle(sensitivities, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test3DMatrix() {
    final Matrix<?> m = new Matrix<Double>() {

      @Override
      public int getNumberOfElements() {
        return 0;
      }

      @Override
      public Double getEntry(int... indices) {
        return 0.;
      }

    };
    final Map<Integer, Matrix<?>> sensitivities = Collections.<Integer, Matrix<?>>singletonMap(1, m);
    new ParametricVaRDataBundle(sensitivities, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCovarianceMatrix() {
    final Map<Integer, Matrix<?>> sensitivities = Collections.<Integer, Matrix<?>>singletonMap(1, null);
    new ParametricVaRDataBundle(sensitivities, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonSquareCovarianceMatrix() {
    final Map<Integer, DoubleMatrix2D> m = Collections.<Integer, DoubleMatrix2D>singletonMap(1, new DoubleMatrix2D(new double[][] {new double[] {1, 2}}));
    new ParametricVaRDataBundle(VECTOR, m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingSensitivityAndCovarianceMatrices() {
    final Map<Integer, DoubleMatrix2D> m = Collections.<Integer, DoubleMatrix2D>singletonMap(1, new DoubleMatrix2D(new double[][] {new double[] {1, 2}, new double[] {3, 4}}));
    new ParametricVaRDataBundle(VECTOR, m);
  }

  @Test
  public void testConversionToMatrix() {
    final Map<Integer, Matrix<?>> sensitivities = Collections.<Integer, Matrix<?>>singletonMap(2, new DoubleMatrix1D(new double[] {2, 1}));
    final ParametricVaRDataBundle data = new ParametricVaRDataBundle(sensitivities, MATRIX);
    final Matrix<?> m = data.getSensitivityData(2);
    assertTrue(m instanceof DoubleMatrix2D);
    final double[][] diagonal = new double[][] {new double[] {2, 0}, new double[] {0, 1}};
    final double[][] converted = ((DoubleMatrix2D) m).getData();
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
    assertEquals(VECTOR.get(1), DATA.getSensitivityData(1));
    assertEquals(MATRIX.get(1), DATA.getCovarianceMatrix(1));
  }

  @Test
  public void testEqualsAndHashCode() {
    final Map<Integer, Matrix<?>> vector = Collections.<Integer, Matrix<?>>singletonMap(1, new DoubleMatrix1D(new double[] {5}));
    final Map<Integer, DoubleMatrix2D> matrix = Collections.<Integer, DoubleMatrix2D>singletonMap(1, new DoubleMatrix2D(new double[][] {new double[] {6}}));
    ParametricVaRDataBundle data1 = new ParametricVaRDataBundle(VECTOR, MATRIX);
    assertEquals(data1, DATA);
    assertEquals(data1.hashCode(), DATA.hashCode());
    data1 = new ParametricVaRDataBundle(vector, MATRIX);
    assertFalse(data1.equals(DATA));
    data1 = new ParametricVaRDataBundle(VECTOR, matrix);
    assertFalse(data1.equals(DATA));
  }
}
