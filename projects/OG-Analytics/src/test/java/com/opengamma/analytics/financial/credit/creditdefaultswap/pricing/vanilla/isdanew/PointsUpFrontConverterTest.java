/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDAInstrumentTypes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class PointsUpFrontConverterTest {

  private static final PointsUpFrontConverter PUF = new PointsUpFrontConverter();
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");

  private static final DayCount ACT365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

  private static final LocalDate TODAY = LocalDate.of(2008, Month.SEPTEMBER, 19);
  private static final LocalDate STEPIN_DATE = TODAY.plusDays(1);
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TODAY, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final LocalDate START_DATE = LocalDate.of(2007, Month.MARCH, 20);
  private static final LocalDate END_DATE = LocalDate.of(2015, Month.DECEMBER, 20);

  private static final LocalDate[] MATURITIES = new LocalDate[] {LocalDate.of(2008, Month.DECEMBER, 20), LocalDate.of(2009, Month.JUNE, 20), LocalDate.of(2010, Month.JUNE, 20),
    LocalDate.of(2011, Month.JUNE, 20), LocalDate.of(2012, Month.JUNE, 20), LocalDate.of(2014, Month.JUNE, 20), LocalDate.of(2017, Month.JUNE, 20) };

  // yield curve
  private static final LocalDate SPOT_DATE = addWorkDays(TODAY, 2, DEFAULT_CALENDAR);
  private static final ISDACompliantYieldCurve YIELD_CURVE;

  static {
    final int nMoneyMarket = 6;
    final int nSwaps = 15;
    final int nInstruments = nMoneyMarket + nSwaps;

    final ISDAInstrumentTypes[] types = new ISDAInstrumentTypes[nInstruments];
    final Period[] tenors = new Period[nInstruments];
    final int[] mmMonths = new int[] {1, 2, 3, 6, 9, 12 };
    final int[] swapYears = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 20, 25, 30 };
    // check
    ArgumentChecker.isTrue(mmMonths.length == nMoneyMarket, "mmMonths");
    ArgumentChecker.isTrue(swapYears.length == nSwaps, "swapYears");

    for (int i = 0; i < nMoneyMarket; i++) {
      types[i] = ISDAInstrumentTypes.MoneyMarket;
      tenors[i] = Period.ofMonths(mmMonths[i]);
    }
    for (int i = nMoneyMarket; i < nInstruments; i++) {
      types[i] = ISDAInstrumentTypes.Swap;
      tenors[i] = Period.ofYears(swapYears[i - nMoneyMarket]);
    }

    final double[] rates = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316, 0.03231,
      0.03367, 0.03419, 0.03411, 0.03412 };

    final DayCount moneyMarketDCC = ACT360;
    final DayCount swapDCC = ACT360;
    final DayCount curveDCC = ACT365;
    final Period swapInterval = Period.ofMonths(6);

    YIELD_CURVE = ISDACompliantYieldCurveBuild.build(TODAY, SPOT_DATE, types, tenors, rates, moneyMarketDCC, swapDCC, swapInterval, curveDCC, FOLLOWING);
  }

  @Test
  public void SingleCDSTest() {
    final Period tenor = Period.ofMonths(3);
    final boolean payAccOnDefault = true;
    final StubType stubType = StubType.FRONTSHORT;
    final boolean protectionStart = true;

    final double pointsUpFront = 0.007;
    final double permium = 100. / 10000;
    final double recovery = 0.4;
    final CDSAnalytic cds = new CDSAnalytic(TODAY, STEPIN_DATE, CASH_SETTLE_DATE, START_DATE, END_DATE, payAccOnDefault, tenor, stubType, protectionStart, recovery);
    final double parSpread = PUF.pufToQuotedSpread(cds, permium, YIELD_CURVE, pointsUpFront);
    final double expectedParSpread = 0.011112592882846; // taken from Excel-ISDA 1.8.2
    assertEquals("Par Spread", expectedParSpread, parSpread, 1e-14);

    final double derivedPUF = PUF.quotedSpreadToPUF(cds, permium, YIELD_CURVE, parSpread);
    assertEquals("PUF", pointsUpFront, derivedPUF, 1e-15);

  }

  @Test
  public void SingleCDSTest2() {
    final Period tenor = Period.ofMonths(6);
    final boolean payAccOnDefault = true;
    final StubType stubType = StubType.FRONTLONG;
    final boolean protectionStart = true;

    final double parSpread = 143.4 / 10000;
    final double permium = 500. / 10000;
    final double recovery = 0.4;
    final CDSAnalytic cds = new CDSAnalytic(TODAY, STEPIN_DATE, CASH_SETTLE_DATE, START_DATE, END_DATE, payAccOnDefault, tenor, stubType, protectionStart, recovery);
    final double puf = PUF.quotedSpreadToPUF(cds, permium, YIELD_CURVE, parSpread);
    final double expectedPUF = -0.2195134271137960; // taken from Excel-ISDA 1.8.2
    assertEquals("PUF", expectedPUF, puf, 5e-13);

    final double derivedParSpread = PUF.pufToQuotedSpread(cds, permium, YIELD_CURVE, puf);
    assertEquals("Par Spread", parSpread, derivedParSpread, 1e-15);

  }

  @Test
  public void MultiCDSTest() {
    final Period tenor = Period.ofMonths(3);
    final boolean payAccOnDefault = true;
    final StubType stubType = StubType.FRONTSHORT;
    final boolean protectionStart = true;
    final double permium = 100. / 10000;
    final double recovery = 0.4;

    final int n = 7;
    final double[] pointsUpFront = {0.007, 0.008, 0.001, 0.0011, 0.0012, 0.0015, 0.0014 };

    // make CDS
    final CDSAnalytic[] cds = new CDSAnalytic[n];
    for (int i = 0; i < n; i++) {
      cds[i] = new CDSAnalytic(TODAY, STEPIN_DATE, CASH_SETTLE_DATE, START_DATE, MATURITIES[i], payAccOnDefault, tenor, stubType, protectionStart, recovery);
    }

    final double[] quotedSpreads = PUF.pufToQuotedSpreads(cds, permium, YIELD_CURVE, pointsUpFront);
    final double[] parSpreads = PUF.pufToParSpreads(cds, permium, YIELD_CURVE, pointsUpFront);

    //    for (int i = 0; i < n; i++) {
    //      System.out.println(quotedSpreads[i] * 10000 + "\t" + parSpreads[i] * 10000);
    //    }

    final double[] derivedPUF = PUF.quotedSpreadsToPUF(cds, permium, YIELD_CURVE, quotedSpreads);
    final double[] derivedPUF2 = PUF.parSpreadsToPUF(cds, permium, YIELD_CURVE, parSpreads);
    for (int i = 0; i < n; i++) {
      assertEquals("PUF1", pointsUpFront[i], derivedPUF[i], 1e-15);
      assertEquals("PUF1", pointsUpFront[i], derivedPUF2[i], 1e-15);
    }

  }
}
