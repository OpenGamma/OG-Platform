/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.List;

import javax.time.calendar.ZonedDateTime;

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
import com.opengamma.analytics.financial.instrument.swap.SwapFixedOISDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;

/**
 * 
 */
public class ConstantSpreadHorizonThetaCalculator {
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVE_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();
  private static final ConstantSpreadSwaptionBlackRolldown SWAPTION_ROLLDOWN = ConstantSpreadSwaptionBlackRolldown.getInstance();
  private static final ConstantSpreadInterestRateFutureOptionBlackDataRolldown IR_FUTURE_OPTION_ROLLDOWN = ConstantSpreadInterestRateFutureOptionBlackDataRolldown.getInstance();
  private static final ConstantSpreadFXBlackRolldown FX_OPTION_ROLLDOWN = ConstantSpreadFXBlackRolldown.getInstance();
  private final TodayPaymentCalculator _paymentCalculator;
  private final double _shiftTime;

  public ConstantSpreadHorizonThetaCalculator(final double daysPerYear) {
    ArgumentChecker.isTrue(daysPerYear > 0, "Number of days per year must be greater than zero; have {}", daysPerYear);
    _shiftTime = 1. / daysPerYear;
    _paymentCalculator = TodayPaymentCalculator.getInstance(_shiftTime);
  }

  public MultipleCurrencyAmount getTheta(final SwapFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveBundle data, final DoubleTimeSeries<ZonedDateTime>[] fixingSeries) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, fixingSeries, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final DoubleTimeSeries<ZonedDateTime>[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, tomorrow);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(tomorrow, shiftedFixingSeries, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = instrumentTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swap");
    }
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, _shiftTime);
    final Currency currency = paymentTomorrow.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final SwapFixedIborSpreadDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveBundle data, final DoubleTimeSeries<ZonedDateTime>[] fixingSeries) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, fixingSeries, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final DoubleTimeSeries<ZonedDateTime>[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, tomorrow);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(tomorrow, shiftedFixingSeries, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = instrumentTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swap");
    }
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, _shiftTime);
    final Currency currency = paymentTomorrow.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final SwapFixedOISDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveBundle data, final DoubleTimeSeries<ZonedDateTime>[] fixingSeries) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, fixingSeries, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final DoubleTimeSeries<ZonedDateTime>[] shiftedFixingSeries = getDateShiftedTimeSeries(fixingSeries, tomorrow);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(tomorrow, shiftedFixingSeries, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = instrumentTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swap");
    }
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, _shiftTime);
    final Currency currency = paymentTomorrow.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final SwaptionPhysicalFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveWithBlackSwaptionBundle data) {
    final SwaptionPhysicalFixedIbor swaptionToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final SwaptionPhysicalFixedIbor swaptionTomorrow = definition.toDerivative(tomorrow, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = swaptionTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1 || !paymentTomorrow.getCurrencyAmounts()[0].getCurrency().equals(definition.getUnderlyingSwap().getCurrency())) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swaption");
    }
    final Currency currency = definition.getUnderlyingSwap().getCurrency();
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final YieldCurveWithBlackSwaptionBundle tomorrowData = SWAPTION_ROLLDOWN.rollDown(data, _shiftTime);
    final double result = swaptionTomorrow.accept(pvCalculator, tomorrowData) - swaptionToday.accept(pvCalculator, data) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final SwaptionCashFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveWithBlackSwaptionBundle data) {
    final SwaptionCashFixedIbor swaptionToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final SwaptionCashFixedIbor swaptionTomorrow = definition.toDerivative(tomorrow, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = swaptionTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1 || !paymentTomorrow.getCurrencyAmounts()[0].getCurrency().equals(definition.getUnderlyingSwap().getCurrency())) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swaption");
    }
    final Currency currency = definition.getUnderlyingSwap().getCurrency();
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final YieldCurveWithBlackSwaptionBundle tomorrowData = SWAPTION_ROLLDOWN.rollDown(data, _shiftTime);
    final double result = swaptionTomorrow.accept(pvCalculator, tomorrowData) - swaptionToday.accept(pvCalculator, data) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final InterestRateFutureDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveBundle data, final Double lastMarginPrice) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, lastMarginPrice, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(tomorrow, lastMarginPrice, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = instrumentTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the interest rate future");
    }
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, _shiftTime);
    final Currency currency = paymentTomorrow.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveWithBlackCubeBundle data, final Double lastMarginPrice) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, lastMarginPrice, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(tomorrow, lastMarginPrice, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = instrumentTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the interest rate future option");
    }
    final YieldCurveWithBlackCubeBundle tomorrowData = IR_FUTURE_OPTION_ROLLDOWN.rollDown(data, _shiftTime);
    final Currency currency = paymentTomorrow.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, tomorrowData) - instrumentToday.accept(pvCalculator, data) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final ForexDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveBundle data) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(tomorrow, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = instrumentTomorrow.accept(_paymentCalculator);
    final YieldCurveBundle tomorrowData = CURVE_ROLLDOWN.rollDown(data, _shiftTime);
    final PresentValueForexCalculator pvCalculator = PresentValueForexCalculator.getInstance();
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data)).plus(paymentTomorrow);
  }

  public MultipleCurrencyAmount getTheta(final ForexOptionVanillaDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final SmileDeltaTermStructureDataBundle data) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(tomorrow, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = instrumentTomorrow.accept(_paymentCalculator);
    final SmileDeltaTermStructureDataBundle tomorrowData = FX_OPTION_ROLLDOWN.rollDown(data, _shiftTime);
    final PresentValueBlackForexCalculator pvCalculator = PresentValueBlackForexCalculator.getInstance();
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data)).plus(paymentTomorrow);
  }

  public MultipleCurrencyAmount getTheta(final ForexOptionDigitalDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final SmileDeltaTermStructureDataBundle data,
      final PresentValueForexCalculator pvCalculator) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(tomorrow, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = instrumentTomorrow.accept(_paymentCalculator);
    final SmileDeltaTermStructureDataBundle tomorrowData = FX_OPTION_ROLLDOWN.rollDown(data, _shiftTime);
    return subtract(instrumentTomorrow.accept(pvCalculator, tomorrowData), instrumentToday.accept(pvCalculator, data)).plus(paymentTomorrow);
  }

  private DoubleTimeSeries<ZonedDateTime>[] getDateShiftedTimeSeries(final DoubleTimeSeries<ZonedDateTime>[] fixingSeries, final ZonedDateTime tomorrow) {
    final int n = fixingSeries.length;
    @SuppressWarnings("unchecked")
    final DoubleTimeSeries<ZonedDateTime>[] laggedFixingSeries = new DoubleTimeSeries[n];
    for (int i = 0; i < n; i++) {
      if (fixingSeries[i].isEmpty()) {
        laggedFixingSeries[i] = ArrayZonedDateTimeDoubleTimeSeries.EMPTY_SERIES;
      } else {
        final List<ZonedDateTime> times = fixingSeries[i].times();
        final List<Double> values = fixingSeries[i].values();
        final double last = fixingSeries[i].getLatestValue();
        times.add(tomorrow);
        values.add(last);
        laggedFixingSeries[i] = new ListZonedDateTimeDoubleTimeSeries(times, values);
      }
    }
    return laggedFixingSeries;
  }

  private MultipleCurrencyAmount subtract(final MultipleCurrencyAmount a, final MultipleCurrencyAmount b) {
    final CurrencyAmount[] currencyAmounts = b.getCurrencyAmounts();
    final CurrencyAmount[] negativeCurrencyAmounts = new CurrencyAmount[currencyAmounts.length];
    for (int i = 0; i < currencyAmounts.length; i++) {
      final CurrencyAmount ca = currencyAmounts[i];
      negativeCurrencyAmounts[i] = CurrencyAmount.of(ca.getCurrency(), -ca.getAmount());
    }
    final MultipleCurrencyAmount negativeB = MultipleCurrencyAmount.of(currencyAmounts);
    return a.plus(negativeB);
  }
}
