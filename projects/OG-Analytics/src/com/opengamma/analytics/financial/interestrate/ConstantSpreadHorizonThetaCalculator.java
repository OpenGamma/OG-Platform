/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.horizon.ConstantSpreadFXBlackRolldown;
import com.opengamma.analytics.financial.horizon.ConstantSpreadInterestRateFutureOptionBlackDataRolldown;
import com.opengamma.analytics.financial.horizon.ConstantSpreadSwaptionBlackRolldown;
import com.opengamma.analytics.financial.horizon.ConstantSpreadYieldCurveBundleRolldownFunction;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;

import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

/**
 *  Computes the difference in present value between one day and the next, without Volatility or Rate slide. 
 * That is, the market moves in such a way that the discount rates or implied volatility requested 
 * for the same maturity DATE will be equal on both dates. <p>
 * 
 * Note that the time to maturity will differ by the daysForward provided in the constructor
 */
public final class ConstantSpreadHorizonThetaCalculator {
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();
  private static final ConstantSpreadSwaptionBlackRolldown SWAPTION_ROLLDOWN = ConstantSpreadSwaptionBlackRolldown.getInstance();
  private static final ConstantSpreadInterestRateFutureOptionBlackDataRolldown IR_FUTURE_OPTION_ROLLDOWN = ConstantSpreadInterestRateFutureOptionBlackDataRolldown.getInstance();
  private static final ConstantSpreadFXBlackRolldown FX_OPTION_ROLLDOWN = ConstantSpreadFXBlackRolldown.getInstance();

  private static final ConstantSpreadHorizonThetaCalculator INSTANCE = new ConstantSpreadHorizonThetaCalculator();

  public static ConstantSpreadHorizonThetaCalculator getInstance() {
    return INSTANCE;
  }

  private ConstantSpreadHorizonThetaCalculator() {
  }

  public MultipleCurrencyAmount getTheta(final SwapFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final DoubleTimeSeries<ZonedDateTime>[] fixingSeries, final int daysForward) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, fixingSeries, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final DoubleTimeSeries<ZonedDateTime>[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, shiftedFixingSeries, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = instrumentToday.accept(paymentCalculator);
    if (paymentToday.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swap");
    }
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final Currency currency = paymentToday.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data) + paymentToday.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final SwapFixedIborSpreadDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final DoubleTimeSeries<ZonedDateTime>[] fixingSeries, final int daysForward) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, fixingSeries, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final DoubleTimeSeries<ZonedDateTime>[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, shiftedFixingSeries, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = instrumentToday.accept(paymentCalculator);
    if (paymentToday.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swap");
    }
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final Currency currency = paymentToday.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data) + paymentToday.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final SwapFixedONDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final DoubleTimeSeries<ZonedDateTime>[] fixingSeries, final int daysForward) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, fixingSeries, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final DoubleTimeSeries<ZonedDateTime>[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, horizonDate);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, shiftedFixingSeries, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = instrumentToday.accept(paymentCalculator);
    if (paymentToday.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swap");
    }
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final Currency currency = paymentToday.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data) + paymentToday.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final SwaptionPhysicalFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveWithBlackSwaptionBundle data, final int daysForward) {
    final SwaptionPhysicalFixedIbor swaptionToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final SwaptionPhysicalFixedIbor swaptionTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = swaptionToday.accept(paymentCalculator);
    if (paymentToday.size() != 1 || !paymentToday.getCurrencyAmounts()[0].getCurrency().equals(definition.getUnderlyingSwap().getCurrency())) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swaption");
    }
    final Currency currency = definition.getUnderlyingSwap().getCurrency();
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final YieldCurveWithBlackSwaptionBundle tomorrowData = SWAPTION_ROLLDOWN.rollDown(data, shiftTime);
    final double result = swaptionTomorrow.accept(pvCalculator, tomorrowData) - swaptionToday.accept(pvCalculator, data) + paymentToday.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final SwaptionCashFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveWithBlackSwaptionBundle data, final int daysForward) {
    final SwaptionCashFixedIbor swaptionToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final SwaptionCashFixedIbor swaptionTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = swaptionToday.accept(paymentCalculator);
    if (paymentToday.size() != 1 || !paymentToday.getCurrencyAmounts()[0].getCurrency().equals(definition.getUnderlyingSwap().getCurrency())) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swaption");
    }
    final Currency currency = definition.getUnderlyingSwap().getCurrency();
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final YieldCurveWithBlackSwaptionBundle tomorrowData = SWAPTION_ROLLDOWN.rollDown(data, shiftTime);
    final double result = swaptionTomorrow.accept(pvCalculator, tomorrowData) - swaptionToday.accept(pvCalculator, data) + paymentToday.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final InterestRateFutureDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final Double lastMarginPrice, final int daysForward) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, lastMarginPrice, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, lastMarginPrice, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = instrumentToday.accept(paymentCalculator);
    if (paymentToday.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the interest rate future");
    }
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final Currency currency = paymentToday.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data) + paymentToday.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveWithBlackCubeBundle data, final Double lastMarginPrice, final int daysForward) {

    final ZonedDateTime expiry = definition.getUnderlyingOption().getExpirationDate();
    Validate.isTrue(!date.isAfter(expiry), "Attempted to compute theta on expiry ir future option. date = " + date + ", expiry = " + expiry);
    final ZonedDateTime horizon = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizon);
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();

    // Compute today's pv
    final Currency currency = definition.getUnderlyingOption().getUnderlyingFuture().getCurrency();
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, lastMarginPrice, yieldCurveNames);
    final double valueToday = instrumentToday.accept(pvCalculator, data);

    // Compute cash flows between today and horizon
    // TODO Confirm choice that no payment is made on expiry
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final MultipleCurrencyAmount paymentToday = instrumentToday.accept(paymentCalculator);
    if (paymentToday.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the interest rate future option");
    }

    // Compute value at horizon
    final double valueHorizon;
    if (horizon.isBefore(definition.getUnderlyingOption().getExpirationDate())) {
      final InstrumentDerivative instrumentHorizon = definition.toDerivative(horizon, lastMarginPrice, yieldCurveNames);
      final YieldCurveWithBlackCubeBundle tomorrowData = IR_FUTURE_OPTION_ROLLDOWN.rollDown(data, shiftTime);
      valueHorizon = instrumentHorizon.accept(pvCalculator, tomorrowData);
    } else {
      valueHorizon = 0.0;
    }

    final double result = valueHorizon + paymentToday.getAmount(currency) - valueToday;
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final ForexDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data,
      final int daysForward) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = instrumentToday.accept(paymentCalculator);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, shiftTime);
    final PresentValueForexCalculator pvCalculator = PresentValueForexCalculator.getInstance();
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data)).plus(paymentToday);
  }

  public MultipleCurrencyAmount getTheta(final ForexOptionVanillaDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final SmileDeltaTermStructureDataBundle data, final int daysForward) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = instrumentToday.accept(paymentCalculator);
    final SmileDeltaTermStructureDataBundle tomorrowData = FX_OPTION_ROLLDOWN.rollDown(data, shiftTime);
    final PresentValueBlackForexCalculator pvCalculator = PresentValueBlackForexCalculator.getInstance();
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data)).plus(paymentToday);
  }

  public MultipleCurrencyAmount getTheta(final ForexOptionDigitalDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final SmileDeltaTermStructureDataBundle data,
      final PresentValueForexCalculator pvCalculator, final int daysForward) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime horizonDate = date.plusDays(daysForward);
    final double shiftTime = TimeCalculator.getTimeBetween(date, horizonDate);
    final TodayPaymentCalculator paymentCalculator = TodayPaymentCalculator.getInstance(shiftTime);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(horizonDate, yieldCurveNames);
    final MultipleCurrencyAmount paymentToday = instrumentToday.accept(paymentCalculator);
    final SmileDeltaTermStructureDataBundle tomorrowData = FX_OPTION_ROLLDOWN.rollDown(data, shiftTime);
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data)).plus(paymentToday);
  }

  /**
   * Create a new time series with the same data up to tomorrow (tomorrow excluded) and with an extra data for tomorrow equal to the last value in the time series.
   * @param fixingSeries The time series.
   * @param tomorrow Tomorrow date.
   * @return The time series with added data.
   */
  private DoubleTimeSeries<ZonedDateTime>[] getDateShiftedTimeSeries(final DoubleTimeSeries<ZonedDateTime>[] fixingSeries, final ZonedDateTime tomorrow) {
    final int n = fixingSeries.length;
    @SuppressWarnings("unchecked")
    final DoubleTimeSeries<ZonedDateTime>[] laggedFixingSeries = new DoubleTimeSeries[n];
    for (int i = 0; i < n; i++) {
      if (fixingSeries[i].isEmpty()) {
        laggedFixingSeries[i] = ArrayZonedDateTimeDoubleTimeSeries.EMPTY_SERIES;
      } else {
        final DoubleTimeSeries<ZonedDateTime> ts = fixingSeries[i].subSeries(fixingSeries[i].getEarliestTime(), tomorrow);
        final List<ZonedDateTime> times = ts.times();
        final List<Double> values = ts.values();
        final double last = ts.getLatestValue();
        times.add(tomorrow);
        values.add(last);
        laggedFixingSeries[i] = new ListZonedDateTimeDoubleTimeSeries(times, values);
      }
    }
    return laggedFixingSeries;
  }

  private MultipleCurrencyAmount subtract(final MultipleCurrencyAmount a, final MultipleCurrencyAmount b) {
    return a.plus(b.multipliedBy(-1));
  }
}
