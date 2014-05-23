/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CDSAnalyticTest extends ISDABaseTest {
  private static CDSAnalyticFactory FACTORY = new CDSAnalyticFactory();

  @Test
  public void accruedInterestTest() {

    final LocalDate accStart = LocalDate.of(2011, Month.MARCH, 21);
    final LocalDate maturity = LocalDate.of(2011, Month.SEPTEMBER, 20);

    LocalDate tradeDate = LocalDate.of(2011, Month.JUNE, 18);
    CDSAnalytic cds = FACTORY.makeCDS(tradeDate, accStart, maturity);
    assertEquals(90, cds.getAccuredDays());

    tradeDate = LocalDate.of(2011, Month.JUNE, 19);
    cds = FACTORY.makeCDS(tradeDate, accStart, maturity);
    assertEquals(0, cds.getAccuredDays());

    tradeDate = LocalDate.of(2011, Month.JUNE, 20);
    cds = FACTORY.makeCDS(tradeDate, accStart, maturity);
    assertEquals(1, cds.getAccuredDays());
  }

  @Test
  public void accruedInterestTest2() {

    final LocalDate accStart = LocalDate.of(2011, Month.MARCH, 21);
    final LocalDate maturity = LocalDate.of(2011, Month.JUNE, 20);

    LocalDate tradeDate = LocalDate.of(2011, Month.JUNE, 18);
    CDSAnalytic cds = FACTORY.makeCDS(tradeDate, accStart, maturity);
    assertEquals(90, cds.getAccuredDays());

    tradeDate = LocalDate.of(2011, Month.JUNE, 19);
    cds = FACTORY.makeCDS(tradeDate, accStart, maturity);
    assertEquals(91, cds.getAccuredDays()); //NOTE: this is the result from calling the ISDA c code (via Excel). The Markit calculator
    //shows 0 accrued days for this - this is probably an override before the model is hit. 
  }

  @Test
  public void equalsTest() {
    final LocalDate tradeDate = LocalDate.of(2014, 2, 26);
    final Period term = Period.ofYears(5);
    final CDSAnalytic cds1 = FACTORY.makeIMMCDS(tradeDate, term);
    final CDSAnalytic cds2 = FACTORY.makeIMMCDS(tradeDate, term);
    assertEquals(cds1, cds2);

    final CDSAnalytic cds3 = FACTORY.withRecoveryRate(0.3).makeIMMCDS(tradeDate, term);
    assertFalse(cds1.equals(cds3));

    final CDSAnalytic cds4 = cds3.withRecoveryRate(0.4);
    assertTrue(cds1.equals(cds4));
  }

  @Test
  public void offsetTest() {
    final LocalDate tradeDate = LocalDate.of(2014, 2, 26);
    final LocalDate expiry = LocalDate.of(2014, 3, 12);
    final Period term = Period.ofYears(5);
    final CDSAnalytic fwdCDS = FACTORY.makeIMMCDS(expiry, term);
    final CDSAnalytic fwdStartingCDS = FACTORY.makeForwardStartingIMMCDS(tradeDate, expiry, term);
    final double tE = ACT365F.getDayCountFraction(tradeDate, expiry);
    final CDSAnalytic fwdStartingCDS2 = fwdCDS.withOffset(tE);
    assertEquals(fwdStartingCDS.getAccruedYearFraction(), fwdStartingCDS2.getAccruedYearFraction());
    assertEquals(fwdStartingCDS.getLGD(), fwdStartingCDS2.getLGD());
    assertEquals(fwdStartingCDS.getNumPayments(), fwdStartingCDS2.getNumPayments());
    assertEquals(fwdStartingCDS.getAccStart(), fwdStartingCDS2.getAccStart(), 1e-15);
    assertEquals(fwdStartingCDS.getEffectiveProtectionStart(), fwdStartingCDS2.getEffectiveProtectionStart(), 1e-15);
    assertEquals(fwdStartingCDS.getCashSettleTime(), fwdStartingCDS2.getCashSettleTime(), 1e-15);
    assertEquals(fwdStartingCDS.getProtectionEnd(), fwdStartingCDS2.getProtectionEnd(), 1e-15);
    for (int i = 0; i < fwdStartingCDS.getNumPayments(); i++) {
      final CDSCoupon c1 = fwdStartingCDS.getCoupon(i);
      final CDSCoupon c2 = fwdStartingCDS2.getCoupon(i);
      assertEquals(c1.getEffStart(), c2.getEffStart(), 1e-15);
      assertEquals(c1.getEffEnd(), c2.getEffEnd(), 1e-15);
      assertEquals(c1.getPaymentTime(), c2.getPaymentTime(), 1e-15);
      assertEquals(c1.getYearFrac(), c2.getYearFrac());
      assertEquals(c1.getYFRatio(), c2.getYFRatio());
    }
  }

}
