/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * 
 */
public class ParametricWithMeanVaRDataBundleTest {
  private final Map<Integer, DoubleMatrix1D> MEAN = Collections.<Integer, DoubleMatrix1D> singletonMap(1, new DoubleMatrix1D(new double[] { 2 }));
  private final Map<Integer, Matrix<?>> VECTOR = Collections.<Integer, Matrix<?>> singletonMap(1, new DoubleMatrix1D(new double[] { 4 }));
  private final Map<Integer, DoubleMatrix2D> MATRIX = Collections.<Integer, DoubleMatrix2D> singletonMap(1, new DoubleMatrix2D(new double[][] { new double[] { 2 } }));
  private final ParametricWithMeanVaRDataBundle DATA = new ParametricWithMeanVaRDataBundle(MEAN, VECTOR, MATRIX);

  @Test(expected = IllegalArgumentException.class)
  public void testNullMeans() {
    new ParametricWithMeanVaRDataBundle(null, VECTOR, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingMeanAnd1DMatrix() {
    final Map<Integer, DoubleMatrix1D> mean = Collections.<Integer, DoubleMatrix1D> singletonMap(1, new DoubleMatrix1D(new double[] { 3, 5 }));
    new ParametricWithMeanVaRDataBundle(mean, VECTOR, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingMeanAnd2DMatrix() {
    final Map<Integer, DoubleMatrix1D> mean = Collections.<Integer, DoubleMatrix1D> singletonMap(2, new DoubleMatrix1D(new double[] { 4 }));
    final Map<Integer, Matrix<?>> vector = Collections.<Integer, Matrix<?>> singletonMap(2, new DoubleMatrix2D(new double[][] { new double[] { 3, 4 }, new double[] { 5, 6 } }));
    final Map<Integer, DoubleMatrix2D> matrix = Collections.<Integer, DoubleMatrix2D> singletonMap(2, new DoubleMatrix2D(new double[][] { new double[] { 7, 8 },
        new double[] { 9, 10 } }));
    new ParametricWithMeanVaRDataBundle(mean, vector, matrix);
  }

  @Test
  public void test() {
    assertEquals(MEAN.get(1), DATA.getMean(1));
  }
}
