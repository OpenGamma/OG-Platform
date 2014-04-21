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
  private static final Interpolator1D TIME_SQUARE_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.TIME_SQUARE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final GridInterpolator2D INTERPOLATOR_LINEAR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);
  private static final GridInterpolator2D INTERPOLATOR_TIMESQUARE_LINEAR_2D = new GridInterpolator2D(TIME_SQUARE_FLAT, LINEAR_FLAT);

  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_DELAY = InterpolatedDoublesSurface.from(
      new double[] {0.20, 0.20, 0.20, 0.20, 0.45, 0.45, 0.45, 0.45, 0.70, 0.70, 0.70, 0.70 },
      new double[] {0.00, 0.08, 0.16, 1.00, 0.00, 0.08, 0.16, 1.00, 0.00, 0.08, 0.16, 1.00 },
      new double[] {0.35, 0.34, 0.33, 0.32, 0.30, 0.29, 0.28, 0.27, 0.29, 0.28, 0.27, 0.26 },
      INTERPOLATOR_LINEAR_2D);

  private static final double[] EXPIRY2 = new double[] {0.20, 0.20, 0.20, 0.20, 0.20, 0.45, 0.45, 0.45, 0.45, 0.45 };
  private static final double[] LOGMONEY = new double[] {-0.010, -0.005, 0.000, 0.005, 0.010, -0.010, -0.005, 0.000, 0.005, 0.010 };
  private static final double[] VOL_EXP_LOGMONEY = new double[] {0.50, 0.49, 0.47, 0.48, 0.51, 0.45, 0.44, 0.42, 0.43, 0.46 };
  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_LOGMONEY = InterpolatedDoublesSurface.from(EXPIRY2,
      LOGMONEY, VOL_EXP_LOGMONEY, INTERPOLATOR_TIMESQUARE_LINEAR_2D);

  /** Parameters surface in dimension expiry/log-moneyness. Flat in the moneyness dimension for testing purposes */
  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_LOGMONEY_FLAT = InterpolatedDoublesSurface.from(EXPIRY2,
      LOGMONEY, new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.45, 0.45, 0.45, 0.45, 0.45 }, INTERPOLATOR_TIMESQUARE_LINEAR_2D);

  /**
   * Returns an interpolated surface (linear interpolation, flat extrapolation) with dimensions expiry/delay.
   * @return The surface.
   */
  public static InterpolatedDoublesSurface blackSurfaceExpiryDelay() {
    return BLACK_SURFACE_EXP_DELAY;
  }

  /**
   * Returns an interpolated surface (time-square on expiration / linear on log moneyness interpolation, flat extrapolation)
   * @return The surface.
   */
  public static InterpolatedDoublesSurface blackSurfaceExpiryLogMoneyness() {
    return BLACK_SURFACE_EXP_LOGMONEY;
  }

  /**
   * Returns an interpolated surface (time-square on expiration / linear on log moneyness interpolation, flat extrapolation)
   * @return The surface.
   */
  public static InterpolatedDoublesSurface blackSurfaceExpiryLogMoneyness(final double shift) {
    final double[] shiftedVol = VOL_EXP_LOGMONEY.clone();
    for (int loopvol = 0; loopvol < shiftedVol.length; loopvol++) {
      shiftedVol[loopvol] += shift;
    }
    return InterpolatedDoublesSurface.from(EXPIRY2, LOGMONEY, shiftedVol, INTERPOLATOR_TIMESQUARE_LINEAR_2D);
  }

  /**
   * Returns an interpolated surface (time-square on expiration / linear on log moneyness interpolation, flat extrapolation).
   * Surface is flat in the moneyness dimension to facilitate some tests.
   * @return The surface.
   */
  public static InterpolatedDoublesSurface blackSurfaceExpiryLogMoneynessFlat() {
    return BLACK_SURFACE_EXP_LOGMONEY_FLAT;
  }

}
