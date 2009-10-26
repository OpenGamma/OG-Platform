/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class LeastSquaresRegressionTest {

  @Test
  public void test() {
    final LeastSquaresRegression regression = new OrdinaryLeastSquaresRegression();
    try {
      regression.checkData(null, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    Double[][] x = new Double[0][0];
    try {
      regression.checkData(x, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    Double[] y = new Double[0];
    try {
      regression.checkData(x, (Double[]) null, y);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    x = new Double[1][2];
    y = new Double[3];
    try {
      regression.checkData(x, (Double[]) null, y);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    x = new Double[][] { new Double[] { 1., 2., 3. }, new Double[] { 4., 5. }, new Double[] { 6., 7., 8. }, new Double[] { 9., 0., 0. } };
    try {
      regression.checkData(x, (Double[]) null, y);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    x[1] = new Double[] { 4., 5., 6. };
    try {
      regression.checkData(x, (Double[]) null, y);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    y = new Double[] { 1., 2., 3., 4. };
    Double[] w1 = new Double[0];
    try {
      regression.checkData(x, w1, y);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    Double[][] w = new Double[0][0];
    try {
      regression.checkData(x, w, y);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    w1 = new Double[3];
    try {
      regression.checkData(x, w, y);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    w = new Double[3][0];
    try {
      regression.checkData(x, w, y);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    w = new Double[][] { new Double[] { 1., 2., 3. }, new Double[] { 4., 5. }, new Double[] { 6., 7., 8. }, new Double[] { 9., 0., 0. } };
    try {
      regression.checkData(x, w, y);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
