/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class FXConstantSpreadHorizonCalculator implements HorizonCalculatorDeprecated<ForexDefinition, YieldCurveBundle, Void> {
  /** Rolls down a yield curve */
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();
  /** The present value calculator */
  private static final PresentValueMCACalculator PV_CALCULATOR = PresentValueMCACalculator.getInstance();

  @Override
  public MultipleCurrencyAmount getTheta(final ForexDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final int daysForward, final Calendar calendar) {
    return getTheta(definition, date, yieldCurveNames, data, daysForward, calendar);
  }

  @Override
  public MultipleCurrencyAmount getTheta(final ForexDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final int daysForward, final Calendar calendar, final Void additionalData) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    return instrumentTomorrow.accept(PV_CALCULATOR, tomorrowData).plus(instrumentToday.accept(PV_CALCULATOR, data).multipliedBy(-1));
  }

}
