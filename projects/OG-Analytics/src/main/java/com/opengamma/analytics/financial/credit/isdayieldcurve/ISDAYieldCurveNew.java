/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdayieldcurve;

import java.util.Arrays;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class ISDAYieldCurveNew {
  private static final long BASIS = 0;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Act/365 (Fixed)");
  private static final double EPS = 3e-16;

  public void buildCurve(final ZonedDateTime valueDate, final Period[] cashTenors, final double[] cashRates, final Period[] swapTenors, final double[] swapRates,
      final DayCount cashDayCount, final PeriodFrequency swapFixedFrequency, final PeriodFrequency swapFloatFrequency, final DayCount swapFixedDayCount,
      final DayCount swapFloatDayCount, final BusinessDayConvention badDayConvention, final Calendar holidays) {
    final int nCashInstruments = cashTenors.length;
    final ZonedDateTime[] cashDates = new ZonedDateTime[nCashInstruments];
    if (nCashInstruments != 0) {
      cashDates[0] = valueDate.plus(cashTenors[0]);
      for (int i = 1; i < nCashInstruments; i++) {
        final ZonedDateTime date = valueDate.plus(cashTenors[i]);
        if (!date.isAfter(cashDates[i - 1]) || !date.isAfter(valueDate)) {
          throw new IllegalArgumentException();
        }
        cashDates[i] = date;
      }
    }
    final TCurve zcurveIni = JpmcdsNewTCurve(valueDate, 1L, DayCountFactory.INSTANCE.getDayCount("Act/365 (Fixed)"));
    final TCurve zCurveCash = JpmcdsZCCash(zcurveIni, cashDates, cashRates, cashDayCount);
    final int nSwapInstruments = swapTenors.length;
    final ZonedDateTime[] swapDates = new ZonedDateTime[nSwapInstruments];
    if (nSwapInstruments != 0) {
      for (int i = 0; i < nSwapInstruments; i++) {
        swapDates[i] = valueDate.plus(swapTenors[i]);
      }
    }
    //    final TCurve zCurveSwap = JpmcdsZCSwaps(zCurveCash, null, swapDates, swapRates, swapFixedFrequency, swapFloatFrequency, swapFixedDayCount, swapFloatDayCount,
    //        '3', temp, badDayConvention, holidays);
    return;
  }

  public TCurve JpmcdsZCCash(final TCurve zeroCurve, final ZonedDateTime[] dates, final double[] rates, final DayCount dayCount) {
    final ZCurve zcStub = JpmcdsZCFromTCurve(zeroCurve);
    ZCurve zcCash = JpmcdsZCMake(zeroCurve.getBaseDate(), 1L, DayCountFactory.INSTANCE.getDayCount("Act/365 (Fixed)"));
    zcCash = JpmcdsZCAddMoneyMarket(zcCash, dates, rates, dayCount);
    ZCurve zCurve = zcCash;
    if (zcStub != null) {
      zCurve = JpmcdsZCAddPrefixCurve(zCurve, zcStub);
      zCurve = JpmcdsZCAddSuffixCurve(zCurve, zcStub);
    }
    return JpmcdsZCToTCurve(zcCash);
  }

  public TCurve JpmcdsZCSwaps(final TCurve zeroCurve, final TCurve discZC, final ZonedDateTime[] dates, final double[] rates, final PeriodFrequency swapFixedFrequency, final PeriodFrequency swapFloatFrequency,
      final DayCount swapFixedDayCount, final DayCount swapFloatDayCount, final char fwdLength, final Object badDayAndStubPos, final BusinessDayConvention badDayConv, final Calendar holiday) {
    if (dates.length == 0) {
      return JpmcdsCopyCurve(zeroCurve);
    }
    final Object stubPos;
    //    if (JpmcdsBadDayAndStubPosSplit(badDayAndStubPos)) {
    //
    //    }
    checkZCSwapsInputs(zeroCurve, discZC, dates, rates, swapFixedFrequency, swapFloatFrequency, holiday);
    final ZCurve zcSwaps = JpmcdsZCFromTCurve(zeroCurve);
    ZonedDateTime lastStubDate;
    if (zeroCurve.getDates().length < 1) {
      lastStubDate = zeroCurve.getBaseDate();
    } else {
      lastStubDate = zeroCurve.getDates()[zeroCurve.getDates().length - 1];
    }
    final int numSwaps = dates.length;
    final int offset = 0;
    //    while (numSwaps > 0 && dates[offset].isBefore(lastStubDate)) {
    //      offset++;
    //      numSwaps--;
    //    }
    final ZonedDateTime[] newDates = new ZonedDateTime[offset];
    final double[] newRates = new double[offset];
    if (numSwaps > 0) {
      //      zcSwaps = JpmcdsZCAddSwaps(zcSwaps, discZC, newDates, newRates, swapFixedFrequency, swapFloatFrequency, swapFixedDayCount, swapFloatDayCount,
      //          124L, null, badDayAndStubPos, holiday);
    }
    final TCurve tcSwaps = JpmcdsZCToTCurve(zcSwaps);
    return tcSwaps;
  }

  public void checkZCSwapsInputs(final TCurve zeroCurve, final TCurve discZC, final ZonedDateTime[] dates, final double[] rates, final PeriodFrequency swapFixedFrequency, final PeriodFrequency swapFloatFrequency,
      final Calendar holiday) {
    if (zeroCurve == null) {
      throw new IllegalArgumentException();
    }
    final long fixedPeriod = swapFixedFrequency.getPeriod().toTotalMonths();
    if (12 % fixedPeriod != 0) {
      throw new IllegalArgumentException();
    }
    final long floatPeriod = swapFloatFrequency.getPeriod().toTotalMonths();
    if (12 % floatPeriod != 0) {
      throw new IllegalArgumentException();
    }
    for (int i = 0; i < dates.length; i++) {
      if (rates[i] <= -1) {
        throw new IllegalArgumentException();
      }
      if (i > 0 && !dates[i].isAfter(dates[i - 1])) {
        throw new IllegalArgumentException();
      }
      if (dates[i].isBefore(zeroCurve.getBaseDate())) {
        throw new IllegalArgumentException();
      }
    }
    if (!holiday.isWorkingDay(zeroCurve.getBaseDate().toLocalDate())) {
      throw new IllegalArgumentException();
    }
  }

  public ZCurve JpmcdsZCFromTCurve(final TCurve tCurve) {
    ZCurve zCurve = JpmcdsZCMake(tCurve.getBaseDate(), tCurve.getBasis(), tCurve.getDayCount());
    final ZonedDateTime[] dates = tCurve.getDates();
    final double[] rates = tCurve.getRates();
    final int n = dates.length;
    for (int i = 0; i < n; i++) {
      zCurve = JpmcdsZCAddRate(dates[i], rates[i], zCurve);
    }
    return zCurve;
  }

  public TCurve JpmcdsZCToTCurve(final ZCurve zCurve) {
    final ZonedDateTime[] dates = zCurve.getDates();
    final double[] rates = zCurve.getRates();
    final TCurve curve = JpmcdsMakeTCurveNoRateCheck(zCurve.getValueDate(), dates, rates, zCurve.getBasis(), zCurve.getDayCount());
    checkTCurve(curve);
    return curve;
  }

  public ZCurve JpmcdsZCAddMoneyMarket(final ZCurve curve, final ZonedDateTime[] dates, final double[] rates, final DayCount dayCount) {
    if (dates.length == 0) {
      return null;
    }
    ZCurve newCurve = JpmcdsZCCopy(curve);
    for (int i = 0; i < dates.length; i++) {
      newCurve = JpmcdsZCAddGenRate(dates[i], rates[i], curve.getBasis(), dayCount, newCurve);
    }
    return newCurve;
  }

  public ZCurve JpmcdsZCMake(final ZonedDateTime valueDate, final long basis, final DayCount dayCount) {
    return new ZCurve(valueDate, new double[0], new ZonedDateTime[0], new double[0], basis, dayCount);
  }

  public TCurve JpmcdsNewTCurve(final ZonedDateTime baseDate, final long basis, final DayCount dayCount) {
    return new TCurve(baseDate, new double[0], new ZonedDateTime[0], basis, dayCount);
  }

  public ZCurve JpmcdsZCCopy(final ZCurve zCurve) {
    return new ZCurve(zCurve.getValueDate(), zCurve.getRates(), zCurve.getDates(), zCurve.getDiscountFactors(), zCurve.getBasis(), zCurve.getDayCount());
  }

  public TCurve JpmcdsCopyCurve(final TCurve tCurve) {
    return new TCurve(tCurve.getBaseDate(), tCurve.getRates(), tCurve.getDates(), tCurve.getBasis(), tCurve.getDayCount());
  }

  public ZCurve JpmcdsZCAddRateAndDiscount(final ZonedDateTime date, final double rate, final double discount, final ZCurve zCurve) {
    final ZonedDateTime[] dates = zCurve.getDates();
    final double[] rates = zCurve.getRates();
    final double[] discountFactors = zCurve.getDiscountFactors();
    final int index = JpmcdsZCFindDateExact(date, dates, rate, rates);
    if (index >= 0) {
      return new ZCurve(zCurve.getValueDate(), rates, dates, zCurve.getDiscountFactors(), zCurve.getBasis(), zCurve.getDayCount());
    }
    final int upperIndex = -index;
    final int n = dates.length;
    final ZonedDateTime[] newDates = new ZonedDateTime[n + 1];
    final double[] newRates = new double[n + 1];
    final double[] newDiscountFactors = new double[n + 1];
    for (int i = 0; i < upperIndex; i++) {
      newDates[i] = dates[i];
      newRates[i] = rates[i];
      newDiscountFactors[i] = discountFactors[i];
    }
    newDates[upperIndex] = date;
    newRates[upperIndex] = rate;
    newDiscountFactors[upperIndex] = discount;
    for (int i = upperIndex + 1; i < n + 1; i++) {
      newDates[i] = dates[i - 1];
      newRates[i] = rates[i - 1];
      newDiscountFactors[i] = discountFactors[i - 1];
    }
    return new ZCurve(zCurve.getValueDate(), newRates, newDates, newDiscountFactors, zCurve.getBasis(), zCurve.getDayCount());
  }

  public ZCurve JpmcdsZCAddRate(final ZonedDateTime date, final double rate, final ZCurve zCurve) {
    final ZonedDateTime[] dates = zCurve.getDates();
    final double[] rates = zCurve.getRates();
    final double[] discountFactors = zCurve.getDiscountFactors();
    final int index = JpmcdsZCFindDateExact(date, dates, rate, rates);
    if (index >= 0) {
      return new ZCurve(zCurve.getValueDate(), rates, dates, zCurve.getDiscountFactors(), zCurve.getBasis(), zCurve.getDayCount());
    }
    final double discount = JpmcdsZCComputeDiscount(zCurve.getValueDate(), zCurve.getBasis(), zCurve.getDayCount(), date, rate);
    final int upperIndex = -index;
    final int n = dates.length;
    final ZonedDateTime[] newDates = new ZonedDateTime[n + 1];
    final double[] newRates = new double[n + 1];
    final double[] newDiscountFactors = new double[n + 1];
    for (int i = 0; i < upperIndex; i++) {
      newDates[i] = dates[i];
      newRates[i] = rates[i];
      newDiscountFactors[i] = discountFactors[i];
    }
    newDates[upperIndex] = date;
    newRates[upperIndex] = rate;
    newDiscountFactors[upperIndex] = discount;
    for (int i = upperIndex + 1; i < n + 1; i++) {
      newDates[i] = dates[i - 1];
      newRates[i] = rates[i - 1];
      newDiscountFactors[i] = discountFactors[i - 1];
    }
    return new ZCurve(zCurve.getValueDate(), newRates, newDates, newDiscountFactors, zCurve.getBasis(), zCurve.getDayCount());
  }

  public ZCurve JpmcdsZCAddGenRate(final ZonedDateTime date, final double rate, final long basis, final DayCount dayCount, final ZCurve zCurve) {
    if (basis == zCurve.getBasis() && dayCount.getConventionName().equals(zCurve.getDayCount().getConventionName())) {
      return JpmcdsZCAddRate(date, rate, zCurve);
    }
    final double discount = JpmcdsRateToDiscount(rate, zCurve.getValueDate(), date, dayCount, basis);
    return JpmcdsZCAddDiscountFactor(date, rate, discount, zCurve);
  }

  public ZCurve JpmcdsZCAddDiscountFactor(final ZonedDateTime date, final double rate, final double discount, final ZCurve zCurve) {
    return JpmcdsZCAddRateAndDiscount(date, rate, discount, zCurve);
  }

  public ZCurve JpmcdsZCAddDiscountFactor(final ZonedDateTime date, final double discount, final ZCurve zc) {
    final double rate = JpmcdsDiscountToRate(discount, zc.getValueDate(), date, zc.getDayCount(), zc.getBasis());
    final int n = zc.getDates().length;
    final ZonedDateTime[] dates = new ZonedDateTime[n + 1];
    final double[] rates = new double[n + 1];
    final double[] discountFactors = new double[n + 1];
    System.arraycopy(zc.getDates(), 0, dates, 0, n);
    System.arraycopy(zc.getRates(), 0, rates, 0, n);
    System.arraycopy(zc.getDiscountFactors(), 0, discountFactors, 0, n);
    dates[n] = date;
    rates[n] = rate;
    discountFactors[n] = discount;
    return new ZCurve(zc.getValueDate(), rates, dates, discountFactors, zc.getBasis(), zc.getDayCount());
  }

  public int JpmcdsZCFindDateExact(final ZonedDateTime date, final ZonedDateTime[] dates, final double rate, final double[] rates) {
    final int index = Arrays.binarySearch(dates, date);
    if (index >= 0) {
      if (Math.abs(rate - rates[index]) >= 1e-7) {
        throw new IllegalArgumentException("Trying to add new rate for date " + date);
      }
    }
    return index;
  }

  public double JpmcdsZCComputeDiscount(final ZonedDateTime valueDate, final long basis, final DayCount dayCount, final ZonedDateTime date, final double rate) {
    if (basis == 1 && rate >= -1 && !date.isBefore(valueDate) &&
        (dayCount.getConventionName().equals("Actual/365") || dayCount.getConventionName().equals("Actual/360"))) {
      return Math.pow(1 + rate, dayCount.getDayCountFraction(valueDate, date));
    }
    //JpmcdsRateToDiscount
    if (basis == -2L) {
      if (rate <= 0) {
        throw new IllegalArgumentException();
      }
      return rate;
    } else if (basis < 0L) {
      throw new IllegalArgumentException();
    }
    final double yearFraction = dayCount.getDayCountFraction(valueDate, date);
    double discount;
    if (basis == 0) {
      final double denom = 1 + rate * yearFraction;
      if (denom <= 0 || Double.compare(denom, 0) == 0) {
        discount = 0;
      } else {
        discount = 1 / denom;
      }
    } else if (basis == 512L) {
      if (Double.compare(yearFraction, 0) == 0) {
        discount = 1;
      } else {
        discount = 1 - rate * yearFraction;
        if (discount <= 0) {
          discount = 0;
        }
      }
    } else if (basis == 5000L) {
      discount = Math.exp(-rate * yearFraction);
    } else if (basis == -2L) {
      discount = rate;
    } else {
      final double temp = 1 + rate / basis;
      if (temp <= 0 || Double.compare(temp, 0) == 0) {
        discount = 0;
      }
      discount = Math.pow(temp, -basis * yearFraction);
    }
    return discount;
  }

  public double JpmcdsRateToDiscount(final double rate, final ZonedDateTime startDate, final ZonedDateTime endDate, final DayCount dayCount, final long basis) {
    if (basis == -2L) {
      if (rate <= 0) {
        throw new IllegalArgumentException();
      }
      return rate;
    } else if (basis < 0L) {
      throw new IllegalArgumentException();
    }
    final double rateYF = dayCount.getDayCountFraction(startDate, endDate);
    double discount;
    if (basis == 0) {
      final double denom = 1 + rate * rateYF;
      if (denom <= 0 || Double.compare(denom, 0) == 0) {
        discount = 0;
      } else {
        discount = 1 / denom;
      }
    } else if (basis == 512L) {
      if (Double.compare(rateYF, 0) == 0) {
        discount = 1;
      } else {
        discount = 1 - rate * rateYF;
        if (discount <= 0) {
          discount = 0;
        }
      }
    } else if (basis == 5000L) {
      discount = Math.exp(-rate * rateYF);
    } else if (basis == -2L) {
      discount = rate;
    } else {
      final double temp = 1 + rate / basis;
      if (temp <= 0 || Double.compare(temp, 0) == 0) {
        discount = 0;
      }
      discount = Math.pow(temp, -basis * rateYF);
    }
    return discount;
  }

  public double JpmcdsDiscountToRate(final double discount, final ZonedDateTime startDate, final ZonedDateTime endDate, final DayCount dayCount, final long basis) {
    if (discount <= 0) {
      throw new IllegalArgumentException();
    }
    if (basis == -2L) {
      if (startDate.equals(endDate)) {
        if (!CompareUtils.closeEquals(1, discount, EPS)) {
          throw new IllegalArgumentException();
        }
        return 1;
      }
      return discount;
    }
    if (startDate.equals(endDate)) {
      throw new IllegalArgumentException();
    }
    if (basis < 0) {
      throw new IllegalArgumentException();
    }
    final double rateYF = dayCount.getDayCountFraction(startDate, endDate);
    return JpmcdsDiscountToRateYearFrac(discount, rateYF, basis);
  }

  public double JpmcdsDiscountToRateYearFrac(final double discount, final double yearFraction, final long basis) {
    if (discount <= 0) {
      throw new IllegalArgumentException();
    }
    if (CompareUtils.closeEquals(yearFraction, 0, EPS)) {
      throw new IllegalArgumentException();
    }
    if (basis == 0L) {
      return (1 / discount - 1) / yearFraction;
    }
    if (basis == 512L) {
      return (1 - discount) / yearFraction;
    }
    if (basis == 5000L) {
      return -Math.log(discount) / yearFraction;
    }
    if (basis == -2L) {
      return discount;
    }
    return basis * (Math.pow(discount,  -1 / (basis * yearFraction)) - 1);
  }

  public ZCurve JpmcdsZCAddPrefixCurve(final ZCurve curve1, final ZCurve curve2) {
    ZonedDateTime firstDate;
    final ZonedDateTime[] dates1 = curve1.getDates();
    final ZonedDateTime[] dates2 = curve2.getDates();
    if (dates1.length == 0) {
      if (dates2.length == 0) {
        return JpmcdsZCMake(curve1.getValueDate(), curve1.getBasis(), curve1.getDayCount());
      }
      firstDate = dates2[dates2.length - 1];
    } else {
      firstDate = dates1[0];
    }
    final double[] rates2 = curve2.getRates();
    ZCurve newCurve = JpmcdsZCCopy(curve1);
    for (int i = 0; i < dates2.length && dates2[i].isBefore(firstDate); i++) {
      newCurve = JpmcdsZCAddGenRate(dates2[i], rates2[i], curve2.getBasis(), curve2.getDayCount(), newCurve);
    }
    return newCurve;
  }

  public ZCurve JpmcdsZCAddSuffixCurve(final ZCurve curve1, final ZCurve curve2) {
    ZonedDateTime lastDate;
    final ZonedDateTime[] dates1 = curve1.getDates();
    final ZonedDateTime[] dates2 = curve2.getDates();
    final double[] rates2 = curve2.getRates();
    if (dates1.length == 0) {
      if (dates2.length == 0) {
        return JpmcdsZCMake(curve1.getValueDate(), curve1.getBasis(), curve1.getDayCount());
      }
      lastDate = dates2[0];
    } else {
      lastDate = dates1[dates1.length - 1];
    }
    ZCurve newCurve = JpmcdsZCCopy(curve1);
    for (int i = dates2.length - 1; i >= 0 && dates2[i].isAfter(lastDate); i--) {
      newCurve = JpmcdsZCAddGenRate(dates2[i], rates2[i], curve2.getBasis(), curve2.getDayCount(), newCurve);
    }
    return newCurve;
  }

  public TCurve JpmcdsMakeTCurveNoRateCheck(final ZonedDateTime baseDate, final ZonedDateTime[] dates, final double[] rates, final long basis, final DayCount dayCount) {
    for (int i = 0; i < dates.length; i++) {
      if (i > 0 && !dates[i - 1].isBefore(dates[i])) {
        throw new IllegalArgumentException();
      }
    }
    return new TCurve(baseDate, rates, dates, basis, dayCount);
  }

  public void checkTCurve(final TCurve tc) {
    if (tc == null) {
      throw new IllegalArgumentException();
    }
    if (tc.getBasis() < -2L) {
      throw new IllegalArgumentException();
    }
    final ZonedDateTime[] dates = tc.getDates();
    final double[] rates = tc.getRates();
    for (int i = 0; i < dates.length; i++) {
      if (i > 0 && !dates[i - 1].isBefore(dates[i])) {
        throw new IllegalArgumentException();
      }
      if (!JpmcdsRateValid(rates[i], tc.getBaseDate(), dates[i], tc.getDayCount(), tc.getBasis())) {
        throw new IllegalArgumentException();
      }
    }
  }

  public boolean JpmcdsRateValid(final double rate, final ZonedDateTime startDate, final ZonedDateTime endDate, final DayCount dayCount, final long basis) {
    double yearFraction;
    if (basis == 0L || basis == 512L) {
      yearFraction = dayCount.getDayCountFraction(startDate, endDate);
    } else {
      yearFraction = 1;
    }
    return JpmcdsRateValidYearFrac(rate, yearFraction, basis);
  }

  public boolean JpmcdsRateValidYearFrac(final double rate, final double yearFraction, final long basis) {
    if (basis == 0L) {
      return rate * yearFraction > -1;
    } else if (basis == 512L) {
      return rate * yearFraction < 1;
    } else if (basis == 5000L) {
      return true;
    } else if (basis == -2L) {
      return rate > 0;
    }
    return rate > -basis;
  }

  public ZCurve JpmcdsZCAddSwaps(final ZCurve zc, final TCurve discZC, final ZonedDateTime[] inDates, final double[] inRates, final PeriodFrequency swapFixedFrequency, final PeriodFrequency swapFloatFrequency,
      final DayCount swapFixedDayCount, final DayCount swapFloatDayCount, final BusinessDayConvention badDayConv, final long interpType, /*final TInterpData interpData,*/ final Calendar holiday) {
    final int n = inDates.length;
    final ZonedDateTime valueDate = zc.getValueDate();
    final ZonedDateTime[] adjusted = new ZonedDateTime[n];
    final ZonedDateTime[] previous = new ZonedDateTime[n];
    final ZonedDateTime[] original = new ZonedDateTime[n];
    final boolean[] onCycle = new boolean[n];
    for (int i = 0; i < n; i++) {
      original[i] = inDates[i];
      adjusted[i] = JpmcdsZCAdjustDate(inDates[i], badDayConv, holiday);
      final boolean isOnCycle = isOnCycle(valueDate, inDates[i]);
      onCycle[i] = isOnCycle;
      //      TDateInterval interval;
      //      if (isOnCycle) {
      //        interval = JpmcdsDateFromDateAndOffset(valueDate, numIntervals - 1, previousDate);
      //      } else {
      //        interval = JpmcdsDateFromDateAndOffset(originalDate, -1, previousDate);
      //      }
    }
    final double[] swapRates = inRates;
    final boolean oneAlreadyAdded = false;
    for (int i = 0; i < inDates.length; i++) {
      if (adjusted[i].isAfter(zc.getDates()[zc.getDates().length - 1])) {
        if (oneAlreadyAdded &&
            discZC == null &&
            inRates[i - 1] != 0 &&
            adjusted[i - 1].equals(zc.getDates()[zc.getDates().length - 1]) &&
            previous[i].equals(original[i - 1]) &&
            onCycle[i] &&
            interpType != 123L) {
          AddSwapFromPrevious(zc, adjusted[i], swapRates[i], 1, adjusted[i - 1], swapRates[i - 1], 1, swapFixedDayCount);
        } else {
          //          JpmcdsZCAddSwap(zc, discZC, 1, original[i], onCycle[i], swapRates[i], swapFixedFrequency, swapFloatFrequency,
          //              swapFixedDayCount, swapFloatDayCount, interpType, interpData, badDayAndStubConv, holiday);
          //          oneAlreadyAdded = true;
        }
      }
    }
    return null;
  }

  public ZonedDateTime JpmcdsZCAdjustDate(final ZonedDateTime date, final BusinessDayConvention badDayConv, final Calendar calendar) {
    return badDayConv.adjustDate(calendar, date);
  }

  public boolean isOnCycle(final ZonedDateTime valueDate, final ZonedDateTime date) {
    if (valueDate.getDayOfMonth() <= 28 && date.getDayOfMonth() <= 28) {
      //final int extraDays = JpmcdsCountDates();
      //return extraDays == 0;
    }
    return false;
  }

  public ZCurve AddSwapFromPrevious(final ZCurve zc, final ZonedDateTime dateNew, final double rateNew, final double priceNew, final ZonedDateTime dateOld, final double rateOld,
      final double priceOld, final DayCount dayCount) {
    final double yf = dayCount.getDayCountFraction(dateOld, dateNew);
    final double divisor = 1 + rateNew * yf;
    if (Math.abs(divisor) < 3e-16) {
      throw new IllegalArgumentException();
    }
    final double sumDY = (priceOld - zc.getDiscountFactors()[zc.getDiscountFactors().length - 1]) / rateOld;
    final double discount = (priceNew - rateNew * sumDY) / divisor;
    if (discount <= 0) {
      throw new IllegalArgumentException();
    }
    return JpmcdsZCAddDiscountFactor(dateNew, discount, zc);
  }

  //  public ZCurve JpmcdsZCAddSwap(final ZCurve zc, final TCurve discZC, final double price, final ZonedDateTime maturityDate, final boolean onCycle, final double rate,
  //      final PeriodFrequency swapFixedFrequency, final PeriodFrequency swapFloatFrequency, final DayCount swapFixedDayCount, final DayCount swapFloatDayCount,
  //      final long interpType, final TInterpData interpData, final TBadDayAndStubPos badDayAndStubPos, final Calendar holiday) {
  //
  //  }
}
