/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Sets of market data used in tests. Normal or Bachelier model.
 */
public class NormalDataSets {

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);

  private static final double[] EXPIRATIONS_1 = new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0};
  private static final double[] STIRFUTURES_STRIKES_PRICES_1 = new double[] {0.99, 0.99, 0.99, 0.995, 0.995, 0.995};
  private static final double[] VOLATILITIES_1 = new double[] {0.0100, 0.0110, 0.0120, 0.0090, 0.0100, 0.0100};
  private static final InterpolatedDoublesSurface NORMAL_SURFACE_STIRFUTURES_EXP_STRIKEPRICE = 
      InterpolatedDoublesSurface.from(EXPIRATIONS_1, STIRFUTURES_STRIKES_PRICES_1, VOLATILITIES_1, INTERPOLATOR_2D);

  public static InterpolatedDoublesSurface createNormalSurfaceFuturesPrices() {
    return NORMAL_SURFACE_STIRFUTURES_EXP_STRIKEPRICE;
  }

  public static InterpolatedDoublesSurface createNormalSurfaceFuturesPricesShift(final double shift) {
    double[] shiftedVol = VOLATILITIES_1.clone();
    for (int loopvol = 0; loopvol < shiftedVol.length; loopvol++) {
      shiftedVol[loopvol] += shift;
    }
    return InterpolatedDoublesSurface.from(EXPIRATIONS_1, STIRFUTURES_STRIKES_PRICES_1, shiftedVol, INTERPOLATOR_2D);
  }


  private static final double[] EXPIRATIONS_2 = 
      new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0, 0.5, 1.0, 5.0, 0.5, 1.0, 5.0};
  private static final double[] SWAPTION_TENOR_2 = 
      new double[] {1.0d, 1.0d, 1.0d, 2.0d, 2.0d, 2.0d, 5.0d, 5.0d, 5.0d, 10.0d, 10.0d, 10.0d};
  private static final double[] VOLATILITIES_2 = 
      new double[] {0.0100, 0.0110, 0.0120, 0.0090, 0.0100, 0.0100, 0.0100, 0.0110, 0.0120, 0.0090, 0.0100, 0.0100};
  private static final InterpolatedDoublesSurface NORMAL_SURFACE_SWAPTION_EXP_TENOR = 
      InterpolatedDoublesSurface.from(EXPIRATIONS_2, SWAPTION_TENOR_2, VOLATILITIES_2, INTERPOLATOR_2D);

  public static InterpolatedDoublesSurface normalSurfaceSwaptionExpiryTenor() {
    return NORMAL_SURFACE_SWAPTION_EXP_TENOR;
  }
  
}
