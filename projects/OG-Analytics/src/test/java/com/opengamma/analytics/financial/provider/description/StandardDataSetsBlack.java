/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;

/**
 * Sets of standard data used in tests. Black volatilities examples.
 */
public class StandardDataSetsBlack {

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_LINEAR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);

  //  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_TEN = InterpolatedDoublesSurface.from(
  //      new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0 },
  //      new double[] {2, 2, 2, 10, 10, 10 },
  //      new double[] {0.35, 0.34, 0.25, 0.30, 0.25, 0.20 },
  //      INTERPOLATOR_LINEAR_2D);
  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_DELAY = InterpolatedDoublesSurface.from(
      new double[] {0.20, 0.20, 0.20, 0.20, 0.45, 0.45, 0.45, 0.45, 0.70, 0.70, 0.70, 0.70 },
      new double[] {0.00, 0.08, 0.16, 1.00, 0.00, 0.08, 0.16, 1.00, 0.00, 0.08, 0.16, 1.00 },
      new double[] {0.35, 0.34, 0.33, 0.32, 0.30, 0.29, 0.28, 0.27, 0.29, 0.28, 0.27, 0.26 },
      INTERPOLATOR_LINEAR_2D);

  //  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_STRIKE_RATE = InterpolatedDoublesSurface.from(
  //      new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0, 0.5, 1.0, 5.0 },
  //      new double[] {0.01, 0.01, 0.01, 0.02, 0.02, 0.02, 0.03, 0.03, 0.03 },
  //      new double[] {0.35, 0.34, 0.25, 0.30, 0.25, 0.20, 0.28, 0.23, 0.18 },
  //      INTERPOLATOR_LINEAR_2D);
  //  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_STRIKE_PRICE = InterpolatedDoublesSurface.from(
  //      new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0, 0.5, 1.0, 5.0 },
  //      new double[] {0.01, 0.01, 0.01, 0.02, 0.02, 0.02, 0.03, 0.03, 0.03 },
  //      new double[] {0.35, 0.34, 0.25, 0.30, 0.25, 0.20, 0.28, 0.23, 0.18 },
  //      INTERPOLATOR_LINEAR_2D);

  /**
   * Returns an interpolated surface (linear interpolation, flat extrapolation) with dimensions expiry/delay
   * @return
   */
  public static InterpolatedDoublesSurface blackSurfaceExpiryDelay() {
    return BLACK_SURFACE_EXP_DELAY;
  }

}
