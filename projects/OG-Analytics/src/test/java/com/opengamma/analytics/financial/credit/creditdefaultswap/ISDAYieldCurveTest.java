/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAInterestRateTypes;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;

/**
 * 
 */
public class ISDAYieldCurveTest {

  // ----------------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // ----------------------------------------------------------------------------------------------

  final ZonedDateTime baseDate2 = ZonedDateTime.of(2012, 11, 15, 0, 0, 0, 0, TimeZone.UTC);

  final ZonedDateTime offsetBaseDate = ZonedDateTime.of(2012, 11, 19, 0, 0, 0, 0, TimeZone.UTC);

  ZonedDateTime[] dates = {
      ZonedDateTime.of(2012, 12, 17, 0, 0, 0, 0, TimeZone.UTC),   // MM
      ZonedDateTime.of(2013, 1, 15, 0, 0, 0, 0, TimeZone.UTC),    // MM
      ZonedDateTime.of(2013, 2, 15, 0, 0, 0, 0, TimeZone.UTC),    // MM
      ZonedDateTime.of(2013, 5, 15, 0, 0, 0, 0, TimeZone.UTC),    // MM
      ZonedDateTime.of(2013, 8, 15, 0, 0, 0, 0, TimeZone.UTC),    // MM
      ZonedDateTime.of(2014, 11, 16, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2015, 11, 15, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2016, 11, 15, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2017, 11, 15, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2018, 11, 15, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2019, 11, 17, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2020, 11, 15, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2021, 11, 15, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2022, 11, 15, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2024, 11, 17, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2027, 11, 15, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2032, 11, 15, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2037, 11, 15, 0, 0, 0, 0, TimeZone.UTC),
      ZonedDateTime.of(2042, 11, 16, 0, 0, 0, 0, TimeZone.UTC)
  };

  double[] times = {
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2012, 12, 16, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2013, 1, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2013, 2, 17, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2013, 5, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2013, 8, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2014, 11, 16, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2015, 11, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2016, 11, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2017, 11, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2018, 11, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2019, 11, 17, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2020, 11, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2021, 11, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2022, 11, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2024, 11, 17, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2027, 11, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2032, 11, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2037, 11, 15, 0, 0, 0, 0, TimeZone.UTC)),
      ACT_365.getDayCountFraction(baseDate2, ZonedDateTime.of(2042, 11, 16, 0, 0, 0, 0, TimeZone.UTC))
  };

  double[] rates = {
      0.002075,
      0.00257,
      0.00310999999999,
      0.00523,
      0.0069649999999,
      0.00377,
      0.00451,
      0.005834,
      0.007625,
      0.009617,
      0.011546,
      0.01329,
      0.01488,
      0.016383,
      0.018786,
      0.02122,
      0.023181,
      0.024195,
      0.02481
  };

  ISDAInterestRateTypes[] rateTypes = {
      ISDAInterestRateTypes.MoneyMarket,
      ISDAInterestRateTypes.MoneyMarket,
      ISDAInterestRateTypes.MoneyMarket,
      ISDAInterestRateTypes.MoneyMarket,
      ISDAInterestRateTypes.MoneyMarket,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap,
      ISDAInterestRateTypes.Swap
  };

  // ----------------------------------------------------------------------------------------------

  private static final int spotDays = 2;

  private static final Calendar calendar = new MondayToFridayCalendar("TestCalendar");    // Holidays (None)

  private static final PeriodFrequency depositCouponFrequency = PeriodFrequency.QUARTERLY;    // 
  private static final PeriodFrequency swapCouponFrequency = PeriodFrequency.QUARTERLY;

  private static final DayCount depositDaycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("ACT/360");    // mm DCC
  private static final DayCount swapDaycountFractionConvention = DayCountFactory.INSTANCE.getDayCount("30/360");        // swap DCC

  private static final BusinessDayConvention businessdayAdjustmentConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"); // Bad Day Conv

  // ----------------------------------------------------------------------------------------------

  //@Test
  public void testISDAYieldCurveBuild() {

    // Need to check the input data (numpts > o, dates in ascending order etc)
    // None of the dates should be < baseDate

    // Need to error check for negative rates?

    // JPMCDSClearExceldateSystem

    // The number of instruments
    int nInstr = times.length;

    ZonedDateTime valueDate = baseDate2;

    DayCount mmDCC = depositDaycountFractionConvention;

    PeriodFrequency fixedIvl = PeriodFrequency.SEMI_ANNUAL;
    PeriodFrequency floatIvl = PeriodFrequency.QUARTERLY;

    DayCount fixedSwapDCC = DayCountFactory.INSTANCE.getDayCount("30/360");
    DayCount floatSwapDCC = DayCountFactory.INSTANCE.getDayCount("ACT/360");

    mmDCC = depositDaycountFractionConvention;

    double fixedSwapFreq = 2.0;
    double floatSwapFreq = 4.0;

    int nSwap = 0;
    int nCash = 0;

    for (int i = 0; i < nInstr; i++) {

      if (rateTypes[i] == ISDAInterestRateTypes.MoneyMarket) {
        nCash++;
      }

      if (rateTypes[i] == ISDAInterestRateTypes.Swap) {
        nSwap++;
      }
    }

    ZonedDateTime[] swapDates = new ZonedDateTime[nSwap];
    ZonedDateTime[] cashDates = new ZonedDateTime[nCash];

    double[] swapRates = new double[nSwap];
    double[] cashRates = new double[nCash];

    double[] cashDF = new double[nCash];
    double[] swapDF = new double[nSwap];

    nSwap = 0;
    nCash = 0;

    for (int i = 0; i < nInstr; i++) {

      if (rateTypes[i] == ISDAInterestRateTypes.MoneyMarket) {
        cashDates[nCash] = dates[i];
        cashRates[nCash] = rates[i];
        nCash++;
      }

      if (rateTypes[i] == ISDAInterestRateTypes.Swap) {
        swapDates[nSwap] = dates[i];
        swapRates[nSwap] = rates[i];
        nSwap++;
      }
    }

    for (int i = 0; i < nCash; i++) {
      System.out.println("MM" + "\t" + cashDates[i] + "\t" + cashRates[i]);
    }

    for (int i = 0; i < nSwap; i++) {
      System.out.println("Swap" + "\t" + swapDates[i] + "\t" + swapRates[i]);
    }

    double[] zCurve = new double[nInstr];

    for (int i = 0; i < nCash; i++) {
      double dcf = TimeCalculator.getTimeBetween(baseDate2, cashDates[i], mmDCC);

      double denom = 1.0 + dcf * cashRates[i];

      double discount = 1.0 / denom;

      System.out.println("i = " + i + "\t" + baseDate2 + "\t" + cashDates[i] + "\t" + cashRates[i] + "\t" + dcf + "\t" + discount);
    }

    // bda the money market dates
    /*
    ZonedDateTime baseDate = valueDate;
    for (int i = 0; i < nInstr; i++) {

      if (rateTypes[i] == ISDAInterestRateTypes.MoneyMarket) {

        if (times[i] <= 3.0) {
          // Need to fill this is
        }
        else if (times[i] <= 21.0) {
          // Need to fill this in
        }
        else {
        }
      }
    }*/

  }

}
