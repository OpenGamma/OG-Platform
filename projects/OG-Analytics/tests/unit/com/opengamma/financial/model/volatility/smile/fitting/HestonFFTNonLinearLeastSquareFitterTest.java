/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
public class HestonFFTNonLinearLeastSquareFitterTest extends LeastSquareSmileFitterTestCase {
  private static final double ALPHA = -0.5;
  private static final double LIMIT_TOLERANCE = 1e-8;
  private static final Interpolator1D<Interpolator1DDataBundle> INTERPOLATOR = Interpolator1DFactory.getInterpolator("DoubleQuadratic");
  private static final HestonFFTNonLinearLeastSquareFitter FITTER = new HestonFFTNonLinearLeastSquareFitter();
  private static final double[] INITIAL_VALUES = new double[] {0.5, 0.3, 0.2, 0.1, 0};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator1() {
    new HestonFFTNonLinearLeastSquareFitter(null, ALPHA, LIMIT_TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator2() {
    new HestonFFTNonLinearLeastSquareFitter(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroAlpha() {
    new HestonFFTNonLinearLeastSquareFitter(INTERPOLATOR, 0, LIMIT_TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongAlpha() {
    new HestonFFTNonLinearLeastSquareFitter(INTERPOLATOR, -1, LIMIT_TOLERANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTolerance() {
    new HestonFFTNonLinearLeastSquareFitter(INTERPOLATOR, ALPHA, -LIMIT_TOLERANCE);
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
