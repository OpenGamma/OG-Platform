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
public class ParametricVaRDataBundleTest {
  private static final List<String> NAMES = Arrays.asList("A", "B", "C");
  private static final DoubleMatrix1D DELTA = new DoubleMatrix1D(new double[] {1, 2, 3});
  private static final DoubleMatrix2D GAMMA = new DoubleMatrix2D(new double[][] {new double[] {1, 0, 0}, new double[] {0, 2, 0}, new double[] {0, 0, 3}});
  private static final DoubleMatrix2D COV = new DoubleMatrix2D(new double[][] {new double[] {0.1, 0.2, 0.3}, new double[] {0.2, 0.4, 0.5}, new double[] {0.3, 0.6, 0.5}});
  private static final int ORDER = 1;

  @Test(expected = IllegalArgumentException.class)
  public void testNullSensitivities1() {
    new ParametricVaRDataBundle(null, COV, ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSensitivities2() {
    new ParametricVaRDataBundle(NAMES, null, COV, ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCovariance1() {
    new ParametricVaRDataBundle(DELTA, null, ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCovariance2() {
    new ParametricVaRDataBundle(NAMES, DELTA, null, ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeOrder1() {
    new ParametricVaRDataBundle(DELTA, COV, -ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeOrder2() {
    new ParametricVaRDataBundle(NAMES, DELTA, COV, -ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongMatrixDimension() {
    final Matrix<double[]> m = new Matrix<double[]>() {

      @Override
      public int getNumberOfElements() {
        return 0;
      }

      @Override
      public double[] getEntry(final int... indices) {
        return null;
      }

    };
    new ParametricVaRDataBundle(m, COV, ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonSquareCovarianceMatrix() {
    new ParametricVaRDataBundle(DELTA, new DoubleMatrix2D(new double[][] {new double[] {1, 2, 3}, new double[] {4, 5, 6}}), ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongCovarianceMatrixSize1() {
    new ParametricVaRDataBundle(DELTA, new DoubleMatrix2D(new double[][] {new double[] {1}}), 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongCovarianceMatrixSize2() {
    new ParametricVaRDataBundle(GAMMA, new DoubleMatrix2D(new double[][] {new double[] {1}}), 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongNamesSize1() {
    new ParametricVaRDataBundle(Arrays.asList("A"), DELTA, COV, ORDER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongNamesSize2() {
    new ParametricVaRDataBundle(Arrays.asList("A"), GAMMA, COV, ORDER);
  }

  @Test
  public void test() {
    ParametricVaRDataBundle data = new ParametricVaRDataBundle(DELTA, COV, ORDER);
    ParametricVaRDataBundle other = new ParametricVaRDataBundle(null, DELTA, COV, ORDER);
    assertEquals(data, other);
    assertEquals(data.hashCode(), other.hashCode());
    assertEquals(data.getNames(), null);
    data = new ParametricVaRDataBundle(NAMES, DELTA, COV, ORDER);
    other = new ParametricVaRDataBundle(NAMES, DELTA, COV, ORDER);
    assertEquals(data, other);
    assertEquals(data.hashCode(), other.hashCode());
    other = new ParametricVaRDataBundle(Arrays.asList("A", "B", "D"), DELTA, COV, ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, new DoubleMatrix1D(new double[] {5, 6, 7}), COV, ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, DELTA, new DoubleMatrix2D(new double[][] {new double[] {0, 0, 0}, new double[] {0, 0, 0}, new double[] {0, 0, 0}}), ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, DELTA, COV, ORDER + 1);
    assertFalse(data.equals(other));
    assertEquals(data.getNames(), NAMES);
    assertEquals(data.getSensitivities(), DELTA);
    assertEquals(data.getCovarianceMatrix(), COV);
    assertEquals(data.getOrder(), ORDER);
  }
}
