/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import org.testng.annotations.Test;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class HestonFFTOptionPriceNonLinearLeastSquareFitterTest extends LeastSquareSmileFitterTestCase {
  private static final double ALPHA = -0.5;
  private static final double LIMIT_TOLERANCE = 1e-8;
  private static final Interpolator1D<Interpolator1DDataBundle> INTERPOLATOR = Interpolator1DFactory.getInterpolator("DoubleQuadratic");
  private static final HestonFFTOptionPriceNonLinearLeastSquareFitter FITTER = new HestonFFTOptionPriceNonLinearLeastSquareFitter();
  private static final double[] INITIAL_VALUES = new double[] {0.5, 0.3, 0.2, 0.1, 0};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator1() {
    new HestonFFTOptionPriceNonLinearLeastSquareFitter(null, ALPHA, LIMIT_TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator2() {
    new HestonFFTOptionPriceNonLinearLeastSquareFitter(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroAlpha() {
    new HestonFFTOptionPriceNonLinearLeastSquareFitter(INTERPOLATOR, 0, LIMIT_TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongAlpha() {
    new HestonFFTOptionPriceNonLinearLeastSquareFitter(INTERPOLATOR, -1, LIMIT_TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTolerance() {
    new HestonFFTOptionPriceNonLinearLeastSquareFitter(INTERPOLATOR, ALPHA, -LIMIT_TOLERANCE);
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
