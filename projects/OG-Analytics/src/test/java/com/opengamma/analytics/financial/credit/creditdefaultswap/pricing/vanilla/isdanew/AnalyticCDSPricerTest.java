/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.StubType;

/**
 * 
 */
public class AnalyticCDSPricerTest {

  private static final AnalyticCDSPricer PRICER = new AnalyticCDSPricer();

  @Test(enabled=false)
  public void timingTest() {

    final int warmup = 1000;
    final int benchmark = 10000;

    final double fracSpred = 0.01;

    final double[] ccTimes = new double[] {0.25, 0.5, 1.00000001, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0};
    final double[] ccRates = new double[] {0.05, 0.06, 0.07, 0.05, 0.09, 0.09, 0.07, 0.065, 0.06};
    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.2, 11.0, 30.0};
    final double[] ycRates = new double[] {0.005, 0.006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05};

    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(ccTimes, ccRates);
    final ISDACompliantYieldCurve yieldCurve = new ISDACompliantYieldCurve(ycTimes, ycRates);

    LocalDate today = LocalDate.of(2013, 7, 2); // Tuesday
    LocalDate stepin = today.plusDays(1);
    LocalDate valueDate = today.plusDays(3); // Friday
    LocalDate startDate = today.plusMonths(1); // protection starts in a month
    LocalDate endDate1 = LocalDate.of(2018, 6, 20);
    LocalDate endDate2 = LocalDate.of(2023, 6, 20);

    CDSAnalytic cds1 = new CDSAnalytic(today, stepin, valueDate, startDate, endDate1, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
    CDSAnalytic cds2 = new CDSAnalytic(today, stepin, valueDate, startDate, endDate2, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);

    for (int i = 0; i < warmup; i++) {
      double p1 = PRICER.pv(cds1, yieldCurve, creditCurve, fracSpred);
      double p2 = PRICER.pv(cds2, yieldCurve, creditCurve, fracSpred);
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
      CDSAnalytic cds1temp = new CDSAnalytic(today, stepin, valueDate, startDate, endDate1, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
      CDSAnalytic cds2temp = new CDSAnalytic(today, stepin, valueDate, startDate, endDate2, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
    }

    timer = System.nanoTime();
    double p3 = 0;
    for (int i = 0; i < benchmark; i++) {
      CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate1, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
      p3 += PRICER.pv(cds, yieldCurve, creditCurve, fracSpred);
    }
    System.out.println(p3);
    time = (System.nanoTime() - timer) / 1e6;
    System.out.println("time for " + benchmark + " 5 year CDS with date logic: " + time + "ms");

    timer = System.nanoTime();
    double p4 = 0;
    for (int i = 0; i < benchmark; i++) {
      CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate2, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);
      p4 += PRICER.pv(cds1, yieldCurve, creditCurve, fracSpred);
    }
    System.out.println(p3);
    time = (System.nanoTime() - timer) / 1e6;
    System.out.println("time for " + benchmark + " 10 year CDS with date logic: " + time + "ms");

  }

  @Test(enabled=false)
  public void sensitivityTest() {
    final double[] ccTimes = new double[] {0.25, 0.5, 1.01, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0};
    final double[] ccRates = new double[] {0.005, 0.006, 0.07, 0.08, 0.09, 0.09, 0.07, 0.065, 0.06};
    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.2, 11.0, 30.0};
    final double[] ycRates = new double[] {0.000, 0.0006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05};

    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(ccTimes, ccRates);
    final ISDACompliantYieldCurve yieldCurve = new ISDACompliantYieldCurve(ycTimes, ycRates);

    LocalDate today = LocalDate.of(2013, 7, 2); // Tuesday
    LocalDate stepin = today.plusDays(1); // this is usually 1
    LocalDate valueDate = today.plusDays(3); // Friday
    LocalDate startDate = today; // protection starts now
    LocalDate endDate = LocalDate.of(2017, 9, 20);

    final boolean payAccOnDefault = true;

    CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate, payAccOnDefault, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);

    final int n = creditCurve.getNumberOfKnots();
    for (int i = 0; i < n; i++) {
      final double fdProSense = fdProtectionLegSense(cds, yieldCurve, creditCurve, i);
      final double analProSense = PRICER.protectionLegCreditSensitivity(cds, yieldCurve, creditCurve, i);

      final double fdRPV01Sense = fdRPV01Sense(cds, yieldCurve, creditCurve, i);
      final double analRPV01Sense = PRICER.rpv01CreditSensitivity(cds, yieldCurve, creditCurve, i);

      assertEquals("ProSense " + i, fdProSense, analProSense, 1e-8);
      assertEquals("RPV01Sense " + i, fdRPV01Sense, analRPV01Sense, 1e-8);

      // System.out.println(fdRPV01Sense + "\t" + analRPV01Sense);
    }
  }

  @Test(enabled=false)
  public void sensitivityParallelShiftTest() {
    final double[] ccTimes = new double[] {0.25, 0.5, 1.00000001, 2.0, 3.0, 5.0, 7.2, 10.0, 20.0};
    final double[] ccRates = new double[] {0.05, 0.06, 0.07, 0.05, 0.09, 0.09, 0.07, 0.065, 0.06};
    final double[] ycTimes = new double[] {1 / 52., 1 / 12., 1 / 4., 1 / 2., 3 / 4., 1.0, 2.1, 5.2, 11.0, 30.0};
    final double[] ycRates = new double[] {0.005, 0.006, 0.007, 0.01, 0.01, 0.015, 0.02, 0.03, 0.04, 0.05};

    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(ccTimes, ccRates);
    final ISDACompliantYieldCurve yieldCurve = new ISDACompliantYieldCurve(ycTimes, ycRates);

    LocalDate today = LocalDate.of(2013, 7, 2); // Tuesday
    LocalDate stepin = today.plusDays(2); // this is usually 1
    LocalDate valueDate = today.plusDays(3); // Friday
    LocalDate startDate = today.plusMonths(1); // protection starts in a month
    LocalDate endDate = LocalDate.of(2023, 6, 20);

    CDSAnalytic cds = new CDSAnalytic(today, stepin, valueDate, startDate, endDate, true, Period.ofMonths(3), StubType.FRONTSHORT, false, 0.4);

    final double fd = fdProtectionLegSense(cds, yieldCurve, creditCurve);

    final int n = creditCurve.getNumberOfKnots();
    double anal = 0.0;
    for (int i = 0; i < n; i++) {
      anal += PRICER.protectionLegCreditSensitivity(cds, yieldCurve, creditCurve, i);
    }
    assertEquals(fd, anal, 1e-8);
  }

  private double fdRPV01Sense(final CDSAnalytic cds, ISDACompliantYieldCurve yieldCurve, ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {

    final double h = creditCurve.getZeroRateAtIndex(creditCurveNode);
    final double eps = 1e-4 * Math.max(0.001, h);

    final ISDACompliantCreditCurve ccUp = creditCurve.withRate(h + eps, creditCurveNode);
    final ISDACompliantCreditCurve ccDown = creditCurve.withRate(h - eps, creditCurveNode);
    final double up = PRICER.rpv01(cds, yieldCurve, ccUp, PriceType.DIRTY); // clean or dirty has no effect on sensitivity
    final double down = PRICER.rpv01(cds, yieldCurve, ccDown, PriceType.DIRTY);
    return (up - down) / 2 / eps;
  }

  private double fdProtectionLegSense(final CDSAnalytic cds, ISDACompliantYieldCurve yieldCurve, ISDACompliantCreditCurve creditCurve) {

    final int n = creditCurve.getNumberOfKnots();
    final double h = 0.5 * (creditCurve.getZeroRateAtIndex(0) + creditCurve.getZeroRateAtIndex(n - 1));
    final double eps = 1e-5 * h;

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

  private double fdProtectionLegSense(final CDSAnalytic cds, ISDACompliantYieldCurve yieldCurve, ISDACompliantCreditCurve creditCurve, final int creditCurveNode) {

    final double h = creditCurve.getZeroRateAtIndex(creditCurveNode);
    final double eps = 1e-4 * Math.max(0.001, h);

    final ISDACompliantCreditCurve ccUp = creditCurve.withRate(h + eps, creditCurveNode);
    final ISDACompliantCreditCurve ccDown = creditCurve.withRate(h - eps, creditCurveNode);
    final double up = PRICER.protectionLeg(cds, yieldCurve, ccUp);
    final double down = PRICER.protectionLeg(cds, yieldCurve, ccDown);
    return (up - down) / 2 / eps;
  }

}
