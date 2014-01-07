/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class VanillaFXOptionConstantSpreadHorizonCalculator implements HorizonCalculatorDeprecated<ForexOptionVanillaDefinition, SmileDeltaTermStructureDataBundle, Void> {
  /** Rolls down FX option data (surfaces and surface) */
  private static final ConstantSpreadFXOptionBlackRolldown FX_OPTION_ROLLDOWN = ConstantSpreadFXOptionBlackRolldown.getInstance();

  @Override
  public MultipleCurrencyAmount getTheta(final ForexOptionVanillaDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final SmileDeltaTermStructureDataBundle data,
      final int daysForward, final Calendar calendar) {
    return getTheta(definition, date, yieldCurveNames, data, daysForward, calendar);
  }

  @Override
  public MultipleCurrencyAmount getTheta(final ForexOptionVanillaDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final SmileDeltaTermStructureDataBundle data,
      final int daysForward, final Calendar calendar, final Void additionalData) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final SmileDeltaTermStructureDataBundle tomorrowData = FX_OPTION_ROLLDOWN.rollDown(data, shiftTime);
    final PresentValueBlackSmileForexCalculator pvCalculator = PresentValueBlackSmileForexCalculator.getInstance();
    return instrumentTomorrow.accept(pvCalculator, tomorrowData).plus(instrumentToday.accept(pvCalculator, data).multipliedBy(-1));
  }

}
