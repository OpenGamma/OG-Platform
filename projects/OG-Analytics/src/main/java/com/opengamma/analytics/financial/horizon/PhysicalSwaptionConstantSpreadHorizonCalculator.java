/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class PhysicalSwaptionConstantSpreadHorizonCalculator implements HorizonCalculator<SwaptionPhysicalFixedIborDefinition, YieldCurveWithBlackSwaptionBundle, Void> {
  /** Rolls down swaption data (curves and surface) */
  private static final ConstantSpreadSwaptionBlackRolldown SWAPTION_ROLLDOWN = ConstantSpreadSwaptionBlackRolldown.getInstance();

  @Override
  public MultipleCurrencyAmount getTheta(final SwaptionPhysicalFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveWithBlackSwaptionBundle data, final int daysForward,
      final Calendar calendar) {
    return getTheta(definition, date, yieldCurveNames, data, daysForward, calendar, null);
  }

  @Override
  public MultipleCurrencyAmount getTheta(final SwaptionPhysicalFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveWithBlackSwaptionBundle data, final int daysForward,
      final Calendar calendar, final Void additionalData) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final SwaptionPhysicalFixedIbor swaptionToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final SwaptionPhysicalFixedIbor swaptionTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final Currency currency = definition.getCurrency();
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final YieldCurveWithBlackSwaptionBundle tomorrowData = SWAPTION_ROLLDOWN.rollDown(data, shiftTime);
    final double result = swaptionTomorrow.accept(pvCalculator, tomorrowData) - swaptionToday.accept(pvCalculator, data);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

}
