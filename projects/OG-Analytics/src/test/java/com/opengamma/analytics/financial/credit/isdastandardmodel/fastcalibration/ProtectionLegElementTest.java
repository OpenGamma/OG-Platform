/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.DoublesScheduleGenerator.getIntegrationsPoints;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.AccrualOnDefaultFormulae;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSCoupon;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.PremiumLegElement;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.ProtectionLegElement;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ProtectionLegElementTest extends ISDABaseTest {

  @Test
  public void test() {
    final LocalDate tradeDate = LocalDate.of(2013, Month.SEPTEMBER, 5);
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 1, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    //final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M" };
    final double[] rates = new double[] {0.004919, 0.005006, 0.00515, 0.005906, 0.008813, 0.0088, 0.01195, 0.01534, 0.01836, 0.02096, 0.02322, 0.02514, 0.02673, 0.02802, 0.02997, 0.0318, 0.03331,
      0.03383, 0.034 };
    final ISDACompliantYieldCurve yieldCurve = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT_ACT_ISDA, ACT_ACT_ISDA, Period.ofMonths(6));

    final double[] ccKnots = new double[] {0.5, 1, 2, 3, 5, 10 };
    final double[] h = new double[] {0.01, 0.012, 0.015, 0.015, 0.013, 0.012 };
    final double[] knots = getIntegrationsPoints(0, 10, yieldCurve.getKnotTimes(), ccKnots);
    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(ccKnots, h);

    final ProtectionLegElement protElem = new ProtectionLegElement(2., 5., yieldCurve, 4, knots);
    double[] pv = protElem.pvAndSense(creditCurve);
    final double eps = 1e-6;
    final ISDACompliantCreditCurve ccUp = creditCurve.withRate(h[4] + eps, 4);
    final ISDACompliantCreditCurve ccDown = creditCurve.withRate(h[4] - eps, 4);
    double pvUp = protElem.pvAndSense(ccUp)[0];
    double pvDown = protElem.pvAndSense(ccDown)[0];
    double fdSense = (pvUp - pvDown) / 2 / eps;
    assertEquals(fdSense, pv[1], 1e-10);

    final CDSAnalyticFactory factory = new CDSAnalyticFactory();
    final CDSAnalytic cds = factory.makeIMMCDS(tradeDate, Period.ofYears(5));
    final CDSCoupon coupon = cds.getCoupon(cds.getNumPayments() - 1);
    PremiumLegElement premElem = new PremiumLegElement(0, coupon, yieldCurve, 4, knots, AccrualOnDefaultFormulae.OrignalISDA);
    pv = premElem.pvAndSense(creditCurve);
    pvUp = premElem.pvAndSense(ccUp)[0];
    pvDown = premElem.pvAndSense(ccDown)[0];
    fdSense = (pvUp - pvDown) / 2 / eps;
    assertEquals(fdSense, pv[1], 1e-10);

    premElem = new PremiumLegElement(0, coupon, yieldCurve, 4, knots, AccrualOnDefaultFormulae.MarkitFix);
    pv = premElem.pvAndSense(creditCurve);
    pvUp = premElem.pvAndSense(ccUp)[0];
    pvDown = premElem.pvAndSense(ccDown)[0];
    fdSense = (pvUp - pvDown) / 2 / eps;
    assertEquals(fdSense, pv[1], 1e-10);
  }

}
