/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Computes the difference in present value between one day and the next, without volatility or rate slide.
 * That is, the market moves in such a way that the discount rates or implied volatility requested
 * for the same maturity DATE will be equal on both dates. <p>
 *
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
//TODO Rethink daysForward as it is only safely handles 1/-1.
@Deprecated
public final class ConstantSpreadHorizonThetaCalculator {
  /** Rolls down a yield curve */
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();
  /** Rolls down swaption data (curves and surface) */
  private static final ConstantSpreadSwaptionBlackRolldown SWAPTION_ROLLDOWN = ConstantSpreadSwaptionBlackRolldown.getInstance();
  /** Rolls down interest rate future option data (curves and surface) */
  private static final ConstantSpreadInterestRateFutureOptionBlackDataRolldown IR_FUTURE_OPTION_ROLLDOWN = ConstantSpreadInterestRateFutureOptionBlackDataRolldown.getInstance();
  /** Rolls down FX option data (surfaces and surface) */
  private static final ConstantSpreadFXOptionBlackRolldown FX_OPTION_ROLLDOWN = ConstantSpreadFXOptionBlackRolldown.getInstance();
  /** Singleton instance */
  private static final ConstantSpreadHorizonThetaCalculator INSTANCE = new ConstantSpreadHorizonThetaCalculator();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static ConstantSpreadHorizonThetaCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private ConstantSpreadHorizonThetaCalculator() {
  }

  /**
   * Calculates the theta for a swap without rate slide. This method does not take holidays into account.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param yieldCurveNames The yield curve names, not null
   * @param data The initial yield curve data, not null
   * @param fixingSeries The fixing series, not null
   * @param daysForward The number of days to roll forward, must be +/-1
   * @return The theta
   */
  public MultipleCurrencyAmount getTheta(final SwapDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final ZonedDateTimeDoubleTimeSeries[] fixingSeries, final int daysForward) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(fixingSeries, "fixing series");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final Swap<? extends Payment, ? extends Payment> instrumentToday = definition.toDerivative(date, fixingSeries, yieldCurveNames);
    final ZonedDateTimeDoubleTimeSeries[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, horizonDate);
    final Swap<? extends Payment, ? extends Payment> instrumentTomorrow = definition.toDerivative(horizonDate, shiftedFixingSeries, yieldCurveNames);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final Currency currency = definition.getFirstLeg().getCurrency();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  /**
   * Calculates the theta for a swap without rate slide. This method takes holidays into account when rolling forward.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param yieldCurveNames The yield curve names, not null
   * @param data The initial yield curve data, not null
   * @param fixingSeries The fixing series, not null
   * @param daysForward The number of days to roll forward, must be +/-1
   * @param calendar The holiday calendar, not null
   * @return The theta
   */
  public MultipleCurrencyAmount getTheta(final SwapDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final ZonedDateTimeDoubleTimeSeries[] fixingSeries, final int daysForward, final Calendar calendar) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(fixingSeries, "fixing series");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday;
    final InstrumentDerivative instrumentTomorrow;
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    if (calendar.isWorkingDay(date.toLocalDate())) {
      instrumentToday = definition.toDerivative(date, fixingSeries, yieldCurveNames);
      final ZonedDateTimeDoubleTimeSeries[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, horizonDate);
      instrumentTomorrow = definition.toDerivative(horizonDate, shiftedFixingSeries, yieldCurveNames);
    } else {
      final ZonedDateTime nextWorkingDay = ScheduleCalculator.getAdjustedDate(date, 1, calendar);
      final ZonedDateTime nextHorizonDate = nextWorkingDay.plusDays(daysForward);
      final ZonedDateTimeDoubleTimeSeries[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, nextHorizonDate);
      instrumentToday = definition.toDerivative(nextWorkingDay, fixingSeries, yieldCurveNames);
      instrumentTomorrow = definition.toDerivative(nextHorizonDate, shiftedFixingSeries, yieldCurveNames);
    }
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final Currency currency = definition.getFirstLeg().getCurrency();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  /**
   * Calculates the theta for a fixed / ibor physically-settled swaption without volatility or rate slide.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param yieldCurveNames The yield curve names, not null
   * @param data The initial yield curve data, not null
   * @param daysForward The number of days to roll forward, must be +/-1
   * @return The theta
   */
  public MultipleCurrencyAmount getTheta(final SwaptionPhysicalFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveWithBlackSwaptionBundle data, final int daysForward) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
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

  /**
   * Calculates the theta for a fixed / ibor cash-settled swaption without volatility or rate slide.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param yieldCurveNames The yield curve names, not null
   * @param data The initial yield curve data, not null
   * @param daysForward The number of days to roll forward, must be +/-1
   * @return The theta
   */
  public MultipleCurrencyAmount getTheta(final SwaptionCashFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveWithBlackSwaptionBundle data, final int daysForward) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final SwaptionCashFixedIbor swaptionToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final SwaptionCashFixedIbor swaptionTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final Currency currency = definition.getCurrency();
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final YieldCurveWithBlackSwaptionBundle tomorrowData = SWAPTION_ROLLDOWN.rollDown(data, shiftTime);
    final double result = swaptionTomorrow.accept(pvCalculator, tomorrowData) - swaptionToday.accept(pvCalculator, data);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  /**
   * Calculates the theta for an interest rate future without rate slide.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param yieldCurveNames The yield curve names, not null
   * @param data The initial yield curve data, not null
   * @param lastMarginPrice Last margin price, not null
   * @param daysForward The number of days to roll forward, must be +/-1
   * @return The theta
   */
  public MultipleCurrencyAmount getTheta(final InterestRateFutureTransactionDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final Double lastMarginPrice, final int daysForward) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(lastMarginPrice, "last margin price");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, lastMarginPrice, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, lastMarginPrice, yieldCurveNames);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final Currency currency = definition.getUnderlyingSecurity().getCurrency();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  /**
   * Calculates the theta for an interest rate future option without volatility or rate slide.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param yieldCurveNames The yield curve names, not null
   * @param data The initial yield curve data, not null
   * @param lastMarginPrice Last margin price, not null
   * @param daysForward The number of days to roll forward, must be +/-1
   * @return The theta
   */
  public MultipleCurrencyAmount getTheta(final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveWithBlackCubeBundle data, final Double lastMarginPrice, final int daysForward) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(lastMarginPrice, "last margin price");
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

  /**
   * Calculates the theta for a FX spot or forward trade without rate slide.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param yieldCurveNames The yield curve names, not null
   * @param data The initial yield curve data, not null
   * @param daysForward The number of days to roll forward, must be +/-1
   * @return The theta
   */
  public MultipleCurrencyAmount getTheta(final ForexDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final int daysForward) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final PresentValueMCACalculator pvCalculator = PresentValueMCACalculator.getInstance();
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data));
  }

  /**
   * Calculates the theta for a vanilla FX option without volatility or rate slide.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param yieldCurveNames The yield curve names, not null
   * @param data The initial yield curve data, not null
   * @param daysForward The number of days to roll forward, must be +/-1
   * @return The theta
   */
  public MultipleCurrencyAmount getTheta(final ForexOptionVanillaDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final SmileDeltaTermStructureDataBundle data, final int daysForward) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final SmileDeltaTermStructureDataBundle tomorrowData = FX_OPTION_ROLLDOWN.rollDown(data, shiftTime);
    final PresentValueBlackSmileForexCalculator pvCalculator = PresentValueBlackSmileForexCalculator.getInstance();
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data));
  }

  /**
   * Calculates the theta for a digital FX option without volatility or rate slide.
   * @param definition The swap definition, not null
   * @param date The calculation date, not null
   * @param yieldCurveNames The yield curve names, not null
   * @param data The initial yield curve data, not null
   * @param pvCalculator The present value calculator to use, not null
   * @param daysForward The number of days to roll forward, must be +/-1
   * @return The theta
   */
  public MultipleCurrencyAmount getTheta(final ForexOptionDigitalDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final SmileDeltaTermStructureDataBundle data, final PresentValueMCACalculator pvCalculator, final int daysForward) {
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.notNull(data, "yield curve data");
    ArgumentChecker.notNull(pvCalculator, "present value calculator");
    ArgumentChecker.isTrue(daysForward == 1 || daysForward == -1, "daysForward must be either 1 or -1");
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final SmileDeltaTermStructureDataBundle tomorrowData = FX_OPTION_ROLLDOWN.rollDown(data, shiftTime);
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data));
  }

  /**
   * Create a new time series with the same data up to tomorrow (tomorrow excluded) and with an extra data for tomorrow equal to the last value in the time series.
   * @param fixingSeries The time series.
   * @param tomorrow Tomorrow date.
   * @return The time series with added data.
   */
  private ZonedDateTimeDoubleTimeSeries[] getDateShiftedTimeSeries(final ZonedDateTimeDoubleTimeSeries[] fixingSeries, final ZonedDateTime tomorrow) {
    final int n = fixingSeries.length;
    final ZonedDateTimeDoubleTimeSeries[] laggedFixingSeries = new ZonedDateTimeDoubleTimeSeries[n];
    for (int i = 0; i < n; i++) {
      if (fixingSeries[i].isEmpty()) {
        laggedFixingSeries[i] = ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(tomorrow.getZone());
      } else {
        final ZonedDateTimeDoubleTimeSeries ts = fixingSeries[i].subSeries(fixingSeries[i].getEarliestTime(), tomorrow);
        if (ts == null || ts.isEmpty()) {
          laggedFixingSeries[i] = ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(tomorrow.getZone());
        } else {
          final ZonedDateTimeDoubleTimeSeries subSeries = fixingSeries[i].subSeries(fixingSeries[i].getEarliestTime(), tomorrow);
          final List<ZonedDateTime> times = new ArrayList<>(subSeries.times());
          final List<Double> values = new ArrayList<>(subSeries.values());
          times.add(tomorrow);
          values.add(ts.getLatestValue());
          laggedFixingSeries[i] = ImmutableZonedDateTimeDoubleTimeSeries.of(times, values, tomorrow.getZone());
        }
      }
    }
    return laggedFixingSeries;
  }

  /**
   * Subtracts two multiple currency amounts.
   * @param a The first multiple currency amount
   * @param b The second multiple currency amount
   * @return The difference
   */
  private MultipleCurrencyAmount subtract(final MultipleCurrencyAmount a, final MultipleCurrencyAmount b) {
    return a.plus(b.multipliedBy(-1));
  }
}
