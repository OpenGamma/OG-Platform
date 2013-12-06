/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the "time/square of value" interpolator.
 */
@Test(groups = TestGroup.UNIT)
public class TimeSquareInterpolator1DTest {

  private static final Interpolator1D INTERPOLATOR = new TimeSquareInterpolator1D();
  private static final double[] X = new double[] {1, 2, 3};
  private static final double[] Y = new double[] {4, 5, 6};
  private static final Interpolator1DDataBundle DATA = INTERPOLATOR.getDataBundle(X, Y);

  private static final double TOLERANCE_Y = 1.0E-10;
  private static final double TOLERANCE_SENSI = 1.0E-6;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDataBundle() {
    INTERPOLATOR.interpolate(null, 2.3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullValue() {
    INTERPOLATOR.interpolate(DATA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void lowValue() {
    INTERPOLATOR.interpolate(DATA, -4.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void highValue() {
    INTERPOLATOR.interpolate(DATA, 10.0);
  }

  @Test
  /**
   * Tests the sensitivity of the interpolated value to the Y values.
   */
  public void interpolation() {
    final Interpolator1D interpolatorFromFactory = Interpolator1DFactory.TIME_SQUARE_INSTANCE;
    double[] x = new double[] {1.0, 1.5, 2, 2.75};
    for (int loopx = 0; loopx < x.length; loopx++) {
      double yCalculated = INTERPOLATOR.interpolate(DATA, x[loopx]);
      double yCalculatedFactory = interpolatorFromFactory.interpolate(DATA, x[loopx]);
      final int index = DATA.getLowerBoundIndex(x[loopx]);
      final double weight = (DATA.getKeys()[index + 1] - x[loopx]) / (DATA.getKeys()[index + 1] - DATA.getKeys()[index]);
      final double val1 = DATA.getKeys()[index] * DATA.getValues()[index] * DATA.getValues()[index];
      final double val2 = DATA.getKeys()[index + 1] * DATA.getValues()[index + 1] * DATA.getValues()[index + 1];
      final double yExpected = Math.sqrt((weight * val1 + (1.0 - weight) * val2) / x[loopx]);
      assertEquals("TimeSquare interpolator: data point " + loopx, yExpected, yCalculated, TOLERANCE_Y);
      assertEquals("TimeSquare interpolator: data point " + loopx, yCalculated, yCalculatedFactory, TOLERANCE_Y);
    }
    int lenghtx = DATA.getKeys().length;
    double lastx = DATA.getKeys()[lenghtx - 1];
    double yCalculated = INTERPOLATOR.interpolate(DATA, lastx);
    final double yExpected = DATA.getValues()[lenghtx - 1];
    assertEquals("TimeSquare interpolator: last point", yExpected, yCalculated, TOLERANCE_Y);
  }

  @Test
  /**
   * Tests the sensitivity of the interpolated value to the Y values.
   */
  public void interpolationSensitivity() {
    double shift = 1.0E-6;
    double[] x = new double[] {1.0, 1.5, 2, 2.75};
    for (int loopx = 0; loopx < x.length; loopx++) {
      double yInit = INTERPOLATOR.interpolate(DATA, x[loopx]);
      double[] ySensiCalculated = INTERPOLATOR.getNodeSensitivitiesForValue(DATA, x[loopx]);
      for (int loopsens = 0; loopsens < X.length; loopsens++) {
        double[] yVectorBumped = Y.clone();
        yVectorBumped[loopsens] += shift;
        Interpolator1DDataBundle dataBumped = INTERPOLATOR.getDataBundle(X, yVectorBumped);
        double yBumped = INTERPOLATOR.interpolate(dataBumped, x[loopx]);
        double ySensiExpected = (yBumped - yInit) / shift;
        assertEquals("TimeSquare interpolator: test " + loopx + " node " + loopsens, ySensiExpected, ySensiCalculated[loopsens], TOLERANCE_SENSI);
      }
    }
    int lenghtx = DATA.getKeys().length;
    double lastx = DATA.getKeys()[lenghtx - 1];
    double yInitLast = INTERPOLATOR.interpolate(DATA, lastx);
    double[] ySensiCalculated = INTERPOLATOR.getNodeSensitivitiesForValue(DATA, lastx);
    for (int loopsens = 0; loopsens < X.length; loopsens++) {
      double[] yVectorBumped = Y.clone();
      yVectorBumped[loopsens] += shift;
      Interpolator1DDataBundle dataBumped = INTERPOLATOR.getDataBundle(X, yVectorBumped);
      double yBumped = INTERPOLATOR.interpolate(dataBumped, lastx);
      double ySensiExpected = (yBumped - yInitLast) / shift;
      assertEquals("TimeSquare interpolator: test last node " + loopsens, ySensiExpected, ySensiCalculated[loopsens], TOLERANCE_SENSI);
    }
  }

}
