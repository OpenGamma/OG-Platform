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

import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.var.parametric.ParametricWithMeanVaRDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * @author emcleod
 * 
 */
public class ParametricWithMeanVaRDataBundleTest {
  private final Map<Sensitivity, DoubleMatrix1D> MEAN = Collections.<Sensitivity, DoubleMatrix1D> singletonMap(Sensitivity.VALUE_DELTA, new DoubleMatrix1D(new double[] { 2 }));
  private final Map<Sensitivity, Matrix<?>> VECTOR = Collections.<Sensitivity, Matrix<?>> singletonMap(Sensitivity.VALUE_DELTA, new DoubleMatrix1D(new double[] { 4 }));
  private final Map<Sensitivity, DoubleMatrix2D> MATRIX = Collections.<Sensitivity, DoubleMatrix2D> singletonMap(Sensitivity.VALUE_DELTA, new DoubleMatrix2D(
      new double[][] { new double[] { 2 } }));
  private final ParametricWithMeanVaRDataBundle DATA = new ParametricWithMeanVaRDataBundle(MEAN, VECTOR, MATRIX);

  @Test(expected = IllegalArgumentException.class)
  public void testNullMeans() {
    new ParametricWithMeanVaRDataBundle(null, VECTOR, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingMeanAnd1DMatrix() {
    final Map<Sensitivity, DoubleMatrix1D> mean = Collections.<Sensitivity, DoubleMatrix1D> singletonMap(Sensitivity.VALUE_DELTA, new DoubleMatrix1D(new double[] { 3, 5 }));
    new ParametricWithMeanVaRDataBundle(mean, VECTOR, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingMeanAnd2DMatrix() {
    final Map<Sensitivity, DoubleMatrix1D> mean = Collections.<Sensitivity, DoubleMatrix1D> singletonMap(Sensitivity.VALUE_GAMMA, new DoubleMatrix1D(new double[] { 4 }));
    final Map<Sensitivity, Matrix<?>> vector = Collections.<Sensitivity, Matrix<?>> singletonMap(Sensitivity.VALUE_GAMMA, new DoubleMatrix2D(new double[][] {
        new double[] { 3, 4 }, new double[] { 5, 6 } }));
    final Map<Sensitivity, DoubleMatrix2D> matrix = Collections.<Sensitivity, DoubleMatrix2D> singletonMap(Sensitivity.VALUE_GAMMA, new DoubleMatrix2D(new double[][] {
        new double[] { 7, 8 }, new double[] { 9, 10 } }));
    new ParametricWithMeanVaRDataBundle(mean, vector, matrix);
  }

  @Test
  public void test() {
    assertEquals(MEAN.get(Sensitivity.VALUE_DELTA), DATA.getMean(Sensitivity.VALUE_DELTA));
  }
}
