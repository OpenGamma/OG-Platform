/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDAInstrumentTypes;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
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
public class ParellelCS01Test {

  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");
  // private static final Calendar DEFAULT_CALENDAR = new NoHolidayCalendar();
  private static final DayCount ACT365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final DayCount D30360 = DayCountFactory.INSTANCE.getDayCount("30/360");

  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final BusinessDayConvention MOD_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");

  private static final LocalDate TODAY = LocalDate.of(2013, 6, 4);
  private static final LocalDate EFFECTIVE_DATE = TODAY.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TODAY, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final LocalDate STARTDATE = LocalDate.of(2013, 2, 2);
  private static final LocalDate[] MATURITIES = new LocalDate[] {LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2013, 12, 20), LocalDate.of(2014, 3, 20),
      LocalDate.of(2014, 6, 20), LocalDate.of(2014, 9, 20), LocalDate.of(2014, 12, 20), LocalDate.of(2015, 3, 20), LocalDate.of(2015, 6, 20), LocalDate.of(2015, 9, 20), LocalDate.of(2015, 12, 20),
      LocalDate.of(2016, 3, 20), LocalDate.of(2016, 6, 20), LocalDate.of(2016, 9, 20), LocalDate.of(2016, 12, 20), LocalDate.of(2017, 3, 20), LocalDate.of(2017, 6, 20), LocalDate.of(2017, 9, 20),
      LocalDate.of(2017, 12, 20), LocalDate.of(2018, 3, 20), LocalDate.of(2018, 6, 20), LocalDate.of(2018, 9, 20), LocalDate.of(2018, 12, 20), LocalDate.of(2019, 3, 20), LocalDate.of(2019, 6, 20),
      LocalDate.of(2019, 9, 20), LocalDate.of(2019, 12, 20), LocalDate.of(2020, 3, 20), LocalDate.of(2020, 6, 20), LocalDate.of(2020, 9, 20), LocalDate.of(2020, 12, 20), LocalDate.of(2021, 3, 20),
      LocalDate.of(2021, 6, 20), LocalDate.of(2021, 9, 20), LocalDate.of(2021, 12, 20), LocalDate.of(2022, 3, 20), LocalDate.of(2022, 6, 20), LocalDate.of(2022, 9, 20), LocalDate.of(2022, 12, 20),
      LocalDate.of(2023, 3, 20), LocalDate.of(2023, 6, 20)};
  private static final double[] FLAT_SPREADS = new double[] {8.97, 9.77, 10.7, 11.96, 13.17, 15.59, 17.8, 19.66, 21.35, 23.91, 26.54, 28.56, 30.63, 32.41, 34.08, 35.33, 36.74, 38.9, 40.88, 42.71,
      44.49, 46.92, 49.2, 51.36, 53.5, 55.58, 57.59, 59.49, 61.4, 62.76, 64.11, 65.35, 66.55, 67.58, 68.81, 69.81, 70.79, 71.65, 72.58, 73.58, 74.2};
 
  //These numbers come from The ISDA excel plugin 
  private static final double[] CS01 = new double[] {4.44275669542324, 30.0292310963296, 55.3868828464654, 80.4711784871091, 106.115178267918, 131.768346646544, 157.154795933765, 182.274611106283,
      207.957754964565, 233.488139351017, 258.616674295747, 283.692918485109, 308.9181603095, 334.009883131127, 358.675097865475, 382.992574496952, 407.651615499056, 431.854306207759,
      455.571341165587, 478.807345690023, 502.320875404118, 525.221886681367, 547.56958798809, 569.366146839778, 591.319055971737, 612.907283712702, 633.900434684163, 654.581372006954,
      675.104344433526, 695.667761357141, 715.649374946677, 735.150221910334, 754.800017771881, 774.265323151377, 792.955421728157, 811.335431749015, 829.837914594809, 848.145638350457,
      865.827578890886, 882.881604614791, 900.543748901181};

  private static final double DEAL_SPREAD = 100.0;
  private static final boolean PAY_ACC_ON_DEFAULT = true;
  private static final Period TENOR = Period.ofMonths(3);
  private static final StubType STUB = StubType.FRONTSHORT;
  private static final boolean PROCTECTION_START = true;

  private static final LocalDate[] PAR_SPREAD_DATES = new LocalDate[] {LocalDate.of(2013, 12, 20), LocalDate.of(2014, 6, 20), LocalDate.of(2015, 6, 20), LocalDate.of(2016, 6, 20),
      LocalDate.of(2017, 6, 20), LocalDate.of(2018, 6, 20), LocalDate.of(2019, 6, 20), LocalDate.of(2020, 6, 20), LocalDate.of(2021, 6, 20), LocalDate.of(2022, 6, 20), LocalDate.of(2023, 6, 20),
      LocalDate.of(2028, 6, 20), LocalDate.of(2033, 6, 20), LocalDate.of(2043, 6, 20)};

  private static final double RECOVERY_RATE = 0.4;
  private static final double NOTIONAL = 1e6;

  // yield curve
  private static final LocalDate SPOT_DATE = LocalDate.of(2013, 6, 6);
  private static final ISDACompliantYieldCurveBuild YIELD_CURVE_BUILDER = new ISDACompliantYieldCurveBuild();
  private static final ISDACompliantYieldCurve YIELD_CURVE;

  static {
    final int nMoneyMarket = 5;
    final int nSwaps = 14;
    final int nInstruments = nMoneyMarket + nSwaps;

    final ISDAInstrumentTypes[] types = new ISDAInstrumentTypes[nInstruments];
    Period[] tenors = new Period[nInstruments];
    final int[] mmMonths = new int[] {1, 2, 3, 6, 12};
    final int[] swapYears = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30};
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

    final double[] rates = new double[] {0.00194, 0.002292, 0.002733, 0.004153, 0.006902, 0.004575, 0.006585, 0.00929, 0.012175, 0.0149, 0.01745, 0.019595, 0.02144, 0.023045, 0.02567, 0.02825,
        0.03041, 0.031425, 0.03202};

    final DayCount moneyMarketDCC = ACT360;
    final DayCount swapDCC = D30360;
    final DayCount curveDCC = ACT365;
    final Period swapInterval = Period.ofMonths(6);

    YIELD_CURVE = YIELD_CURVE_BUILDER.build(TODAY, SPOT_DATE, types, tenors, rates, moneyMarketDCC, swapDCC, swapInterval, curveDCC, MOD_FOLLOWING);

    // YIELD_CURVE = new ISDACompliantYieldCurve(1.0, 0.05);
  }

  @Test
  public void test() {
    SpreadSensitivityCalculator cal = new SpreadSensitivityCalculator();

    final int m = PAR_SPREAD_DATES.length;
    final CDSAnalytic[] curveCDSs = new CDSAnalytic[m];
    for (int i = 0; i < m; i++) {
      curveCDSs[i] = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, TODAY, PAR_SPREAD_DATES[i], PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY_RATE);
    }

    final double fracSpread = DEAL_SPREAD / 10000;

    final int n = MATURITIES.length;
    for (int i = 0; i < n; i++) {
      final CDSAnalytic cds = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, MATURITIES[i], PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY_RATE);
      final double[] flatSpreads = new double[m];
      Arrays.fill(flatSpreads, FLAT_SPREADS[i] / 10000);
      double cs01 = NOTIONAL / 10000 * cal.parallelCreditDV01(cds, fracSpread, PriceType.DIRTY, YIELD_CURVE, curveCDSs, flatSpreads, 1e-4, BumpType.ADDITIVE);
    //  System.out.println(MATURITIES[i].toString() + "\t" + cs01);
      assertEquals(MATURITIES[i].toString(), CS01[i], cs01, 1e-14*NOTIONAL); 
    }

  }
}
