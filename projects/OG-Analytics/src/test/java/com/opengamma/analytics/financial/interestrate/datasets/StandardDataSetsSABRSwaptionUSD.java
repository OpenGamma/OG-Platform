/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.datasets;

import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;

/**
 * Sets of market data used in tests.
 */
public class StandardDataSetsSABRSwaptionUSD {

  /**
   * The linear interpolator/ flat extrapolator.  Used for SABR parameters interpolation.
   */
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with a given SABR function. Expiry is between 0 and 10 years, maturity between 0 and 10 years.
   * Beta is 0.5.  Alpha 0.05 at 1Y and 0.06 at 10Y. Rho 0.50 at 1Y and 0.30 at 10Y. Nu -0.25 at 1Y and 0.00 at 10Y.
   * @param sabrFunction The SABR function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1(final VolatilityFunctionProvider<SABRFormulaData> sabrFunction) {
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(
        new double[] {0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10, 0.0, 0.5, 1, 2, 5, 10 },
        new double[] {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100 },
        new double[] {0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06, 0.06 },
        INTERPOLATOR_2D);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(
        new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100 },
        new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100 },
        new double[] {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 },
        INTERPOLATOR_2D);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(
        new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100 },
        new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100 },
        new double[] {-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25,
          0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00 },
        INTERPOLATOR_2D);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(
        new double[] {0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100, 0.0, 0.5, 1, 2, 5, 10, 100 },
        new double[] {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 10, 10, 10, 10, 10, 10, 10, 100, 100, 100, 100, 100, 100, 100 },
        new double[] {0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50, 0.50,
          0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30 },
        INTERPOLATOR_2D);
    return new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, sabrFunction);
  }

  /**
   * Create a set of SABR parameter surface (linearly interpolated) with Hagan volatility function.
   * @return The SABR parameters parameters.
   */
  public static SABRInterestRateParameters createSABR1() {
    return createSABR1(new SABRHaganVolatilityFunction());
  }

}
