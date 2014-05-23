/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Calculates the change in value of a FX option when the curves and (Black) surface have been
 * shifted forward in time.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class ForwardSlideFXOptionBlackRolldown implements RolldownFunction<SmileDeltaTermStructureDataBundle> {
  /** Rolls down the yield curves */
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVES_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();
  /** The singleton instance */
  private static final ForwardSlideFXOptionBlackRolldown INSTANCE = new ForwardSlideFXOptionBlackRolldown();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static ForwardSlideFXOptionBlackRolldown getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private ForwardSlideFXOptionBlackRolldown() {
  }

  @Override
  public SmileDeltaTermStructureDataBundle rollDown(final SmileDeltaTermStructureDataBundle data, final double shiftTime) {
    final YieldCurveBundle shiftedCurves = CURVES_ROLLDOWN.rollDown(data, shiftTime);
    final Pair<Currency, Currency> currencyPair = data.getCurrencyPair();
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityData = data.getVolatilityModel();
    final SmileDeltaTermStructureParametersStrikeInterpolation smile = new SmileDeltaTermStructureParametersStrikeInterpolation(volatilityData.getVolatilityTerm(),
        volatilityData.getStrikeInterpolator()) {

      @Override
      public double getVolatility(final double time, final double strike, final double forward) {
        return volatilityData.getVolatility(time + shiftTime, strike, forward);
      }
    };
    return new SmileDeltaTermStructureDataBundle(shiftedCurves, smile, currencyPair);
  }

}
