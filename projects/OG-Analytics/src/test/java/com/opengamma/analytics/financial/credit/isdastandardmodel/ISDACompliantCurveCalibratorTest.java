/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ISDACompliantCurveCalibratorTest {
  private static final ISDACompliantPresentValueCreditDefaultSwap PRICER = new ISDACompliantPresentValueCreditDefaultSwap();

  private static final LocalDate TODAY = LocalDate.of(2013, 4, 21);
  private static final LocalDate BASE_DATE = TODAY;

  private static final LocalDate[] YC_DATES = new LocalDate[] {LocalDate.of(2013, 6, 27), LocalDate.of(2013, 8, 27), LocalDate.of(2013, 11, 27), LocalDate.of(2014, 5, 27), LocalDate.of(2015, 5, 27),
    LocalDate.of(2016, 5, 27), LocalDate.of(2018, 5, 27), LocalDate.of(2020, 5, 27), LocalDate.of(2023, 5, 27), LocalDate.of(2028, 5, 27), LocalDate.of(2033, 5, 27), LocalDate.of(2043, 5, 27) };
  private static final double[] YC_RATES;
  private static final double[] DISCOUNT_FACT;
  private static final double[] YC_TIMES;
  private static final ISDACompliantDateYieldCurve YIELD_CURVE;
  private static final DayCount ACT365 = DayCounts.ACT_365;

  static {
    final int ycPoints = YC_DATES.length;
    YC_RATES = new double[ycPoints];
    DISCOUNT_FACT = new double[ycPoints];
    Arrays.fill(DISCOUNT_FACT, 1.0);
    YC_TIMES = new double[ycPoints];
    for (int i = 0; i < ycPoints; i++) {
      YC_TIMES[i] = ACT365.getDayCountFraction(BASE_DATE, YC_DATES[i]);
    }
    YIELD_CURVE = new ISDACompliantDateYieldCurve(BASE_DATE, YC_DATES, YC_RATES);
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {

    final LocalDate today = LocalDate.of(2013, 2, 2);
    final LocalDate stepinDate = today.plusDays(1); // aka effective date
    final LocalDate valueDate = today; // NOT + 3 business days
    final LocalDate startDate = LocalDate.of(2012, 7, 29);
    final LocalDate[] endDates = new LocalDate[] {LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2014, 3, 20), LocalDate.of(2015, 3, 20), LocalDate.of(2016, 3, 20),
      LocalDate.of(2018, 3, 20), LocalDate.of(2023, 3, 20) };

    final double[] coupons = new double[] {50, 70, 100, 150, 200, 400, 1000 };
    final int n = coupons.length;
    for (int i = 0; i < n; i++) {
      coupons[i] /= 10000;
    }

    final Period tenor = Period.ofMonths(3);
    final StubType stubType = StubType.FRONTSHORT;
    final boolean payAccOndefault = true;
    final boolean protectionStart = true;
    final double recovery = 0.4;

    final ISDACompliantCurveCalibrator calibrator = new ISDACompliantCurveCalibrator();
    final ISDACompliantDateCreditCurve hc = calibrator.calibrateHazardCurve(today, stepinDate, valueDate, startDate, endDates, coupons, payAccOndefault, tenor, stubType, protectionStart, YIELD_CURVE,
        recovery);

    //    final int m = hc.getNumberOfCurvePoints();
    //    double[] t = hc.getTimes();
    //    double[] fittedRates = hc.getRates();
    //    for (int i = 0; i < m; i++) {
    //      double df = Math.exp(-t[i] * fittedRates[i]);
    //      double df2 = hc.getSurvivalProbability(t[i]);
    //      // System.out.println(t[i] + "\t" + fittedRates[i] + "\t" + df + "\t" + df2);
    //    }
    // System.out.println();

    for (int i = 0; i < n; i++) {
      final double rpv01 = PRICER.pvPremiumLegPerUnitSpread(today, stepinDate, valueDate, startDate, endDates[i], payAccOndefault, tenor, stubType, YIELD_CURVE, hc, protectionStart, PriceType.CLEAN);
      final double proLeg = PRICER.calculateProtectionLeg(today, stepinDate, valueDate, startDate, endDates[i], YIELD_CURVE, hc, recovery, protectionStart);
      final double pv = 1e7 * (proLeg - coupons[i] * rpv01);
      assertEquals(0.0, pv, 1e-8); // on a notional of 1e7
      // System.out.println(pv);
    }

    final int warmup = 1;
    final int benchmark = 0;

    for (int k = 0; k < warmup; k++) {
      final ISDACompliantDateCreditCurve hc2 = calibrator.calibrateHazardCurve(today, stepinDate, valueDate, startDate, endDates, coupons, payAccOndefault, tenor, stubType, protectionStart,
          YIELD_CURVE, recovery);
    }

    if (benchmark > 0) {
      final long t0 = System.nanoTime();
      for (int k = 0; k < benchmark; k++) {
        final ISDACompliantDateCreditCurve hc2 = calibrator.calibrateHazardCurve(today, stepinDate, valueDate, startDate, endDates, coupons, payAccOndefault, tenor, stubType, protectionStart,
            YIELD_CURVE, recovery);
      }
      final long time = System.nanoTime() - t0;
      final double timePerCalibration = (time) / 1e6 / benchmark;
      System.out.println("time per calibration: " + timePerCalibration + "ms");
    }
  }

}
