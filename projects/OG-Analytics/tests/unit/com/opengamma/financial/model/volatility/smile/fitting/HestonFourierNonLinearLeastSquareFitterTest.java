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
public class HestonFourierNonLinearLeastSquareFitterTest extends LeastSquareSmileFitterTestCase {
  private static final HestonFourierNonLinearLeastSquareFitter FITTER = new HestonFourierNonLinearLeastSquareFitter();
  private static final double[] INITIAL_VALUES = new double[] {0.5, 0.3, 0.2, 0.1, 0};
  private static final Interpolator1D<Interpolator1DDataBundle> INTERPOLATOR = Interpolator1DFactory.getInterpolator("DoubleQuadratic");

  @Override
  protected LeastSquareSmileFitter getFitter() {
    return FITTER;
  }

  @Override
  protected double[] getInitialValues() {
    return INITIAL_VALUES;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator1() {
    new HestonFourierNonLinearLeastSquareFitter(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator2() {
    new HestonFourierNonLinearLeastSquareFitter(null, 0.5, 1e-4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroAlpha() {
    new HestonFourierNonLinearLeastSquareFitter(INTERPOLATOR, 0, 1e-6);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongAlpha() {
    new HestonFourierNonLinearLeastSquareFitter(INTERPOLATOR, -1, 1e-6);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTolerance() {
    new HestonFourierNonLinearLeastSquareFitter(INTERPOLATOR, 0.5, -1e-6);
  }
}
