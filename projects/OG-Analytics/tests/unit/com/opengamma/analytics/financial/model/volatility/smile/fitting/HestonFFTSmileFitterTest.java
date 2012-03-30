/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.HestonFFTSmileFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.LeastSquareSmileFitter;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class HestonFFTSmileFitterTest extends LeastSquareSmileFitterTestCase {
  private static final double ALPHA = -0.5;
  private static final double LIMIT_TOLERANCE = 1e-8;
  private static final Interpolator1D INTERPOLATOR = Interpolator1DFactory.getInterpolator("DoubleQuadratic");
  private static final HestonFFTSmileFitter FITTER = new HestonFFTSmileFitter(false);
  private static final double[] INITIAL_VALUES = new double[] {0.5, 0.3, 0.2, 0.1, 0 };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator1() {
    new HestonFFTSmileFitter(null, ALPHA, LIMIT_TOLERANCE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator2() {
    new HestonFFTSmileFitter(null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroAlpha() {
    new HestonFFTSmileFitter(INTERPOLATOR, 0, LIMIT_TOLERANCE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongAlpha() {
    new HestonFFTSmileFitter(INTERPOLATOR, -1, LIMIT_TOLERANCE, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTolerance() {
    new HestonFFTSmileFitter(INTERPOLATOR, ALPHA, -LIMIT_TOLERANCE, false);
  }

  @Override
  protected LeastSquareSmileFitter getFitter() {
    return FITTER;
  }

  @Override
  protected double[] getInitialValues() {
    return INITIAL_VALUES;
  }

}
