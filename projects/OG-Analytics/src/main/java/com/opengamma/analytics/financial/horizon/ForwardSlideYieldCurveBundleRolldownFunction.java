/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Produces a yield curve bundle where each curve has a forward slide.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class ForwardSlideYieldCurveBundleRolldownFunction implements RolldownFunction<YieldCurveBundle> {
  /** Rolls down the yield curve s*/
  private static final ForwardSlideYieldCurveRolldownFunction CURVE_ROLLDOWN = ForwardSlideYieldCurveRolldownFunction.getInstance();
  /** The singleton instance */
  private static final ForwardSlideYieldCurveBundleRolldownFunction INSTANCE = new ForwardSlideYieldCurveBundleRolldownFunction();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static ForwardSlideYieldCurveBundleRolldownFunction getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private ForwardSlideYieldCurveBundleRolldownFunction() {
  }

  @Override
  public YieldCurveBundle rollDown(final YieldCurveBundle data, final double time) {
    final YieldCurveBundle shiftedCurves = new YieldCurveBundle(data.getFxRates(), data.getCurrencyMap());
    for (final String name : data.getAllNames()) {
      shiftedCurves.setCurve(name, CURVE_ROLLDOWN.rollDown(data.getCurve(name), time));
    }
    return shiftedCurves;
  }

}
