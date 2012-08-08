/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.horizon.ConstantSpreadYieldCurveBundleRolldownFunction;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public final class ForwardCurveThetaCalculator {
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVES_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();

  private static final ForwardCurveThetaCalculator INSTANCE = new ForwardCurveThetaCalculator();

  public static ForwardCurveThetaCalculator getInstance() {
    return INSTANCE;
  }

  private ForwardCurveThetaCalculator() {
  }

  public MultipleCurrencyAmount getTheta(final ForexOptionVanillaDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final SmileDeltaTermStructureDataBundle data, final int daysForward) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = instrumentToday.accept(paymentCalculator);
    final YieldCurveBundle shiftedCurves = CURVES_ROLLDOWN.rollDown(data, shiftTime);
    final SmileDeltaTermStructureDataBundle tomorrowData = data.with(shiftedCurves);
    final PresentValueBlackSmileForexCalculator pvCalculator = PresentValueBlackSmileForexCalculator.getInstance();
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data)).plus(paymentToday);
  }


  private MultipleCurrencyAmount subtract(final MultipleCurrencyAmount a, final MultipleCurrencyAmount b) {
    return a.plus(b.multipliedBy(-1));
  }
}
