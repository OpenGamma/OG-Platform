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
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 * 
 */
public class SpreadSensitivityTest {

  // private static final ISDACompliantCreditCurveBuild BUILDER = new ISDACompliantCreditCurveBuild();
  private static final ISDACompliantCreditCurveBuilder BUILDER = new FastCreditCurveBuilder();
  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();
  private static final SpreadSensitivityCalculator CDV01_CAL = new SpreadSensitivityCalculator();
  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");

  // common data
  private static final LocalDate TODAY = LocalDate.of(2013, 4, 21);
  private static final LocalDate EFFECTIVE_DATE = TODAY.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TODAY, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final double RECOVERY_RATE = 0.4;
  private static final double NOTIONAL = 1e7;

  // valuation CDS
  private static final LocalDate PROTECTION_STATE_DATE = LocalDate.of(2013, 2, 3); // Seasoned CDS
  private static final LocalDate PROTECTION_END_DATE = LocalDate.of(2018, 3, 20);
  private static final double DEAL_SPREAD = 101;
  private static final CDSAnalytic CDS;

  // market CDSs
  private static final LocalDate[] PAR_SPD_DATES = new LocalDate[] {LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2014, 3, 20), LocalDate.of(2015, 3, 20),
    LocalDate.of(2016, 3, 20), LocalDate.of(2018, 3, 20), LocalDate.of(2023, 3, 20) };
  private static final double[] PAR_SPREADS = new double[] {50, 70, 80, 95, 100, 95, 80 };
  private static final int NUM_MARKET_CDS = PAR_SPD_DATES.length;
  private static final CDSAnalytic[] MARKET_CDS = new CDSAnalytic[NUM_MARKET_CDS];

  // yield curve
  private static ISDACompliantYieldCurve YIELD_CURVE;

  static {
    final double flatrate = 0.05;
    final double t = 20.0;
    YIELD_CURVE = new ISDACompliantYieldCurve(new double[] {t }, new double[] {flatrate });

    final boolean payAccOndefault = true;
    final Period tenor = Period.ofMonths(3);
    final StubType stubType = StubType.FRONTSHORT;
    final boolean protectionStart = true;

    CDS = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, PROTECTION_STATE_DATE, PROTECTION_END_DATE, payAccOndefault, tenor, stubType, protectionStart, RECOVERY_RATE);

    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      MARKET_CDS[i] = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, TODAY, PAR_SPD_DATES[i], payAccOndefault, tenor, stubType, protectionStart, RECOVERY_RATE);
    }
  }

  @Test
  public void parellelCreditDVO1Test() {
    final double fromExcel = 4238.557409;

    final double dealSpread = DEAL_SPREAD / 10000;
    final double[] mrkSpreads = new double[NUM_MARKET_CDS];
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] / 10000;
    }

    final double cdv01 = NOTIONAL / 10000 * CDV01_CAL.parallelCS01FromParSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mrkSpreads, 1e-4, BumpType.ADDITIVE);
    // System.out.println(cdv01);
    assertEquals("", fromExcel, cdv01, 1e-13 * NOTIONAL);
  }

  @Test(enabled = false)
  public void creditCurveTest() {
    final double[] mrkSpreads = new double[1];
    for (int i = 0; i < 1; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] / 10000.;
    }
    final CDSAnalytic[] mrkCDS = new CDSAnalytic[] {MARKET_CDS[0] };
    final ISDACompliantCreditCurve creditCurve = BUILDER.calibrateCreditCurve(mrkCDS, mrkSpreads, YIELD_CURVE);

    final int n = creditCurve.getNumberOfKnots();
    for (int i = 0; i < n; i++) {
      System.out.println(creditCurve.getTimeAtIndex(i) + "\t" + creditCurve.getZeroRateAtIndex(i));
    }
    System.out.println();

    // final int step = 10;
    // for (int i = 0; i < 200; i++) {
    // final LocalDate temp = TODAY.plusDays(i * step);
    // final double t = ACT365.getDayCountFraction(TODAY, temp);
    // final double p = creditCurve.getDiscountFactor(t);
    // final double h = creditCurve.getHazardRate(t);
    // System.out.println(temp + "\t" + t + "\t" + p + "\t" + h);
    // }

    final double price = NOTIONAL * PRICER.pv(CDS, YIELD_CURVE, creditCurve, 50 / 10000.);
    System.out.println(price);
  }

  @Test(enabled = false)
  public void bucketCreditDVO1Test() {
    final double dealSpread = DEAL_SPREAD / 10000;
    final double[] mrkSpreads = new double[NUM_MARKET_CDS];
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] / 10000;
    }

    final double[] bucketCdv01 = CDV01_CAL.bucketedCS01FromParSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mrkSpreads, 1e-4, BumpType.ADDITIVE);
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      System.out.println(bucketCdv01[i] * NOTIONAL / 10000);
    }

  }

  @Test(enabled = false)
  public void bucketFlatCreditDVO1Test() {
    final double dealSpread = DEAL_SPREAD / 10000;
    final double[] mrkSpreads = new double[NUM_MARKET_CDS];
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      mrkSpreads[i] = PAR_SPREADS[i] / 10000;
    }

    final double[] bucketCdv01 = CDV01_CAL.bucketedCS01FromQuotedSpreads(CDS, dealSpread, YIELD_CURVE, MARKET_CDS, mrkSpreads, 1e-4, BumpType.ADDITIVE);
    for (int i = 0; i < NUM_MARKET_CDS; i++) {
      System.out.println(bucketCdv01[i] * NOTIONAL / 10000);
    }

  }

}
