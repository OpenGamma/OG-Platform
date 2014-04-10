/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class IRFutureConstantSpreadHorizonCalculator implements HorizonCalculatorDeprecated<InterestRateFutureTransactionDefinition, YieldCurveBundle, Double> {
  /** Rolls down a yield curve bundle */
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();

  @Override
  public MultipleCurrencyAmount getTheta(final InterestRateFutureTransactionDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveBundle data, final int daysForward,
      final Calendar calendar) {
    throw new UnsupportedOperationException("Must supply a last margin price");
  }

  @Override
  public MultipleCurrencyAmount getTheta(final InterestRateFutureTransactionDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveBundle data, final int daysForward, final Calendar calendar, final Double lastMarginPrice) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(lastMarginPrice, "last margin price");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, lastMarginPrice, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, lastMarginPrice, yieldCurveNames);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final Currency currency = definition.getUnderlyingFuture().getCurrency();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

}
