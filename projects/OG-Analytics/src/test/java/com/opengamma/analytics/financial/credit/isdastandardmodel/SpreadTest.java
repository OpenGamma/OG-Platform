/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

/**
 * 
 */
public class SpreadTest extends ISDABaseTest {

  @Test
  public void test() {
    final ISDACompliantYieldCurve yieldCurve = new ISDACompliantYieldCurve(1, 0.05);
    final ISDACompliantCreditCurve creditCurve = new ISDACompliantCreditCurve(1, 0.01);
    final CDSAnalyticFactory factory = new CDSAnalyticFactory();
    final Period tenor = Period.ofYears(1);

    final LocalDate baseDate = LocalDate.of(2013, 12, 3);
    for (int i = 0; i < 60; i++) {
      final LocalDate tradeDate = baseDate.plusDays(i);
      final CDSAnalytic cds = factory.makeIMMCDS(tradeDate, tenor);
      final CDSAnalytic cds2 = factory.makeIMMCDS(tradeDate, tradeDate.plusDays(1), new Period[] {tenor })[0];
      final double prot = PRICER.protectionLeg(cds, yieldCurve, creditCurve);
      final double rpv01_d = PRICER.annuity(cds, yieldCurve, creditCurve, PriceType.DIRTY);
      final double rpv01_c = rpv01_d - cds.getAccruedPremiumPerUnitSpread();
      final double s = prot / rpv01_c;
      final double s_d = prot / rpv01_d;
      final double s_shortStub = PRICER.parSpread(cds2, yieldCurve, creditCurve);

      System.out.println(tradeDate + "\t" + s * TEN_THOUSAND + "\t" + s_shortStub * TEN_THOUSAND + "\t" + s_d * TEN_THOUSAND);

    }
  }

}
