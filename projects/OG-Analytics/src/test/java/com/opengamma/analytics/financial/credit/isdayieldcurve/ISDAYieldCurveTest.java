/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdayieldcurve;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class ISDAYieldCurveTest {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Daycount conventions

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_365F = DayCountFactory.INSTANCE.getDayCount("ACT/365F");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final ZonedDateTime valuationDate = DateUtils.getUTCDate(2013, 1, 30);

  private static final int spotDays = 0;

  private static final ISDAInstrumentTypes[] instrumentTypes = {
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.MoneyMarket,
      ISDAInstrumentTypes.Swap,
      ISDAInstrumentTypes.Swap /*,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap,
                               ISDAInstrumentTypes.Swap*/

  };

  private static final ISDAYieldCurveTenors[] instrumentTenors = {
      ISDAYieldCurveTenors._1M,
      ISDAYieldCurveTenors._2M,
      ISDAYieldCurveTenors._3M,
      ISDAYieldCurveTenors._6M,
      ISDAYieldCurveTenors._9M,
      ISDAYieldCurveTenors._1Y,
      ISDAYieldCurveTenors._2Y,
      ISDAYieldCurveTenors._3Y /*,
                               ISDAYieldCurveTenors._4Y,
                               ISDAYieldCurveTenors._5Y,
                               ISDAYieldCurveTenors._6Y,
                               ISDAYieldCurveTenors._7Y,
                               ISDAYieldCurveTenors._8Y,
                               ISDAYieldCurveTenors._9Y,
                               ISDAYieldCurveTenors._10Y,
                               ISDAYieldCurveTenors._12Y,
                               ISDAYieldCurveTenors._15Y,
                               ISDAYieldCurveTenors._20Y,
                               ISDAYieldCurveTenors._25Y,
                               ISDAYieldCurveTenors._30Y*/
  };

  private static final double[] instrumentRates = {0.002017, 0.002465, 0.003005, 0.004758, 0.006428, 0.007955, 0.0043949999999999996, 0.00582 /*, 0.0, 0.0, 0.0*/};

  /*
  private static final double flatRate = 0.2;
  private static final double[] instrumentRates = {flatRate, flatRate, flatRate, flatRate, flatRate, flatRate, flatRate, flatRate, flatRate, flatRate, flatRate, flatRate, flatRate, flatRate,
      flatRate, flatRate, flatRate, flatRate, flatRate, flatRate }; */

  private static final Calendar calendar = new MondayToFridayCalendar("TestCalendar");
  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final PeriodFrequency swapFixedLegCouponFrequency = PeriodFrequency.SEMI_ANNUAL;
  private static final PeriodFrequency swapFloatingLegCouponFrequency = PeriodFrequency.QUARTERLY;

  private static final DayCount moneyMarketDaycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");          // MM DCC
  private static final DayCount swapFixedLegDaycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("30/360");          // Swap DCC
  private static final DayCount swapFloatingLegDaycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");      // Swap DCC

  private static final ISDAYieldCurve isdaYieldCurve = new ISDAYieldCurve(
      valuationDate,
      instrumentTenors,
      instrumentTypes,
      instrumentRates,
      spotDays,
      moneyMarketDaycountFractionConvention,
      swapFixedLegDaycountFractionConvention,
      swapFloatingLegDaycountFractionConvention,
      swapFixedLegCouponFrequency,
      swapFloatingLegCouponFrequency,
      businessdayAdjustmentConvention/*,
                                     calendar*/);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO :

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Test
  public void testISDAYieldCurve() {

    final int numberOfInstruments = isdaYieldCurve.getNumberOfInstruments();
    final int numberOfCashInstruments = isdaYieldCurve.getNumberOfCashInstruments();
    final int numberOfSwapInstruments = isdaYieldCurve.getNumberOfSwapInstruments();

    for (int i = 0; i < numberOfInstruments; i++) {
      //System.out.println(i + "\t" + isdaYieldCurve.getYieldCurveDates()[i]);
    }

    //System.out.println();

    for (int i = 0; i < numberOfCashInstruments; i++) {
      //System.out.println(i + "\t" + isdaYieldCurve.getCashDates()[i] + "\t" + isdaYieldCurve.getCashRates()[i]);
    }

    //System.out.println();

    for (int i = 0; i < numberOfSwapInstruments; i++) {
      //System.out.println(i + "\t" + isdaYieldCurve.getSwapDates()[i] + "\t" + isdaYieldCurve.getSwapRates()[i]);
    }

    /*
    for (long i = 0; i < 3000; i++)
    {
      ZonedDateTime testDate = valuationDate.plusDays(i);

      final double Z = isdaYieldCurve.getDiscountFactor(valuationDate, testDate);

      System.out.println("i = " + "\t" + i + "\t" + testDate + "\t" + Z);
    }
    */

  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static ZonedDateTime zdt(int y, int m, int d, int hr, int min, int sec, int nanos, ZoneId zone) {
    return LocalDateTime.of(y, m, d, hr, min, sec, nanos).atZone(zone);
  }

}
