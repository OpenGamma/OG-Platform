/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.parametric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * 
 */
public class ParametricWithMeanVaRDataBundleTest {
  private final DoubleMatrix1D MEAN = new DoubleMatrix1D(new double[] {2});
  private final Matrix<?> VECTOR = new DoubleMatrix1D(new double[] {4});
  private final DoubleMatrix2D MATRIX = new DoubleMatrix2D(new double[][] {new double[] {2}});
  private final List<String> NAMES = Arrays.asList("A");
  private final ParametricWithMeanVaRDataBundle DATA = new ParametricWithMeanVaRDataBundle(VECTOR, MATRIX, 1, MEAN);

  @Test(expected = IllegalArgumentException.class)
  public void testNullMeans1() {
    new ParametricWithMeanVaRDataBundle(VECTOR, MATRIX, 1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingMeanAnd1DMatrix1() {
    new ParametricWithMeanVaRDataBundle(VECTOR, MATRIX, 1, new DoubleMatrix1D(new double[] {3, 5}));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingMeanAnd2DMatrix1() {
    final DoubleMatrix1D mean = new DoubleMatrix1D(new double[] {4});
    final Matrix<?> vector = new DoubleMatrix2D(new double[][] {new double[] {3, 4}, new double[] {5, 6}});
    final DoubleMatrix2D matrix = new DoubleMatrix2D(new double[][] {new double[] {7, 8}, new double[] {9, 10}});
    new ParametricWithMeanVaRDataBundle(vector, matrix, 2, mean);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMeans2() {
    new ParametricWithMeanVaRDataBundle(NAMES, VECTOR, MATRIX, 1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingMeanAnd1DMatrix2() {
    new ParametricWithMeanVaRDataBundle(NAMES, VECTOR, MATRIX, 1, new DoubleMatrix1D(new double[] {3, 5}));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonMatchingMeanAnd2DMatrix2() {
    final DoubleMatrix1D mean = new DoubleMatrix1D(new double[] {4});
    final Matrix<?> vector = new DoubleMatrix2D(new double[][] {new double[] {3, 4}, new double[] {5, 6}});
    final DoubleMatrix2D matrix = new DoubleMatrix2D(new double[][] {new double[] {7, 8}, new double[] {9, 10}});
    new ParametricWithMeanVaRDataBundle(NAMES, vector, matrix, 2, mean);
  }

  @Test
  public void testEqualsAndHashCode() {
    final DoubleMatrix1D mean = new DoubleMatrix1D(new double[] {3});
    final Matrix<?> vector = new DoubleMatrix1D(new double[] {5});
    final DoubleMatrix2D matrix = new DoubleMatrix2D(new double[][] {new double[] {1}});
    ParametricWithMeanVaRDataBundle data = new ParametricWithMeanVaRDataBundle(VECTOR, MATRIX, 1, MEAN);
    assertEquals(data, DATA);
    assertEquals(data.hashCode(), DATA.hashCode());
    data = new ParametricWithMeanVaRDataBundle(VECTOR, MATRIX, 1, mean);
    assertFalse(data.equals(DATA));
    data = new ParametricWithMeanVaRDataBundle(vector, MATRIX, 1, MEAN);
    assertFalse(data.equals(DATA));
    data = new ParametricWithMeanVaRDataBundle(VECTOR, MATRIX, 2, MEAN);
    assertFalse(data.equals(DATA));
    data = new ParametricWithMeanVaRDataBundle(VECTOR, matrix, 1, MEAN);
    assertFalse(data.equals(DATA));
    assertEquals(DATA.getMean(), MEAN);
  }

}
