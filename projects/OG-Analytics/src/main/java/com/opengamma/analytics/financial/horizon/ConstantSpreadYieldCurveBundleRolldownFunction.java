/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Produces a {@link YieldCurveBundle} that has been shifted forward in time without slide.
 * That is, it moves in such a way that the rate or discount factor requested for the same maturity <b>date</b>
 * will be equal for the original market data bundle and the shifted one.
 * @deprecated {@link YieldCurveBundle} is deprecated.
 */
@Deprecated
public final class ConstantSpreadYieldCurveBundleRolldownFunction implements RolldownFunction<YieldCurveBundle> {
  /** Rolls down the yield curves without slide */
  private static final ConstantSpreadYieldCurveRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveRolldownFunction.getInstance();
  /** The singleton instance */
  private static final ConstantSpreadYieldCurveBundleRolldownFunction INSTANCE = new ConstantSpreadYieldCurveBundleRolldownFunction();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static ConstantSpreadYieldCurveBundleRolldownFunction getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private ConstantSpreadYieldCurveBundleRolldownFunction() {
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
