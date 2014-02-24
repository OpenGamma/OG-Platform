/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AnalyticCDSPricerTest extends ISDABaseTest {

  @SuppressWarnings("unused")
  @Test(enabled = false)
  public void timingTest() {

    final int warmup = 1000;
    final int benchmark = 10000;

    final double fracSpred = 0.01;

    final double[] ccTimes = new double[] {0.25, 0.5, 1.00000001, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0 };
    final double[] ccRates = new double[] {0.05, 0.06, 0.07, 0.05, 0.09, 0.09, 0.07, 0.065, 0.06 };
    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.2, 11.0, 30.0 };
    final double[] ycRates = new double[] {0.005, 0.006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05 };

    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(ccTimes, ccRates);
    final ISDACompliantYieldCurve yieldCurve = new ISDACompliantYieldCurve(ycTimes, ycRates);

    final LocalDate today = LocalDate.of(2013, 7, 2); // Tuesday
    final LocalDate stepin = today.plusDays(1);
    final LocalDate valueDate = today.plusDays(3); // Friday
    final LocalDate startDate = today.plusMonths(1); // protection starts in a month
    final LocalDate endDate1 = LocalDate.of(2018, 6, 20);
    final LocalDate endDate2 = LocalDate.of(2023, 6, 20);

    final CDSAnalytic cds1 = new CDSAnalytic(today, stepin, valueDate, startDate, endDate1, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
    final CDSAnalytic cds2 = new CDSAnalytic(today, stepin, valueDate, startDate, endDate2, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);

    for (int i = 0; i < warmup; i++) {
      final double p1 = PRICER.pv(cds1, yieldCurve, creditCurve, fracSpred);
      final double p2 = PRICER.pv(cds2, yieldCurve, creditCurve, fracSpred);
    }
    long timer = System.nanoTime();
    double p1 = 0;
    for (int i = 0; i < benchmark; i++) {
      p1 += PRICER.pv(cds1, yieldCurve, creditCurve, fracSpred);
    }
    System.out.println(p1);
    double time = (System.nanoTime() - timer) / 1e6;
    System.out.println("time for " + benchmark + " 5 year CDS: " + time + "ms");

    timer = System.nanoTime();
    double p2 = 0;
    for (int i = 0; i < benchmark; i++) {
      p2 += PRICER.pv(cds2, yieldCurve, creditCurve, fracSpred);
    }
    System.out.println(p2);
    time = (System.nanoTime() - timer) / 1e6;
    System.out.println("time for " + benchmark + " 10 year CDS: " + time + "ms");

    // now do the date logic
    for (int i = 0; i < warmup; i++) {
      final CDSAnalytic cds1temp = new CDSAnalytic(today, stepin, valueDate, startDate, endDate1, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
      final CDSAnalytic cds2temp = new CDSAnalytic(today, stepin, valueDate, startDate, endDate2, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
    }

    timer = System.nanoTime();
    double p3 = 0;
    for (int i = 0; i < benchmark; i++) {
      final CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate1, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
      p3 += PRICER.pv(cds, yieldCurve, creditCurve, fracSpred);
    }
    System.out.println(p3);
    time = (System.nanoTime() - timer) / 1e6;
    System.out.println("time for " + benchmark + " 5 year CDS with date logic: " + time + "ms");

    timer = System.nanoTime();
    double p4 = 0;
    for (int i = 0; i < benchmark; i++) {
      final CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate2, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
      p4 += PRICER.pv(cds, yieldCurve, creditCurve, fracSpred);
    }
    System.out.println(p3);
    time = (System.nanoTime() - timer) / 1e6;
    System.out.println("time for " + benchmark + " 10 year CDS with date logic: " + time + "ms");

  }

  @Test
  public void creditCurveSensitivityTest() {
    final double[] ccTimes = new double[] {0.25, 0.5, 1.001, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0 };
    final double[] ccNormalRates = new double[] {0.05, 0.06, 0.07, 0.08, 0.09, 0.09, 0.07, 0.065, 0.06 };
    final double[] ccLowRates = new double[] {0.00, 0.00, 1e-6, 2e-4, 5e-4, 0.001, 0.0015, 0.002, 0.0015 };
    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.0, 11.0, 30.0 };
    final double[] ycNormalRates = new double[] {0.004, 0.006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05 };
    final double[] ycLowRates = new double[] {0.00, 0.00, 0.00, 0.0, 0.00, 0.0005, 0.001, 0.0015, 0.002, 0.0015 };

    final ISDACompliantCreditCurve creditCurveLow = new ISDACompliantCreditCurve(ccTimes, ccLowRates);
    final ISDACompliantCreditCurve creditCurveNorm = new ISDACompliantCreditCurve(ccTimes, ccNormalRates);
    final ISDACompliantYieldCurve yieldCurveLow = new ISDACompliantYieldCurve(ycTimes, ycLowRates);
    final ISDACompliantYieldCurve yieldCurveNorm = new ISDACompliantYieldCurve(ycTimes, ycNormalRates);

    final LocalDate today = LocalDate.of(2013, 7, 2); // Tuesday
    final LocalDate stepin = today.plusDays(1); // this is usually 1
    final LocalDate valueDate = today.plusDays(3); // Friday
    final LocalDate startDate = today; // protection starts now
    final LocalDate endDate = LocalDate.of(2017, 9, 20);

    final boolean payAccOnDefault = true;

    final CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate, payAccOnDefault, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);

    for (int count = 0; count < 2; count++) {
      final AnalyticCDSPricer pricer = count == 0 ? PRICER : PRICER_MARKIT_FIX;
      creditCurveSenseTest(pricer, cds, yieldCurveLow, creditCurveLow);
      creditCurveSenseTest(pricer, cds, yieldCurveLow, creditCurveNorm);
      creditCurveSenseTest(pricer, cds, yieldCurveNorm, creditCurveLow);
      creditCurveSenseTest(pricer, cds, yieldCurveNorm, creditCurveNorm);
    }
  }

  @Test
  public void yieldCurveSenseTest() {

    final double coupon = 0.01;

    final double[] ccTimes = new double[] {0.25, 0.5, 1.001, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0 };
    final double[] ccNormalRates = new double[] {0.05, 0.06, 0.07, 0.08, 0.09, 0.09, 0.07, 0.065, 0.06 };

    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.0, 11.0, 30.0 };
    final double[] ycNormalRates = new double[] {0.004, 0.006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05 };

    final ISDACompliantCreditCurve creditCurveNorm = new ISDACompliantCreditCurve(ccTimes, ccNormalRates);
    final ISDACompliantYieldCurve yieldCurveNorm = new ISDACompliantYieldCurve(ycTimes, ycNormalRates);

    final CDSAnalyticFactory factory = new CDSAnalyticFactory();
    final CDSAnalytic cds = factory.makeIMMCDS(LocalDate.of(2013, Month.SEPTEMBER, 10), Period.ofYears(5));
    // final AnalyticCDSPricer pricer = new AnalyticCDSPricer(true);

    final int n = ycTimes.length;
    for (int i = 0; i < n; i++) {
      double fd = fdProtectionLegYieldSense(PRICER_MARKIT_FIX, cds, yieldCurveNorm, creditCurveNorm, i);
      double anal = PRICER_MARKIT_FIX.protectionLegYieldSensitivity(cds, yieldCurveNorm, creditCurveNorm, i);
      // System.out.println(fd + "\t" + anal);
      assertEquals(fd, anal, 1e-10);

      fd = fdPremiumLegYieldSense(PRICER_MARKIT_FIX, cds, yieldCurveNorm, creditCurveNorm, i);
      anal = PRICER_MARKIT_FIX.pvPremiumLegYieldSensitivity(cds, yieldCurveNorm, creditCurveNorm, i);
      //   System.out.println(fd + "\t" + anal);
      assertEquals(fd, anal, 1e-9);

      fd = fdPVYieldSense(PRICER_MARKIT_FIX, cds, yieldCurveNorm, creditCurveNorm, coupon, i);
      anal = PRICER_MARKIT_FIX.pvYieldSensitivity(cds, yieldCurveNorm, creditCurveNorm, coupon, i);
      //  System.out.println(fd + "\t" + anal);
      assertEquals(fd, anal, 1e-10);
    }

  }

  @Test(enabled = false)
  void spreadSensitivityTest() {
    System.out.println("spreadSensitivityTest");

    final double[] ccTimes = new double[] {0.25, 0.5, 1.001, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0 };
    final double[] ccNormalRates = new double[] {0.05, 0.06, 0.07, 0.08, 0.09, 0.09, 0.07, 0.065, 0.06 };
    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.0, 11.0, 30.0 };
    final double[] ycNormalRates = new double[] {0.004, 0.006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05 };
    final ISDACompliantCreditCurve creditCurveNorm = new ISDACompliantCreditCurve(ccTimes, ccNormalRates);
    final ISDACompliantYieldCurve yieldCurveNorm = new ISDACompliantYieldCurve(ycTimes, ycNormalRates);

    final LocalDate today = LocalDate.of(2013, 7, 2); // Tuesday
    final LocalDate stepin = today.plusDays(1); // this is usually 1
    final LocalDate valueDate = today.plusDays(3); // Friday
    final LocalDate startDate = today; // protection starts now
    final LocalDate endDate = LocalDate.of(2017, 9, 20);

    final boolean payAccOnDefault = true;

    final CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate, payAccOnDefault, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);

    for (int i = 0; i < ccTimes.length; i++) {
      final double dSdH = PRICER.parSpreadCreditSensitivity(cds, yieldCurveNorm, creditCurveNorm, i);
      System.out.println(dSdH);
    }
  }

  private void creditCurveSenseTest(final AnalyticCDSPricer pricer, final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {
    final int n = creditCurve.getNumberOfKnots();
    for (int i = 0; i < n; i++) {
      final double fdProSense = fdProtectionLegSense(cds, yieldCurve, creditCurve, i);
      final double analProSense = pricer.protectionLegCreditSensitivity(cds, yieldCurve, creditCurve, i);
      final double fdRPV01Sense = fdRPV01Sense(cds, yieldCurve, creditCurve, i, pricer);
      final double analRPV01Sense = pricer.pvPremiumLegCreditSensitivity(cds, yieldCurve, creditCurve, i);
      assertEquals("ProSense " + i, fdProSense, analProSense, 1e-9);
      assertEquals("RPV01Sense " + i, fdRPV01Sense, analRPV01Sense, 5e-8);
    }
  }

  @Test
  // (enabled=false)
  public void sensitivityParallelShiftTest() {
    final double[] ccTimes = new double[] {0.25, 0.5, 1.00000001, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0 };
    final double[] ccRates = new double[] {0.05, 0.06, 0.07, 0.05, 0.09, 0.09, 0.07, 0.065, 0.06 };
    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.2, 11.0, 30.0 };
    final double[] ycRates = new double[] {0.005, 0.006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05 };

    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(ccTimes, ccRates);
    final ISDACompliantYieldCurve yieldCurve = new ISDACompliantYieldCurve(ycTimes, ycRates);

    final LocalDate today = LocalDate.of(2013, 7, 2); // Tuesday
    final LocalDate stepin = today.plusDays(2); // this is usually 1
    final LocalDate valueDate = today.plusDays(3); // Friday
    final LocalDate startDate = today.plusMonths(1); // protection starts in a month
    final LocalDate endDate = LocalDate.of(2023, 6, 20);

    final CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);

    final double fd = fdProtectionLegSense(cds, yieldCurve, creditCurve);

    final int n = creditCurve.getNumberOfKnots();
    double anal = 0.0;
    for (int i = 0; i < n; i++) {
      anal += PRICER.protectionLegCreditSensitivity(cds, yieldCurve, creditCurve, i);
    }
    assertEquals(fd, anal, 1e-8);
  }

  private double fdRPV01Sense(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode, final AnalyticCDSPricer pricer) {

    final double h = creditCurve.getZeroRateAtIndex(creditCurveNode);
    final double eps = 1e-3 * Math.max(1e-3, h);

    final ISDACompliantCreditCurve ccUp = creditCurve.withRate(h + eps, creditCurveNode);
    final ISDACompliantCreditCurve ccDown = creditCurve.withRate(h - eps, creditCurveNode);
    final double up = pricer.annuity(cds, yieldCurve, ccUp, PriceType.DIRTY); // clean or dirty has no effect on sensitivity
    final double down = pricer.annuity(cds, yieldCurve, ccDown, PriceType.DIRTY);
    return (up - down) / 2 / eps;
  }

  private double fdProtectionLegSense(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve) {

    final int n = creditCurve.getNumberOfKnots();
    final double h = 0.5 * (creditCurve.getZeroRateAtIndex(0) + creditCurve.getZeroRateAtIndex(n - 1));
    final double eps = 1e-4 * h;

    final double[] rUp = creditCurve.getKnotZeroRates();
    final double[] rDown = creditCurve.getKnotZeroRates();
    for (int i = 0; i < n; i++) {
      rUp[i] += eps;
      rDown[i] -= eps;
    }
    final double up = PRICER.protectionLeg(cds, yieldCurve, creditCurve.withRates(rUp));
    final double down = PRICER.protectionLeg(cds, yieldCurve, creditCurve.withRates(rDown));
    return (up - down) / 2 / eps;
  }

  private double fdProtectionLegSense(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {

    final double h = creditCurve.getZeroRateAtIndex(creditCurveNode);
    final double eps = 1e-4 * Math.max(1e-3, h);

    final ISDACompliantCreditCurve ccUp = creditCurve.withRate(h + eps, creditCurveNode);
    final ISDACompliantCreditCurve ccDown = creditCurve.withRate(h - eps, creditCurveNode);
    final double up = PRICER.protectionLeg(cds, yieldCurve, ccUp);
    final double down = PRICER.protectionLeg(cds, yieldCurve, ccDown);
    return (up - down) / 2 / eps;
  }

  private double fdProtectionLegYieldSense(final AnalyticCDSPricer pricer, final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve,
      final int yieldCurveNode) {

    final double r = yieldCurve.getZeroRateAtIndex(yieldCurveNode);
    final double eps = 1e-4 * Math.max(1e-3, r);

    final ISDACompliantYieldCurve yUp = yieldCurve.withRate(r + eps, yieldCurveNode);
    final ISDACompliantYieldCurve yDown = yieldCurve.withRate(r - eps, yieldCurveNode);
    final double up = pricer.protectionLeg(cds, yUp, creditCurve);
    final double down = pricer.protectionLeg(cds, yDown, creditCurve);
    return (up - down) / 2 / eps;
  }

  private double fdPremiumLegYieldSense(final AnalyticCDSPricer pricer, final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve,
      final int yieldCurveNode) {

    final double r = yieldCurve.getZeroRateAtIndex(yieldCurveNode);
    final double eps = 1e-4 * Math.max(1e-3, r);

    final ISDACompliantYieldCurve yUp = yieldCurve.withRate(r + eps, yieldCurveNode);
    final ISDACompliantYieldCurve yDown = yieldCurve.withRate(r - eps, yieldCurveNode);
    final double up = pricer.annuity(cds, yUp, creditCurve, PriceType.CLEAN);
    final double down = pricer.annuity(cds, yDown, creditCurve, PriceType.CLEAN);
    return (up - down) / 2 / eps;
  }

  private double fdPVYieldSense(final AnalyticCDSPricer pricer, final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve creditCurve, final double coupon,
      final int yieldCurveNode) {

    final double r = yieldCurve.getZeroRateAtIndex(yieldCurveNode);
    final double eps = 1e-4 * Math.max(1e-3, r);

    final ISDACompliantYieldCurve yUp = yieldCurve.withRate(r + eps, yieldCurveNode);
    final ISDACompliantYieldCurve yDown = yieldCurve.withRate(r - eps, yieldCurveNode);
    final double up = pricer.pv(cds, yUp, creditCurve, coupon, PriceType.DIRTY);
    final double down = pricer.pv(cds, yDown, creditCurve, coupon, PriceType.DIRTY);
    return (up - down) / 2 / eps;
  }

}
