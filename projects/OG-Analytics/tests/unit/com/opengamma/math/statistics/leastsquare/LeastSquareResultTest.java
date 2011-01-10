/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class LeastSquareResultTest {
  private static final DoubleMatrix1D PARAMS = new DoubleMatrix1D(new double[] {1.0, 2.0});
  private static final DoubleMatrix2D COVAR;

  static {
    COVAR = new DoubleMatrix2D(new double[][] { {0.1, 0.2}, {0.2, 0.3}});

  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeChiSq() {
    new LeastSquareResults(-1, PARAMS, COVAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullParams() {
    new LeastSquareResults(1, null, COVAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCovar() {
    new LeastSquareResults(1, PARAMS, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullWrongSize() {
    new LeastSquareResults(1, new DoubleMatrix1D(new double[] {1.2}), COVAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotSquare() {
    new LeastSquareResults(1, PARAMS, new DoubleMatrix2D(new double[][] {{0.2, 0.3}}));
  }

  @Test
  public void testRecall() {
    double chiSq = 12.46;
    LeastSquareResults res = new LeastSquareResults(chiSq, PARAMS, COVAR);
    assertEquals(chiSq, res.getChiSq(), 0.0);
    for (int i = 0; i < 2; i++) {
      assertEquals(PARAMS.getEntry(i), res.getParameters().getEntry(i), 0);
      for (int j = 0; j < 2; j++) {
        assertEquals(COVAR.getEntry(i, j), res.getCovariance().getEntry(i, j), 0);
      }
    }
  }

  @Test
  public void testHashcode() {
    LeastSquareResults ls1 = new LeastSquareResults(1.0, PARAMS, COVAR);
    LeastSquareResults ls2 = new LeastSquareResults(1.0, new DoubleMatrix1D(new double[] {1.0, 2.0}),
        new DoubleMatrix2D(new double[][] { {0.1, 0.2}, {0.2, 0.3}}));
    assertEquals(ls1.hashCode(), ls2.hashCode(), 0);
  }

  @Test
  public void testEquals() {
    LeastSquareResults ls1 = new LeastSquareResults(1.0, PARAMS, COVAR);
    LeastSquareResults ls2 = new LeastSquareResults(1.0, new DoubleMatrix1D(new double[] {1.0, 2.0}),
        new DoubleMatrix2D(new double[][] { {0.1, 0.2}, {0.2, 0.3}}));
    assertEquals(ls1, ls2);
    ls2 = new LeastSquareResults(1.1, PARAMS, COVAR);
    assertFalse(ls1.equals(ls2));
    ls2 = new LeastSquareResults(1.0, new DoubleMatrix1D(new double[] {1.1, 2.0}), new DoubleMatrix2D(new double[][] {
        {0.1, 0.2}, {0.2, 0.3}}));
    assertFalse(ls1.equals(ls2));
    ls2 = new LeastSquareResults(1.0, new DoubleMatrix1D(new double[] {1.0, 2.0}), new DoubleMatrix2D(new double[][] {
        {0.1, 0.2}, {0.2, 0.4}}));
    assertFalse(ls1.equals(ls2));
  }

}
