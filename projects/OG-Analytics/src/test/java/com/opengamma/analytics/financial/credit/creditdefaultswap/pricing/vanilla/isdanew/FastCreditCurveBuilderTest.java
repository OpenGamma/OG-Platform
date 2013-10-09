/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.FastCreditCurveBuilder.Pricer;

/**
 * 
 */
public class FastCreditCurveBuilderTest extends CreditCurveCalibrationTest {

  // private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");

  private static final FastCreditCurveBuilder BUILDER = new FastCreditCurveBuilder();

  // private static final ISDACompliantPresentValueCreditDefaultSwap TEST_PRICER = new ISDACompliantPresentValueCreditDefaultSwap();
  //
  // private static final LocalDate TODAY = LocalDate.of(2013, 4, 21);
  // private static final LocalDate BASE_DATE = TODAY;
  //
  // private static final LocalDate[] YC_DATES = new LocalDate[] {LocalDate.of(2013, 6, 27), LocalDate.of(2013, 8, 27), LocalDate.of(2013, 11, 27), LocalDate.of(2014, 5, 27), LocalDate.of(2015, 5,
  // 27),
  // LocalDate.of(2016, 5, 27), LocalDate.of(2018, 5, 27), LocalDate.of(2020, 5, 27), LocalDate.of(2023, 5, 27), LocalDate.of(2028, 5, 27), LocalDate.of(2033, 5, 27), LocalDate.of(2043, 5, 27)};
  // private static final double[] YC_RATES;
  // private static final double[] DISCOUNT_FACT;
  // private static final double[] YC_TIMES;
  // private static final ISDACompliantDateYieldCurve YIELD_CURVE;
  // private static final DayCount ACT365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  //
  // static {
  // final int ycPoints = YC_DATES.length;
  // YC_RATES = new double[ycPoints];
  // DISCOUNT_FACT = new double[ycPoints];
  // Arrays.fill(DISCOUNT_FACT, 1.0);
  // YC_TIMES = new double[ycPoints];
  // for (int i = 0; i < ycPoints; i++) {
  // YC_TIMES[i] = ACT365.getDayCountFraction(BASE_DATE, YC_DATES[i]);
  // }
  // YIELD_CURVE = new ISDACompliantDateYieldCurve(BASE_DATE, YC_DATES, YC_RATES);
  // }

  @Test
  public void test() {

    // final LocalDate today = LocalDate.of(2013, 2, 2);
    // final LocalDate stepinDate = today.plusDays(1); // aka effective date
    // final LocalDate valueDate = addWorkDays(today, 3, DEFAULT_CALENDAR); // 3 working days on
    // final LocalDate startDate = LocalDate.of(2012, 7, 29);
    // final LocalDate[] endDates = new LocalDate[] {LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2014, 3, 20), LocalDate.of(2015, 3, 20), LocalDate.of(2016, 3, 20),
    // LocalDate.of(2018, 3, 20), LocalDate.of(2023, 3, 20)};
    //
    // final double[] coupons = new double[] {50, 70, 100, 150, 200, 400, 1000};
    // final int n = coupons.length;
    // for (int i = 0; i < n; i++) {
    // coupons[i] /= 10000;
    // }
    //
    // final Period tenor = Period.ofMonths(3);
    // final StubType stubType = StubType.FRONTSHORT;
    // final boolean payAccOndefault = true;
    // final boolean protectionStart = true;
    // final double recovery = 0.4;
    //
    // CDSAnalytic[] cds = new CDSAnalytic[n];
    // for (int i = 0; i < n; i++) {
    // cds[i] = new CDSAnalytic(today, stepinDate, valueDate, startDate, endDates[i], payAccOndefault, tenor, stubType, protectionStart, recovery);
    // }
    //
    // ISDACompliantCreditCurve hc = BUILDER.calibrateCreditCurve(cds, coupons, YIELD_CURVE);
    // ISDACompliantDateCreditCurve hcDate = new ISDACompliantDateCreditCurve(today, endDates, hc.getKnotZeroRates());
    //
    // // test against 'old' pricer as well
    // for(int i=0;i<n;i++) {
    // double rpv01 = TEST_PRICER.pvPremiumLegPerUnitSpread(today, stepinDate, valueDate, startDate, endDates[i], payAccOndefault, tenor, stubType, YIELD_CURVE, hcDate, protectionStart,
    // PriceType.CLEAN);
    // double proLeg = TEST_PRICER.calculateProtectionLeg(today, stepinDate, valueDate, startDate, endDates[i], YIELD_CURVE, hcDate, recovery, protectionStart);
    // double pv2 = 1e7 * (proLeg - coupons[i] * rpv01);
    // assertEquals(0.0, pv2, 1e-7); // we drop a slight bit of accuracy here
    // }
    //
    // final int warmup = 0;
    // final int benchmark = 0;
    //
    // for (int k = 0; k < warmup; k++) {
    // ISDACompliantCreditCurve hc2 = BUILDER.calibrateCreditCurve(cds, coupons, YIELD_CURVE);
    // }
    //
    // if (benchmark > 0) {
    // long t0 = System.nanoTime();
    // for (int k = 0; k < benchmark; k++) {
    // ISDACompliantCreditCurve hc2 = BUILDER.calibrateCreditCurve(cds, coupons, YIELD_CURVE);
    // }
    // long time = System.nanoTime() - t0;
    // double timePerCalibration = ((double) time) / 1e6 / benchmark;
    // System.out.println("time per calibration: " + timePerCalibration + "ms");
    // }

  }

  @Test
  public void internalPricerTest() {
    final AnalyticCDSPricer testedPricer = new AnalyticCDSPricer();

    final Pricer pricer = BUILDER.new Pricer(CDS, YIELD_CURVE, CREDIT_CURVE_KNOTS, DEAL_SPREAD / 10000., 0.0);

    final double proLeg1 = testedPricer.protectionLeg(CDS, YIELD_CURVE, CREDIT_CURVE);
    final double proLeg2 = pricer.protectionLeg(CREDIT_CURVE);

    final double premLeg1 = testedPricer.pvPremiumLegPerUnitSpread(CDS, YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN);
    final double premLeg2 = pricer.rpv01(CREDIT_CURVE, PriceType.CLEAN);

    assertEquals("pro-leg", proLeg1, proLeg2, 1e-13);
    assertEquals("prem-leg", premLeg1, premLeg2, 1e-13);
  }
}
