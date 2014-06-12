/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MultiCDSAnalyticTest extends ISDABaseTest {

  @Test
  public void test() {
    final CDSAnalyticFactory factory = new CDSAnalyticFactory();
    final LocalDate tradeDate = LocalDate.of(2013, 12, 19);
    MultiCDSAnalytic multiCDS = factory.makeMultiIMMCDS(tradeDate, new int[] {0, 1, 2, 4 });

    CDSAnalytic[] cds = factory.makeIMMCDS(tradeDate, new Period[] {Period.ZERO, Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(12) });

    for (int i = 0; i < 4; i++) {
      assertEquals(cds[i].getAccuredDays(), multiCDS.getAccuredDays(i));
      assertEquals(cds[i].getAccruedYearFraction(), multiCDS.getAccruedPremiumPerUnitSpread(i), 1e-16);
      assertEquals(cds[i].getEffectiveProtectionStart(), multiCDS.getEffectiveProtectionStart(), 1e-16);
      assertEquals(cds[i].getAccStart(), multiCDS.getAccStart(), 1e-16);
    }

    multiCDS = factory.makeMultiIMMCDS(tradeDate, new int[] {2, 4, 12, 20, 28, 40 });
    cds = factory.makeIMMCDS(tradeDate, new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) });
    for (int i = 0; i < 6; i++) {
      assertEquals(cds[i].getAccuredDays(), multiCDS.getAccuredDays(i));
      assertEquals(cds[i].getAccruedYearFraction(), multiCDS.getAccruedPremiumPerUnitSpread(i), 1e-16);
      assertEquals(cds[i].getEffectiveProtectionStart(), multiCDS.getEffectiveProtectionStart(), 1e-16);
      assertEquals(cds[i].getAccStart(), multiCDS.getAccStart(), 1e-16);
    }
  }

  @Test
  public void forwardStartTest() {
    final CDSAnalyticFactory factory = new CDSAnalyticFactory();
    final LocalDate tradeDate = LocalDate.of(2012, 7, 30);
    final LocalDate accStart = LocalDate.of(2013, 6, 20);
    final MultiCDSAnalytic multiCDS = factory.makeMultiIMMCDS(tradeDate, accStart, new int[] {4, 5, 6, 8 });

    final CDSAnalytic[] cds = factory.makeIMMCDS(tradeDate, accStart, new Period[] {Period.ofMonths(12), Period.ofMonths(15), Period.ofMonths(18), Period.ofMonths(24) });

    for (int i = 0; i < 4; i++) {
      assertEquals(cds[i].getAccuredDays(), multiCDS.getAccuredDays(i));
      assertEquals(cds[i].getAccruedYearFraction(), multiCDS.getAccruedPremiumPerUnitSpread(i), 1e-16);
      assertEquals(cds[i].getEffectiveProtectionStart(), multiCDS.getEffectiveProtectionStart(), 1e-16);
      assertEquals(cds[i].getAccStart(), multiCDS.getAccStart(), 1e-16);
    }
  }
}
