/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.threeten.bp.Month.OCTOBER;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MatlabTest extends ISDABaseTest {

  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory();
  private static final ISDACompliantYieldCurve YIELD_CUVRE;
  private static final ISDACompliantCreditCurve CREDIT_CUVRE;

  static {
    final double[] ycNodes = new double[] {1 / 12., 2 / 12., 3 / 12., 6 / 12., 1, 2, 3, 5, 7, 10, 15, 20, 30 };
    final double[] r = new double[] {0.03, 0.034, 0.037, 0.04, 0.05, 0.06, 0.057, 0.055, 0.05, 0.047, 0.045, 0.43, 0.04 };
    YIELD_CUVRE = new ISDACompliantYieldCurve(ycNodes, r);
    final double[] ccNodes = new double[] {0.23, 0.56, 1, 3, 5, 10 };
    final double[] h = new double[] {0.01, 0.014, 0.02, 0.02, 0.017, 0.015 };
    CREDIT_CUVRE = new ISDACompliantCreditCurve(ccNodes, h);
  }

  @Test(enabled = false)
  public void dumpCDSData() {
    final CDSAnalytic cds = FACTORY.makeIMMCDS(LocalDate.of(2013, OCTOBER, 23), Period.ofYears(1));

    System.out.println("start " + cds.getAccStart());
    System.out.println("effProtStart " + cds.getEffectiveProtectionStart());
    System.out.println("valuationTime " + cds.getCashSettleTime());
    System.out.println("protEnd " + cds.getProtectionEnd());
    System.out.println("unitAccruedPremium " + cds.getAccruedYearFraction());
    System.out.println("lgd " + cds.getLGD());

    final int n = cds.getNumPayments();
    for (final CDSCoupon coupon : cds.getCoupons()) {
      System.out.println();
      System.out.println("effStart " + coupon.getEffStart());
      System.out.println("effEnd " + coupon.getEffEnd());
      System.out.println("paymentTime " + coupon.getPaymentTime());
      System.out.println("yearFrac " + coupon.getYearFrac());
      System.out.println("yfRatio " + coupon.getYFRatio());
    }

    final double pv = PRICER.pv(cds, YIELD_CUVRE, CREDIT_CUVRE, 0.01);
    final double rpv01 = PRICER.annuity(cds, YIELD_CUVRE, CREDIT_CUVRE, PriceType.CLEAN);
    final double protLeg = PRICER.protectionLeg(cds, YIELD_CUVRE, CREDIT_CUVRE);
    System.out.println("pv " + pv);
    System.out.println("rpv01 " + rpv01);
    System.out.println("protLeg " + protLeg);
  }

  @Test(enabled = false)
  public void priceTest() {
    final CDSAnalytic cds = FACTORY.makeIMMCDS(LocalDate.of(2013, OCTOBER, 28), Period.ofYears(5));
    final double pv = PRICER.pv(cds, YIELD_CUVRE, CREDIT_CUVRE, 0.01);
    System.out.println("pv " + pv);
  }

}
