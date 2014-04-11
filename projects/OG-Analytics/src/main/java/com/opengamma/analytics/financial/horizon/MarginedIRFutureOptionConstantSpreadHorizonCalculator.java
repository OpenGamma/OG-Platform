/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class MarginedIRFutureOptionConstantSpreadHorizonCalculator implements HorizonCalculatorDeprecated<InterestRateFutureOptionMarginTransactionDefinition, YieldCurveWithBlackCubeBundle, Double> {
  /** Rolls down interest rate future option data (curves and surface) */
  private static final ConstantSpreadInterestRateFutureOptionBlackDataRolldown IR_FUTURE_OPTION_ROLLDOWN = ConstantSpreadInterestRateFutureOptionBlackDataRolldown.getInstance();

  @Override
  public MultipleCurrencyAmount getTheta(final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime date,
      final String[] yieldCurveNames, final YieldCurveWithBlackCubeBundle data, final int daysForward,
      final Calendar calendar) {
    throw new UnsupportedOperationException("Need to supply a last margin value");
  }

  @Override
  public MultipleCurrencyAmount getTheta(final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime date,
      final String[] yieldCurveNames, final YieldCurveWithBlackCubeBundle data, final int daysForward,
      final Calendar calendar, final Double lastMarginPrice) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(lastMarginPrice, "last margin price");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final ZonedDateTime expiry = definition.getUnderlyingSecurity().getExpirationDate();
    ArgumentChecker.isTrue(!date.isAfter(expiry), "Attempted to compute theta on expiry ir future option. date = " + date + ", expiry = " + expiry);
    final ZonedDateTime horizon = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizon);
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();

    // Compute today's pv
    final Currency currency = definition.getUnderlyingSecurity().getUnderlyingFuture().getCurrency();
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, lastMarginPrice, yieldCurveNames);
    final double valueToday = instrumentToday.accept(pvCalculator, data);

    // Compute value at horizon
    final double valueHorizon;
    if (horizon.isBefore(definition.getUnderlyingSecurity().getExpirationDate())) {
      final InstrumentDerivative instrumentHorizon = definition.toDerivative(horizon, lastMarginPrice, yieldCurveNames);
      final YieldCurveWithBlackCubeBundle tomorrowData = IR_FUTURE_OPTION_ROLLDOWN.rollDown(data, shiftTime);
      valueHorizon = instrumentHorizon.accept(pvCalculator, tomorrowData);
    } else {
      valueHorizon = 0.0;
    }

    final double result = valueHorizon - valueToday;
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }
}
