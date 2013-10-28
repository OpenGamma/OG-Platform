/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.calculator.generic.TodayPaymentCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the change in value of a FX option when the (Black) surface has been
 * shifted forward in time without slide.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class VolatilitySurfaceConstantSpreadCalculator {
  /** The singleton instance */
  private static final VolatilitySurfaceConstantSpreadCalculator INSTANCE = new VolatilitySurfaceConstantSpreadCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static VolatilitySurfaceConstantSpreadCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private VolatilitySurfaceConstantSpreadCalculator() {
  }

  public MultipleCurrencyAmount getTheta(final ForexOptionVanillaDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final SmileDeltaTermStructureDataBundle data, final int daysForward) {
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1"); // TODO: Update signature of function to take ForwardOrBack
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = instrumentToday.accept(paymentCalculator);
    final SmileDeltaTermStructureParametersStrikeInterpolation volatilityData = data.getVolatilityModel();
    final SmileDeltaTermStructureParametersStrikeInterpolation smile = new SmileDeltaTermStructureParametersStrikeInterpolation(volatilityData.getVolatilityTerm(),
        volatilityData.getStrikeInterpolator()) {

      @Override
      public double getVolatility(final double time, final double strike, final double forward) {
        return volatilityData.getVolatility(time + shiftTime, strike, forward);
      }
    };
    final SmileDeltaTermStructureDataBundle tomorrowData = data.with(smile);
    final PresentValueBlackSmileForexCalculator pvCalculator = PresentValueBlackSmileForexCalculator.getInstance();
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data)).plus(paymentToday);
  }

  private MultipleCurrencyAmount subtract(final MultipleCurrencyAmount a, final MultipleCurrencyAmount b) {
    return a.plus(b.multipliedBy(-1));
  }
}
