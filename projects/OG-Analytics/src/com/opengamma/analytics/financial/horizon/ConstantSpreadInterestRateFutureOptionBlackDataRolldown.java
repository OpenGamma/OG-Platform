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
 * 
 */
public final class ConstantSpreadInterestRateFutureOptionBlackDataRolldown implements RolldownFunction<YieldCurveWithBlackCubeBundle> {
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVES_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();
  private static final ConstantSpreadSurfaceRolldownFunction SURFACE_ROLLDOWN = ConstantSpreadSurfaceRolldownFunction.getInstance();
  private static final ConstantSpreadInterestRateFutureOptionBlackDataRolldown INSTANCE = new ConstantSpreadInterestRateFutureOptionBlackDataRolldown();

  public static ConstantSpreadInterestRateFutureOptionBlackDataRolldown getInstance() {
    return INSTANCE;
  }

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
