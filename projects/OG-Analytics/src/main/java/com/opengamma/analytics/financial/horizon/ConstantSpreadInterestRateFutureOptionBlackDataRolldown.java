/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Produces a YieldCurveWithBlackCubeBundle that has been shifted forward in time without slide.
 * That is, it moves in such a way that the vol or rate requested for the same maturity DATE will be equal
 * for the original market data bundle and the shifted one.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class ConstantSpreadInterestRateFutureOptionBlackDataRolldown implements RolldownFunction<YieldCurveWithBlackCubeBundle> {
  /** Rolls down the yield curves without slide */
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVES_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();
  /** Rolls down the volatility surface without slide */
  private static final ConstantSpreadSurfaceRolldownFunction SURFACE_ROLLDOWN = ConstantSpreadSurfaceRolldownFunction.getInstance();
  /** The singleton instance */
  private static final ConstantSpreadInterestRateFutureOptionBlackDataRolldown INSTANCE = new ConstantSpreadInterestRateFutureOptionBlackDataRolldown();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static ConstantSpreadInterestRateFutureOptionBlackDataRolldown getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private ConstantSpreadInterestRateFutureOptionBlackDataRolldown() {
  }

  @Override
  public YieldCurveWithBlackCubeBundle rollDown(final YieldCurveWithBlackCubeBundle data, final double time) {
    final YieldCurveBundle shiftedCurves = CURVES_ROLLDOWN.rollDown(data, time);
    final Surface<Double, Double, Double> surface = data.getBlackParameters();
    final Surface<Double, Double, Double> shiftedVolatilitySurface = SURFACE_ROLLDOWN.rollDown(surface, time);
    return new YieldCurveWithBlackCubeBundle(shiftedVolatilitySurface, shiftedCurves);
  }
}
