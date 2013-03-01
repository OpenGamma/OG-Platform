/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdayieldcurve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.businessday.ModifiedFollowingBusinessDayConvention;
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

  private static final BusinessDayConvention cashBusDayConv = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("MF");

  private static final GenerateCreditDefaultSwapPremiumLegSchedule businessDayAdjuster = new GenerateCreditDefaultSwapPremiumLegSchedule();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add hashcode and equals methods
  // TODO : Add argument checkers
  // TODO : Check that nCash, nSwap != 0
  // TODO : _basis is hard coded - replace this
  // TODO : Add the swap code
  // TODO : Check the efficiacy of the input data

  // TODO : Need to bda the baseDate

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final int _numberOfInstruments;
  private final int _numberOfCashInstruments;
  private final int _numberOfSwapInstruments;

  // This is the valuation date of the trade
  private final ZonedDateTime _valuationDate;

  // The base date is the trade valuation date + spot days (on this date the discount factor is 1)
  private final ZonedDateTime _baseDate;

  private final int _spotDays;

  private final ZonedDateTime[] _yieldCurveDates;

  private final ZonedDateTime[] _cashDates;
  private final ZonedDateTime[] _swapDates;

  private final double[] _cashRates;
  private final double[] _swapRates;

  private final ZonedDateTime[] _zCurveDates;
  private final double[] _zCurveRates;
  private final double[] _zCurveCCRates;

  private final double _basis = 1.0;

  // Fixed swap interval is 6M (2 payments per year)
  private final double _fixedFreq = 2.0;

  // Float swap interval is 3M (4 payments per year)
  private final double _floatFreq = 4.0;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public ISDAYieldCurve(
      final ZonedDateTime valuationDate,
      final ISDAYieldCurveTenors[] instrumentTenors,
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

    // Add argument checkers (num instruments != 0, swap tenors in ascending order, rates >= -100%, tenors after baseDate etc)

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _valuationDate = valuationDate;

    _spotDays = spotDays;

    _baseDate = valuationDate.plusDays(_spotDays);

    _numberOfInstruments = instrumentTenors.length;

    _yieldCurveDates = calculateInstrumentDates(_baseDate, instrumentTypes, instrumentTenors, holidayCalendar);

    _numberOfSwapInstruments = getNumberOfInstrumentsOfSpecificType(instrumentTypes, ISDAInstrumentTypes.Swap);
    _numberOfCashInstruments = getNumberOfInstrumentsOfSpecificType(instrumentTypes, ISDAInstrumentTypes.MoneyMarket);

    _cashDates = getInstrumentDates(_yieldCurveDates, instrumentTypes, ISDAInstrumentTypes.MoneyMarket, _numberOfCashInstruments);
    _cashRates = getInstrumentRates(instrumentRates, instrumentTypes, ISDAInstrumentTypes.MoneyMarket, _numberOfCashInstruments);

    _swapDates = getInstrumentDates(_yieldCurveDates, instrumentTypes, ISDAInstrumentTypes.Swap, _numberOfSwapInstruments);
    _swapRates = getInstrumentRates(instrumentRates, instrumentTypes, ISDAInstrumentTypes.Swap, _numberOfSwapInstruments);

    _zCurveDates = new ZonedDateTime[_numberOfInstruments];
    _zCurveRates = new double[_numberOfInstruments];
    _zCurveCCRates = new double[_numberOfInstruments];

    double[] zcCashDates = new double[_numberOfCashInstruments + 2];
    double[] zcCashRates = new double[_numberOfCashInstruments + 2];
    double[] zcCashDiscount = new double[_numberOfCashInstruments + 2];

    for (int i = 0; i < _numberOfCashInstruments; i++) {

      // TODO : Check if rate <= 0

      ZonedDateTime startDate = _baseDate;
      ZonedDateTime endDate = _cashDates[i];

      final double rateYF = TimeCalculator.getTimeBetween(startDate, endDate, ACT_360);
      final double denom = 1.0 + _cashRates[i] * rateYF;

      // TODO : Check denom != 0

      zcCashDiscount[i] = 1.0 / denom;

      final double rateYF2 = TimeCalculator.getTimeBetween(startDate, endDate, ACT_365);

      zcCashRates[i] = Math.pow(zcCashDiscount[i], -1.0 / (_basis * rateYF2)) - 1.0;

      _zCurveDates[i] = _cashDates[i];
      _zCurveRates[i] = _cashRates[i];
      _zCurveCCRates[i] = zcCashRates[i];
    }

    ZonedDateTime lastStubDate;

    if (_numberOfCashInstruments < 1) {
      lastStubDate = _baseDate;
    } else {
      lastStubDate = _cashDates[_numberOfCashInstruments - 1];
    }

    int offset = 0;

    int numSwaps = _numberOfSwapInstruments;

    // TODO : Remember to check this logic when some of the swap tenors lie before the end of the deposit tenors
    while (numSwaps > 0 && _swapDates[offset].isBefore(lastStubDate)) {
      offset++;
      numSwaps--;
    }

    // TODO : Remember to place the following code within this loop - not important for the moment
    if (numSwaps > 0) {

    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    /*
    ZonedDateTime lastStubDate;

    if (_numberOfCashInstruments < 1) {
      lastStubDate = _baseDate;
    } else {
      //lastStubDate = cashDates[_numberOfCashInstruments - 1];
    }

    // TODO : Add code to ensure number of active swaps is > 0 (JpmcdsZCSwaps)

    for (int i = 0; i < _numberOfSwapInstruments; i++) {
      //_zCurveDates[i + _numberOfCashInstruments] = swapDates[i];
      //_zCurveRates[i + _numberOfCashInstruments] = swapRates[i];
      //_zCurveCCRates[i + _numberOfCashInstruments] = 0.0;
    }


    */

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  private ZonedDateTime[] calculateInstrumentDates(
      final ZonedDateTime baseDate,
      final ISDAInstrumentTypes[] instrumentTypes,
      final ISDAYieldCurveTenors[] yieldCurveTenors,
      final Calendar calendar) {

    ZonedDateTime[] yieldCurveDates = new ZonedDateTime[yieldCurveTenors.length];
    ZonedDateTime[] bdaYieldCurveDates = new ZonedDateTime[yieldCurveTenors.length];

    ModifiedFollowingBusinessDayConvention moneyMarketBadDayConv = new ModifiedFollowingBusinessDayConvention();

    for (int i = 0; i < yieldCurveTenors.length; i++) {

      // This is very hacky - there must be a better way of doing this probably a switch (ask EM)
      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._1M) {
        yieldCurveDates[i] = baseDate.plusMonths(1);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._2M) {
        yieldCurveDates[i] = baseDate.plusMonths(2);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._3M) {
        yieldCurveDates[i] = baseDate.plusMonths(3);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._6M) {
        yieldCurveDates[i] = baseDate.plusMonths(6);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._9M) {
        yieldCurveDates[i] = baseDate.plusMonths(9);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._1Y) {
        yieldCurveDates[i] = baseDate.plusMonths(12);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._2Y) {
        yieldCurveDates[i] = baseDate.plusYears(2);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._3Y) {
        yieldCurveDates[i] = baseDate.plusYears(3);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._4Y) {
        yieldCurveDates[i] = baseDate.plusYears(4);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._5Y) {
        yieldCurveDates[i] = baseDate.plusYears(5);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._6Y) {
        yieldCurveDates[i] = baseDate.plusYears(6);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._7Y) {
        yieldCurveDates[i] = baseDate.plusYears(7);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._8Y) {
        yieldCurveDates[i] = baseDate.plusYears(8);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._9Y) {
        yieldCurveDates[i] = baseDate.plusYears(9);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._10Y) {
        yieldCurveDates[i] = baseDate.plusYears(10);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._12Y) {
        yieldCurveDates[i] = baseDate.plusYears(12);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._15Y) {
        yieldCurveDates[i] = baseDate.plusYears(15);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._20Y) {
        yieldCurveDates[i] = baseDate.plusYears(20);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._25Y) {
        yieldCurveDates[i] = baseDate.plusYears(25);
      }

      if (yieldCurveTenors[i] == ISDAYieldCurveTenors._30Y) {
        yieldCurveDates[i] = baseDate.plusYears(30);
      }

      bdaYieldCurveDates[i] = yieldCurveDates[i];

      // If the instrument is cash, then business day adjust this
      if (instrumentTypes[i] == ISDAInstrumentTypes.MoneyMarket) {
        bdaYieldCurveDates[i] = moneyMarketBadDayConv.adjustDate(calendar, yieldCurveDates[i]);
      }

    }

    return bdaYieldCurveDates;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public ZonedDateTime[] getCashDates() {
    return _cashDates;
  }

  public ZonedDateTime[] getSwapDates() {
    return _swapDates;
  }

  public double[] getCashRates() {
    return _cashRates;
  }

  public double[] getSwapRates() {
    return _swapRates;
  }

  public ZonedDateTime getBaseDate() {
    return _baseDate;
  }

  public ZonedDateTime[] getYieldCurveDates() {
    return _yieldCurveDates;
  }

  public int getNumberOfInstruments() {
    return _numberOfInstruments;
  }

  public int getNumberOfCashInstruments() {
    return _numberOfCashInstruments;
  }

  public int getNumberOfSwapInstruments() {
    return _numberOfSwapInstruments;
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

  private int getNumberOfInstrumentsOfSpecificType(
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

  private static int getNumberOfActiveSwaps(
      ZonedDateTime lastStubDate,
      ZonedDateTime[] swapDates,
      final int nSwap) {

    int offset = 0;

    int numSwaps = nSwap;

    while (numSwaps > 0 && swapDates[offset].isBefore(lastStubDate)) {
      offset++;
      numSwaps--;
    }

    return numSwaps;
  }

  boolean calculateOnCycle(
      ZonedDateTime valueDate,
      ZonedDateTime unadjustedSwapDate,
      PeriodFrequency swapFixedLegCouponFrequency) {

    boolean onCycle = false;

    if (valueDate.getDayOfMonth() <= 28 && unadjustedSwapDate.getDayOfMonth() <= 28) {

      ZonedDateTime fromDate = valueDate;
      ZonedDateTime toDate = unadjustedSwapDate;

      double intervalYears = 0.0;
      double fromToYears = TimeCalculator.getTimeBetween(fromDate, toDate, ACT_365);

      // Need to fix this - bit of a hack
      if (swapFixedLegCouponFrequency == PeriodFrequency.SEMI_ANNUAL) {
        intervalYears = 0.5;
      }

      if (swapFixedLegCouponFrequency == PeriodFrequency.QUARTERLY) {
        intervalYears = 0.25;
      }

      int lowNumIntervals = Math.max(0, (int) Math.floor(Math.abs(fromToYears / intervalYears)) - 2);
      int index = lowNumIntervals;

      int compoundInterval = 0;
      int multiplier = 0;

      // Need to fix this - bit of a hack
      if (swapFixedLegCouponFrequency == PeriodFrequency.SEMI_ANNUAL) {
        multiplier = 6;
        compoundInterval = index * multiplier;
      }

      if (swapFixedLegCouponFrequency == PeriodFrequency.QUARTERLY) {
        multiplier = 3;
        compoundInterval = index * multiplier;
      }

      ZonedDateTime currDate = valueDate.plusMonths(compoundInterval);
      ZonedDateTime lastDate = currDate;

      while (currDate.isAfter(fromDate) && !currDate.isAfter(toDate)) {
        ++index;
        lastDate = currDate;
        currDate = valueDate.plusMonths(index * multiplier);
      }

      int numIntervals = index - 1;
      int extraDays = (int) Math.abs(TimeCalculator.getTimeBetween(toDate, lastDate));

      if (extraDays == 0) {
        onCycle = true;
      }
    }

    return onCycle;
  }

  //Assuming that the inputs are ordered as MM followed by swap instruments

  private void jpmCDSZCSwaps(
      ZonedDateTime valueDate,
      ZonedDateTime[] cashDates,
      ZonedDateTime[] swapDates,
      double[] swapRates,
      int numSwaps,
      double fixedSwapFreq,
      double floatSwapFreq,
      DayCount swapFixedLegDaycountFractionConvention,
      DayCount swapFloatingLegDaycountFractionConvention,
      BusinessDayConvention businessdayAdjustmentConvention,
      Calendar calendar) {

    if (numSwaps == 0) {
      return;
    }

    GenerateCreditDefaultSwapPremiumLegSchedule swapMaturities = new GenerateCreditDefaultSwapPremiumLegSchedule();

    ZonedDateTime lastStubDate = cashDates[cashDates.length - 1];

    // Need to implement this if want to have swaps with maturities less than MM instruments
    // numSwaps = getNumberOfActiveSwaps(lastStubDate, swapDates, numSwaps);

    ZonedDateTime[] unadjustedSwapDates = new ZonedDateTime[swapDates.length];
    ZonedDateTime[] adjustedSwapDates = new ZonedDateTime[swapDates.length];

    boolean[] onCycleSwapDates = new boolean[swapDates.length];
    ZonedDateTime[] previousSwapDates = new ZonedDateTime[swapDates.length];

    for (int i = 0; i < numSwaps; i++) {
      unadjustedSwapDates[i] = swapDates[i];
      adjustedSwapDates[i] = swapMaturities.businessDayAdjustDate(swapDates[i], calendar, businessdayAdjustmentConvention);
    }

    int numIntervals = 0;
    int extraDays = 0;

    int compoundInterval = 0;
    int multiplier = 0;

    double intervalYears = 0.0;

    // Need to fix this - bit of a hack
    /*if (swapFixedLegCouponFrequency == PeriodFrequency.SEMI_ANNUAL)*/{
      intervalYears = 0.5;
      multiplier = 6;
    }

    /*if (swapFixedLegCouponFrequency == PeriodFrequency.QUARTERLY)*/{
      intervalYears = 0.25;
      multiplier = 3;
    }

    // -------------------------------------------

    for (int i = 0; i < numSwaps; i++) {

      boolean onCycle = false;

      if (valueDate.getDayOfMonth() <= 28 && unadjustedSwapDates[i].getDayOfMonth() <= 28) {

        ZonedDateTime fromDate = valueDate;
        ZonedDateTime toDate = unadjustedSwapDates[i];

        double fromToYears = TimeCalculator.getTimeBetween(fromDate, toDate, ACT_365);

        int lowNumIntervals = Math.max(0, (int) Math.floor(Math.abs(fromToYears / intervalYears)) - 2);
        int index = lowNumIntervals;

        compoundInterval = index * multiplier;

        ZonedDateTime currDate = fromDate.plusMonths(compoundInterval);
        ZonedDateTime lastDate = currDate;

        while (currDate.isAfter(fromDate) && !currDate.isAfter(toDate)) {
          ++index;
          lastDate = currDate;
          currDate = valueDate.plusMonths(index * multiplier);
        }

        numIntervals = index - 1;
        extraDays = (int) Math.abs(TimeCalculator.getTimeBetween(toDate, lastDate));

        if (extraDays == 0) {
          onCycle = true;
        } // end if extraDays 

      } // end if dom <= 28

      onCycleSwapDates[i] = onCycle;

      ZonedDateTime prevDate;

      if (onCycleSwapDates[i]) {
        prevDate = valueDate.plusMonths(multiplier * (numIntervals - 1));
      }
      else {
        prevDate = unadjustedSwapDates[i].plusMonths(6 * (-1));
      }

      previousSwapDates[i] = prevDate;

    } // end loop over i

    /*
    for (int i = 0; i < numSwaps; i++) {
      System.out.println("i = " + i + "\t" + unadjustedSwapDates[i] + "\t" + adjustedSwapDates[i] + "\t" + onCycleSwapDates[i] + "\t" + previousSwapDates[i]);
    }
     */

    // -------------------------------------------

    boolean oneAlreadyAdded = false;

    boolean isEndStub = false;

    int numDates;

    for (int i = 0; i < numSwaps; i++) {

      if (adjustedSwapDates[i].isAfter(cashDates[cashDates.length - 1])) {

        if (onCycleSwapDates[i]) {
          isEndStub = true;
        }
        else {
          // Need to fill this in - jpmcdsisendstub
        } // end if

        // need to add rate = 0 case

        if (isEndStub) {

          ZonedDateTime fromDate = valueDate;
          ZonedDateTime toDate = unadjustedSwapDates[i];

          double fromToYears = TimeCalculator.getTimeBetween(fromDate, toDate, ACT_365);

          int lowNumIntervals = Math.max(0, (int) Math.floor(Math.abs(fromToYears / intervalYears)) - 2);
          int index = lowNumIntervals;

          compoundInterval = index * multiplier;

          ZonedDateTime currDate = fromDate.plusMonths(compoundInterval);
          ZonedDateTime lastDate = currDate;

          while (currDate.isAfter(fromDate) && !currDate.isAfter(toDate)) {
            ++index;
            lastDate = currDate;
            currDate = valueDate.plusMonths(index * multiplier);
          }

          numIntervals = index - 1;
          extraDays = (int) Math.abs(TimeCalculator.getTimeBetween(toDate, lastDate));

        }
        else {
          // Need to add this
        }

        if (extraDays > 0) {
          numDates = numIntervals + 2;
        }
        else {
          numDates = numIntervals + 1;
        }

        ZonedDateTime[] dateList = new ZonedDateTime[numDates];

        if (isEndStub) {

          for (int j = 0; j < numDates - 1; j++) {
            dateList[j] = valueDate.plusMonths(j * multiplier);
          }

          dateList[numDates - 1] = unadjustedSwapDates[i];

        }
        else {
          // Need to add this
        }

        for (int j = 0; j < numDates - 1; j++) {
          dateList[j] = dateList[j + 1];
        }
        numDates--;

        //

        ZonedDateTime[] adjustedDateList = new ZonedDateTime[numDates];

        for (int idx = 0; idx < numDates; idx++) {
          adjustedDateList[idx] = swapMaturities.businessDayAdjustDate(dateList[idx], calendar, businessdayAdjustmentConvention);
        }

        double[] cashflowList = new double[numDates];

        ZonedDateTime prevDate = valueDate;

        for (int idx = 0; idx < numDates; idx++) {

          ZonedDateTime cDate = adjustedDateList[idx];

          double dcf = ACT_360.getDayCountFraction(prevDate, cDate);

          cashflowList[idx] = dcf * swapRates[i];

          prevDate = cDate;
        }

        cashflowList[numDates - 1] += 1.0;

        ZonedDateTime adjMatDate = adjustedSwapDates[i];

        double price = 1.0;

        //ZCAddCashFlowList(adjustedDateList, cashflowList, price, adjMatDate);

      } // end if adjdate >cashdate

    } // end loop over i

  }

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

    // ---------------------------------------------

    // Extrapolation below the first date
    if (date.isBefore(_zCurveDates[0])) {
      loRate = _zCurveCCRates[0];
      z1 = Math.log(1.0 + loRate);
      t = TimeCalculator.getTimeBetween(valueDate, date, ACT_365);
      rate = z1;
      Z = Math.exp(-rate * t);
    }

    // ---------------------------------------------

    // Extrapolation beyond the last date
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

    // ---------------------------------------------

    // Interpolation
    if (!date.isBefore(_zCurveDates[0]) && date.isBefore(_zCurveDates[_numberOfInstruments - 1]))
    {
      // ... date is within the window spanned by the input dates

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

    // ---------------------------------------------

    final double discountFactor = Z;

    return discountFactor;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
