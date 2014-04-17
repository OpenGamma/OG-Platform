/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.parametric;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.Matrix;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ParametricVaRDataBundleTest {
  private static final List<String> NAMES = Arrays.asList("A", "B", "C");
  private static final DoubleMatrix1D ZERO = new DoubleMatrix1D(new double[] {0, 0, 0});
  private static final DoubleMatrix1D R = new DoubleMatrix1D(new double[] {1, 0, 0});
  private static final DoubleMatrix1D DELTA = new DoubleMatrix1D(new double[] {1, 2, 3});
  private static final DoubleMatrix2D GAMMA = new DoubleMatrix2D(new double[][] {new double[] {1, 0, 0}, new double[] {0, 2, 0}, new double[] {0, 0, 3}});
  private static final DoubleMatrix2D COV = new DoubleMatrix2D(new double[][] {new double[] {0.1, 0.2, 0.3}, new double[] {0.2, 0.4, 0.5}, new double[] {0.3, 0.6, 0.5}});
  private static final int ORDER = 1;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSensitivities1() {
    new ParametricVaRDataBundle(null, COV, ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSensitivities2() {
    new ParametricVaRDataBundle(NAMES, null, COV, ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCovariance1() {
    new ParametricVaRDataBundle(DELTA, null, ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCovariance2() {
    new ParametricVaRDataBundle(NAMES, DELTA, null, ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeOrder1() {
    new ParametricVaRDataBundle(DELTA, COV, -ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeOrder2() {
    new ParametricVaRDataBundle(NAMES, DELTA, COV, -ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongMatrixDimension() {
    final Matrix<double[]> m = new Matrix<double[]>() {

      @Override
      public int getNumberOfElements() {
        return 0;
      }

      @Override
      public double[] getEntry(final int... indices) {
        return ArrayUtils.EMPTY_DOUBLE_ARRAY;
      }

    };
    new ParametricVaRDataBundle(m, COV, ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNonSquareCovarianceMatrix() {
    new ParametricVaRDataBundle(DELTA, new DoubleMatrix2D(new double[][] {new double[] {1, 2, 3}, new double[] {4, 5, 6}}), ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCovarianceMatrixSize1() {
    new ParametricVaRDataBundle(DELTA, new DoubleMatrix2D(new double[][] {new double[] {1}}), 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongCovarianceMatrixSize2() {
    new ParametricVaRDataBundle(GAMMA, new DoubleMatrix2D(new double[][] {new double[] {1}}), 2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNamesSize1() {
    new ParametricVaRDataBundle(Arrays.asList("A"), DELTA, COV, ORDER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongReturnSize1() {
    new ParametricVaRDataBundle(new DoubleMatrix1D(new double[] {1, 2, 3, 4, 5}), DELTA, COV, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongReturnSize2() {
    new ParametricVaRDataBundle(new DoubleMatrix1D(new double[] {1, 2, 3, 4, 5}), GAMMA, COV, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNamesSize2() {
    new ParametricVaRDataBundle(Arrays.asList("A"), GAMMA, COV, ORDER);
  }

  @Test
  public void testHashCodeAndEquals() {
    ParametricVaRDataBundle data = new ParametricVaRDataBundle(NAMES, R, DELTA, COV, ORDER);
    ParametricVaRDataBundle other = new ParametricVaRDataBundle(NAMES, R, DELTA, COV, ORDER);
    assertEquals(data, other);
    assertEquals(data.hashCode(), other.hashCode());
    other = new ParametricVaRDataBundle(Arrays.asList("A", "B", "D"), R, DELTA, COV, ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, DELTA, DELTA, COV, ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, R, R, COV, ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, R, DELTA, new DoubleMatrix2D(new double[][] {new double[] {0, 0, 0}, new double[] {0, 0, 0}, new double[] {0, 0, 0}}), ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, R, DELTA, COV, ORDER + 1);
    assertFalse(data.equals(other));
    data = new ParametricVaRDataBundle(NAMES, R, GAMMA, COV, ORDER);
    other = new ParametricVaRDataBundle(NAMES, R, GAMMA, COV, ORDER);
    assertEquals(data, other);
    assertEquals(data.hashCode(), other.hashCode());
    other = new ParametricVaRDataBundle(Arrays.asList("A", "B", "D"), R, GAMMA, COV, ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, DELTA, GAMMA, COV, ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, R, R, COV, ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, R, GAMMA, new DoubleMatrix2D(new double[][] {new double[] {0, 0, 0}, new double[] {0, 0, 0}, new double[] {0, 0, 0}}), ORDER);
    assertFalse(data.equals(other));
    other = new ParametricVaRDataBundle(NAMES, R, GAMMA, COV, ORDER + 1);
    assertFalse(data.equals(other));
  }

  @Test
  public void testConstructors() {
    final ParametricVaRDataBundle data = new ParametricVaRDataBundle(null, ZERO, DELTA, COV, ORDER);
    ParametricVaRDataBundle other = new ParametricVaRDataBundle(DELTA, COV, ORDER);
    assertEquals(data, other);
    other = new ParametricVaRDataBundle(ZERO, DELTA, COV, ORDER);
    assertEquals(data, other);
    other = new ParametricVaRDataBundle((List<String>) null, DELTA, COV, ORDER);
    assertEquals(data, other);
  }

  @Test
  public void testGetters() {
    ParametricVaRDataBundle data = new ParametricVaRDataBundle(NAMES, R, DELTA, COV, ORDER);
    assertEquals(data.getNames(), NAMES);
    assertEquals(data.getSensitivities(), DELTA);
    assertEquals(data.getCovarianceMatrix(), COV);
    assertEquals(data.getOrder(), ORDER);
    assertEquals(data.getExpectedReturn(), R);
    data = new ParametricVaRDataBundle(DELTA, COV, ORDER);
    assertNull(data.getNames());
    assertEquals(data.getSensitivities(), DELTA);
    assertEquals(data.getCovarianceMatrix(), COV);
    assertEquals(data.getOrder(), ORDER);
    assertEquals(data.getExpectedReturn(), ZERO);
    data = new ParametricVaRDataBundle(R, DELTA, COV, ORDER);
    assertNull(data.getNames());
    assertEquals(data.getSensitivities(), DELTA);
    assertEquals(data.getCovarianceMatrix(), COV);
    assertEquals(data.getOrder(), ORDER);
    assertEquals(data.getExpectedReturn(), R);
    data = new ParametricVaRDataBundle(NAMES, DELTA, COV, ORDER);
    assertEquals(data.getNames(), NAMES);
    assertEquals(data.getSensitivities(), DELTA);
    assertEquals(data.getCovarianceMatrix(), COV);
    assertEquals(data.getOrder(), ORDER);
    assertEquals(data.getExpectedReturn(), ZERO);
  }
}
