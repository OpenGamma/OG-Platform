/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.List;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedOISDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
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
    final YieldCurveBundle tomorrowData = getDateShiftedYieldCurveBundle(data);
    final Currency currency = paymentTomorrow.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, data) - instrumentToday.accept(pvCalculator, tomorrowData) + paymentTomorrow.getAmount(currency);
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
    final YieldCurveBundle tomorrowData = getDateShiftedYieldCurveBundle(data);
    final Currency currency = paymentTomorrow.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, data) - instrumentToday.accept(pvCalculator, tomorrowData) + paymentTomorrow.getAmount(currency);
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
    final YieldCurveBundle tomorrowData = getDateShiftedYieldCurveBundle(data);
    final Currency currency = paymentTomorrow.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, data) - instrumentToday.accept(pvCalculator, tomorrowData) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final SwaptionPhysicalFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveWithBlackSwaptionBundle data) {
    final SwaptionPhysicalFixedIbor swaptionToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final SwaptionPhysicalFixedIbor swaptionTomorrow = definition.toDerivative(tomorrow, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = swaptionTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1 || !paymentTomorrow.getCurrencyAmounts()[0].getCurrency().equals(definition.getUnderlyingSwap().getCurrency())) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swap");
    }
    final Currency currency = definition.getUnderlyingSwap().getCurrency();
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final double result = swaptionTomorrow.accept(pvCalculator, data) - swaptionToday.accept(pvCalculator, data) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final SwaptionCashFixedIborDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames, final YieldCurveWithBlackSwaptionBundle data) {
    final SwaptionCashFixedIbor swaptionToday = definition.toDerivative(date, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final SwaptionCashFixedIbor swaptionTomorrow = definition.toDerivative(tomorrow, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = swaptionTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1 || !paymentTomorrow.getCurrencyAmounts()[0].getCurrency().equals(definition.getUnderlyingSwap().getCurrency())) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swap");
    }
    final Currency currency = definition.getUnderlyingSwap().getCurrency();
    final PresentValueBlackCalculator pvCalculator = PresentValueBlackCalculator.getInstance();
    final double result = swaptionTomorrow.accept(pvCalculator, data) - swaptionToday.accept(pvCalculator, data) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final InterestRateFutureDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveBundle data, final Double lastMarginPrice) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, lastMarginPrice, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(tomorrow, lastMarginPrice, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = instrumentTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swap");
    }
    final YieldCurveBundle tomorrowData = getDateShiftedYieldCurveBundle(data);
    final Currency currency = paymentTomorrow.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, data) - instrumentToday.accept(pvCalculator, tomorrowData) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
  }

  public MultipleCurrencyAmount getTheta(final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime date, final String[] yieldCurveNames,
      final YieldCurveBundle data, final Double lastMarginPrice) {
    final InstrumentDerivative instrumentToday = definition.toDerivative(date, lastMarginPrice, yieldCurveNames);
    final ZonedDateTime tomorrow = date.plusDays(1);
    final InstrumentDerivative instrumentTomorrow = definition.toDerivative(tomorrow, lastMarginPrice, yieldCurveNames);
    final MultipleCurrencyAmount paymentTomorrow = instrumentTomorrow.accept(_paymentCalculator);
    if (paymentTomorrow.size() != 1) {
      throw new IllegalStateException("Expecting a single payment in the currency of the swap");
    }
    final YieldCurveBundle tomorrowData = getDateShiftedYieldCurveBundle(data);
    final Currency currency = paymentTomorrow.getCurrencyAmounts()[0].getCurrency(); //TODO assuming that currencies are all the same
    final PresentValueCalculator pvCalculator = PresentValueCalculator.getInstance();
    final double result = instrumentTomorrow.accept(pvCalculator, data) - instrumentToday.accept(pvCalculator, tomorrowData) + paymentTomorrow.getAmount(currency);
    return MultipleCurrencyAmount.of(CurrencyAmount.of(currency, result));
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

  private YieldCurveBundle getDateShiftedYieldCurveBundle(final YieldCurveBundle bundle) {
    final YieldCurveBundle shifted = new YieldCurveBundle();
    for (final String name : bundle.getAllNames()) {
      shifted.setCurve(name, getDateShiftedYieldCurve(bundle.getCurve(name)));
    }
    return shifted;
  }

  private YieldAndDiscountCurve getDateShiftedYieldCurve(final YieldAndDiscountCurve yieldCurve) {
    final Curve<Double, Double> curve = yieldCurve.getCurve();
    final Function1D<Double, Double> shiftedFunction = new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double t) {
        return curve.getYValue(t + _shiftTime);
      }

    };
    return new YieldCurve(FunctionalDoublesCurve.from(shiftedFunction));
  }
}
