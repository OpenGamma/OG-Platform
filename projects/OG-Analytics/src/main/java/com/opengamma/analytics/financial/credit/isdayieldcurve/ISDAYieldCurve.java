/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdayieldcurve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;

/**
 * 
 */
public class ISDAYieldCurve {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add hashcode and equals methods
  // TODO : Add argument checkers
  // TODO : Check that nCash, nSwap != 0
  // TODO : _basis is hard coded - replace this
  // TODO : Add the swap code
  // TODO : Check the efficiacy of the input data

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final int _numberOfInstruments;
  private final int _numberOfCashInstruments;
  private final int _numberOfSwapInstruments;

  private final int _spotDays;

  private final ZonedDateTime _baseDate;

  private final ZonedDateTime[] _zCurveDates;

  private final double[] _zCurveRates;

  private final double[] _zCurveCCRates;

  private final double _basis = 1.0;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public ISDAYieldCurve(
      final ZonedDateTime baseDate,
      final ZonedDateTime[] instrumentTenors, //final ISDAYieldCurveTenors[] instrumentTenors,
      final ISDAInstrumentTypes[] instrumentTypes,
      final double[] instrumentRates,
      final int spotDays,
      final DayCount moneyMarketDaycountConvention,
      final DayCount swapDaycountDaycountConvention,
      final DayCount floatDaycountConvention,
      final PeriodFrequency swapInterval,
      final PeriodFrequency floatInterval,
      final BusinessDayConvention badDayConvention,
      final Calendar holidayCalendar) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Add argument checkers

    // Modify the baseDate to take into account spotDays (bda)

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _baseDate = baseDate;

    _numberOfInstruments = instrumentTenors.length;

    _spotDays = spotDays;

    _zCurveDates = new ZonedDateTime[_numberOfInstruments];
    _zCurveRates = new double[_numberOfInstruments];
    _zCurveCCRates = new double[_numberOfInstruments];

    _numberOfSwapInstruments = getNumberOfInstruments(instrumentTypes, ISDAInstrumentTypes.Swap);
    _numberOfCashInstruments = getNumberOfInstruments(instrumentTypes, ISDAInstrumentTypes.MoneyMarket);

    final ZonedDateTime[] swapDates = getInstrumentDates(instrumentTenors, instrumentTypes, ISDAInstrumentTypes.Swap, _numberOfSwapInstruments);
    final ZonedDateTime[] cashDates = getInstrumentDates(instrumentTenors, instrumentTypes, ISDAInstrumentTypes.MoneyMarket, _numberOfCashInstruments);

    final double[] swapRates = getInstrumentRates(instrumentRates, instrumentTypes, ISDAInstrumentTypes.Swap, _numberOfSwapInstruments);
    final double[] cashRates = getInstrumentRates(instrumentRates, instrumentTypes, ISDAInstrumentTypes.MoneyMarket, _numberOfCashInstruments);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the cash instrument discount factors
    for (int i = 0; i < _numberOfCashInstruments; i++) {

      final double dcf = TimeCalculator.getTimeBetween(baseDate, cashDates[i], moneyMarketDaycountConvention);
      final double dcf2 = TimeCalculator.getTimeBetween(baseDate, cashDates[i], ACT_365);
      final double discount = 1.0 / (1.0 + dcf * cashRates[i]);

      _zCurveDates[i] = cashDates[i];
      _zCurveRates[i] = cashRates[i];
      _zCurveCCRates[i] = Math.pow(discount, -1.0 / (_basis * dcf2)) - 1.0;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ZonedDateTime lastStubDate;

    if (_numberOfCashInstruments < 1) {
      lastStubDate = _baseDate;
    } else {
      lastStubDate = cashDates[_numberOfCashInstruments - 1];
    }

    // TODO : Add code to ensure number of active swaps is > 0 (JpmcdsZCSwaps)

    for (int i = 0; i < _numberOfSwapInstruments; i++) {
      _zCurveDates[i + _numberOfCashInstruments] = swapDates[i];
      _zCurveRates[i + _numberOfCashInstruments] = swapRates[i];
      _zCurveCCRates[i + _numberOfCashInstruments] = 0.0;
    }

    // Fixed swap interval is 6M (2 payments per year)
    final double fixedSwapFreq = 2.0;

    // Float swap interval is 3M (4 payments per year)
    final double floatSwapFreq = 4.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    /*
    for (int i = 0; i < _numberOfInstruments; i++) {
      System.out.println(i + "\t" + _zCurveDates[i] + "\t" + _zCurveRates[i] + "\t" + _zCurveCCRates[i]);
    }
    */

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  public ZonedDateTime[] getZCurveDates() {
    return _zCurveDates;
  }

  public double[] getZCurveRates() {
    return _zCurveRates;
  }

  public double[] getZCurveCCRates() {
    return _zCurveCCRates;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private int getNumberOfInstruments(
      ISDAInstrumentTypes[] rateTypes,
      ISDAInstrumentTypes rateType) {

    int nInstruments = 0;

    for (int i = 0; i < rateTypes.length; i++) {

      if (rateTypes[i] == rateType) {
        nInstruments++;
      }

    }

    return nInstruments;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private ZonedDateTime[] getInstrumentDates(
      ZonedDateTime[] instrumentMaturities,
      ISDAInstrumentTypes[] rateTypes,
      ISDAInstrumentTypes rateType,
      final int nInstruments) {

    int index = 0;

    ZonedDateTime[] instrumentDates = new ZonedDateTime[nInstruments];

    for (int i = 0; i < rateTypes.length; i++) {

      if (rateTypes[i] == rateType) {
        instrumentDates[index] = instrumentMaturities[i];
        index++;
      }

    }

    return instrumentDates;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double[] getInstrumentRates(
      double[] instrumentRates,
      ISDAInstrumentTypes[] rateTypes,
      ISDAInstrumentTypes rateType,
      final int nInstruments) {

    int index = 0;

    double[] instrumentInputRates = new double[nInstruments];

    for (int i = 0; i < rateTypes.length; i++) {

      if (rateTypes[i] == rateType) {
        instrumentInputRates[index] = instrumentRates[i];
        index++;
      }

    }

    return instrumentInputRates;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final double getInterpolatedRate(
      final double t,
      final double t1,
      final double t2,
      final double z1,
      final double z2) {

    final double z1t1 = z1 * t1;
    final double z2t2 = z2 * t2;

    final double zt = z1t1 + (z2t2 - z1t1) * (t - t1) / (t2 - t1);

    return zt;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public final double getDiscountFactor(final ZonedDateTime valueDate, final ZonedDateTime date) {

    ZonedDateTime loDate;
    ZonedDateTime hiDate;

    double loRate = 0.0;
    double hiRate = 0.0;

    double z1 = 0.0;
    double z2 = 0.0;

    double t1 = 0.0;
    double t2 = 0.0;
    double t = 0.0;
    double Z = 0.0;
    double rate = 0.0;

    // Extrapolation below the first date
    if (date.isBefore(_zCurveDates[0])) {
      loRate = _zCurveCCRates[0];
      z1 = Math.log(1.0 + loRate);
      t = TimeCalculator.getTimeBetween(valueDate, date, ACT_365);
      rate = z1;
      Z = Math.exp(-rate * t);
    }

    // Extrapolation beyond the last data
    if (!date.isBefore(_zCurveDates[_numberOfInstruments - 1])) {

      int lo = _numberOfInstruments - 2;
      int hi = _numberOfInstruments - 1;

      t1 = TimeCalculator.getTimeBetween(valueDate, _zCurveDates[lo], ACT_360);
      t2 = TimeCalculator.getTimeBetween(valueDate, _zCurveDates[hi], ACT_360);
      t = TimeCalculator.getTimeBetween(valueDate, date, ACT_360);

      loRate = _zCurveCCRates[lo];
      hiRate = _zCurveCCRates[hi];

      z1 = Math.log(1.0 + loRate);
      z2 = Math.log(1.0 + hiRate);

      // TODO : DoubleToBits this
      if (t == 0.0)
      {

        // TODO : Check for t2 == 0 as well
        t = 1.0 / 365.0;
      }

      final double zt = getInterpolatedRate(t, t1, t2, z1, z2);
      rate = zt / t;

      t = TimeCalculator.getTimeBetween(valueDate, date, ACT_365);

      Z = Math.exp(-rate * t);
    }

    // Interpolation
    if (!date.isBefore(_zCurveDates[0]) && date.isBefore(_zCurveDates[_numberOfInstruments - 1]))
    {
      // ... testDate is within the window spanned by the input dates

      int lo = 0;

      // Start at the first date
      ZonedDateTime rollingDate = _zCurveDates[0];

      while (!rollingDate.isAfter(date))
      {
        lo++;
        rollingDate = _zCurveDates[lo];
      }

      int hi = lo + 1;

      loDate = _zCurveDates[lo - 1];
      hiDate = _zCurveDates[hi - 1];

      loRate = _zCurveCCRates[lo - 1];
      hiRate = _zCurveCCRates[hi - 1];

      z1 = Math.log(1.0 + loRate);
      z2 = Math.log(1.0 + hiRate);

      t1 = TimeCalculator.getTimeBetween(valueDate, loDate, ACT_360);
      t2 = TimeCalculator.getTimeBetween(valueDate, hiDate, ACT_360);
      t = TimeCalculator.getTimeBetween(valueDate, date, ACT_360);

      // TODO : DoubleToBits this
      if (t == 0.0)
      {

        // TODO : Check for t2 == 0 as well
        t = 1.0 / 365.0;
      }

      final double zt = getInterpolatedRate(t, t1, t2, z1, z2);
      rate = zt / t;

      t = TimeCalculator.getTimeBetween(valueDate, date, ACT_365);

      Z = Math.exp(-rate * t);
    }

    final double discountFactor = Z;

    return discountFactor;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
