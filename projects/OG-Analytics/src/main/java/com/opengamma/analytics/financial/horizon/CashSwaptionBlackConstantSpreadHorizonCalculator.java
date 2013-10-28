/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * For cash-settled swaptions, calculates the difference in present value between one day and the next without volatility
 * or rate slide i.e. the market moves in such a way that the interest rates or volatility for the same time to maturity
 * will be equal. The pricing model used is the Black model.
 * @deprecated This class uses deprecated market data objects.
 */
@Deprecated
public class CashSwaptionBlackConstantSpreadHorizonCalculator implements HorizonCalculator<SwaptionCashFixedIborDefinition, YieldCurveWithBlackSwaptionBundle, Void> {
  /** Rolls down swaption data (curves and surface) */
  private static final ConstantSpreadSwaptionBlackRolldown SWAPTION_ROLLDOWN = ConstantSpreadSwaptionBlackRolldown.getInstance();
  /** Present value calculator */
  private static final PresentValueBlackCalculator PV_CALCULATOR = PresentValueBlackCalculator.getInstance();

  @Override
  public MultipleCurrencyAmount getTheta(final SwaptionCashFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveWithBlackSwaptionBundle data, final int daysForward, final Calendar calendar) {
    return getTheta(definition, date, yieldCurveNames, data, daysForward, calendar, null);
  }

  @Override
  public MultipleCurrencyAmount getTheta(final SwaptionCashFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveWithBlackSwaptionBundle data, final int daysForward, final Calendar calendar, final Void additionalData) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final SwaptionCashFixedIbor swaptionToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final SwaptionCashFixedIbor swaptionTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final Currency currency = definition.getCurrency();
    final YieldCurveWithBlackSwaptionBundle tomorrowData = SWAPTION_ROLLDOWN.rollDown(data, shiftTime);
    final double result = swaptionTomorrow.accept(PV_CALCULATOR, tomorrowData) - swaptionToday.accept(PV_CALCULATOR, data);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

}
