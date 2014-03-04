/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getNextIMMDate;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CDSAnalyticTest {
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

  /**
   * 
   */
  @Test
  public void FactoryConsistencyTest() {
    final CDSAnalyticFactory fromInterval = new CDSAnalyticFactory(Period.ofMonths(3));
    final CDSAnalyticFactory fromRateAndInterval = new CDSAnalyticFactory(0.4, Period.ofMonths(3));
    final CDSAnalyticFactory fromOther = new CDSAnalyticFactory(FACTORY);
    final CDSAnalyticFactory fromStepIn = FACTORY.withStepIn(1);
    final CDSAnalyticFactory fromCashSettle = FACTORY.withCashSettle(3);
    final CDSAnalyticFactory fromAccOnDefault = FACTORY.withPayAccOnDefault(true);
    final CDSAnalyticFactory fromProtStart = FACTORY.withProtectionStart(true);
    final CDSAnalyticFactory fromBussDayConv = FACTORY.with(BusinessDayConventions.FOLLOWING);
    final CDSAnalyticFactory fromDayCoConv = FACTORY.withCurveDCC(DayCounts.ACT_365);
    final CDSAnalyticFactory[] set = new CDSAnalyticFactory[] {fromInterval, fromRateAndInterval, fromOther, fromStepIn, fromCashSettle, fromAccOnDefault, fromProtStart, fromBussDayConv,
        fromDayCoConv };

    final LocalDate accStart = LocalDate.of(2011, Month.MARCH, 21);
    final LocalDate maturity = LocalDate.of(2014, Month.SEPTEMBER, 20);
    final LocalDate tradeDate = LocalDate.of(2011, Month.APRIL, 18);
    final int n = set.length;

    final CDSAnalytic cdsBase = FACTORY.makeCDS(tradeDate, accStart, maturity);
    for (int i = 0; i < n; ++i) {
      final CDSAnalytic cds = set[i].makeCDS(tradeDate, accStart, maturity);
      assertEquals(cdsBase.isPayAccOnDefault(), cds.isPayAccOnDefault());
      assertEquals(cdsBase.getLGD(), cds.getLGD());
      assertEquals(cdsBase.getCashSettleTime(), cds.getCashSettleTime());
      assertEquals(cdsBase.getAccStart(), cds.getAccStart());
      assertEquals(cdsBase.getEffectiveProtectionStart(), cds.getEffectiveProtectionStart());
      assertEquals(cdsBase.getProtectionEnd(), cds.getProtectionEnd());
      assertEquals(cdsBase.getNumPayments(), cds.getNumPayments());
      final int m = cdsBase.getCoupons().length;
      for (int j = 0; j < m; ++j) {
        assertEquals(cdsBase.getCoupon(j), cds.getCoupon(j));
      }
      assertEquals(cdsBase.getAccruedYearFraction(), cds.getAccruedYearFraction());
      assertEquals(cdsBase.getAccuredDays(), cds.getAccuredDays());
    }
  }

  /**
   * 
   */
  @Test
  public void makeIMMCDSBusinessDayTest() {
    final LocalDate tradeDate = LocalDate.of(2011, Month.APRIL, 15);
    final Period tenor = Period.ofMonths(3);
    final DayCount accDCC = DayCounts.ACT_365;

    final CDSAnalytic cdsF = FACTORY.makeIMMCDS(tradeDate, tenor, false);
    final double accStartF = cdsF.getAccStart();
    final double expF = -accDCC.getDayCountFraction(IMMDateLogic.getPrevIMMDate(tradeDate), tradeDate);
    assertEquals(expF, accStartF);

    final CDSAnalytic cdsT = FACTORY.makeIMMCDS(tradeDate, tenor, true);
    final double accStartT = cdsT.getAccStart();
    final double expT = -accDCC.getDayCountFraction(IMMDateLogic.getPrevIMMDate(tradeDate).plusDays(1), tradeDate);
    assertEquals(expT, accStartT);

    final CDSAnalytic[] cdsF1 = FACTORY.makeIMMCDS(tradeDate, new Period[] {tenor }, false);
    final double accStartF1 = cdsF1[0].getAccStart();
    assertEquals(expF, accStartF1);

    final CDSAnalytic[] cdsT1 = FACTORY.makeIMMCDS(tradeDate, new Period[] {tenor }, true);
    final double accStartT1 = cdsT1[0].getAccStart();
    assertEquals(expT, accStartT1);
  }

  /**
   * 
   */
  @Test
  public void makeForwardStartingCDXTest() {
    final LocalDate tradeDate = LocalDate.of(2011, Month.APRIL, 15);
    final Period tenor = Period.ofMonths(3);
    final LocalDate fwdStartDate = tradeDate.plusDays(90);
    final DayCount accDCC = DayCounts.ACT_365;

    final CDSAnalytic cdsi = FACTORY.makeForwardStartingCDX(tradeDate, fwdStartDate, tenor);
    final double accStart = cdsi.getAccStart();
    final double exp = accDCC.getDayCountFraction(tradeDate, IMMDateLogic.getPrevIMMDate(fwdStartDate));
    assertEquals(exp, accStart);
  }

  /**
   * 
   */
  @Test
  public void MultiCDSAnalyticTest() {
    final LocalDate tradeDate = LocalDate.of(2011, Month.APRIL, 15);
    LocalDate nextIMM = getNextIMMDate(tradeDate);
    final int index = 3;
    final DayCount accDCC = DayCounts.ACT_365;
    final Calendar calendar = new MondayToFridayCalendar("Weekend_Only");
    final Period prd = Period.ofMonths(3);

    final MultiCDSAnalytic cdsM = FACTORY.makeMultiCDS(tradeDate, nextIMM, index);
    for (int i = 0; i < index; ++i) {
      final double end = cdsM.getProtectionEnd(i);
      final double exp = accDCC.getDayCountFraction(tradeDate, nextIMM, calendar);
      assertEquals(exp, end);
      nextIMM = IMMDateLogic.getNextIMMDate(nextIMM);
    }

    final int month1 = 3;
    final int month2 = 12;
    final int nTenors = (month2 - month1) / 3 + 1;
    final Period firstTenor = Period.ofMonths(month1);
    final Period lastTenor = Period.ofMonths(month2);

    final MultiCDSAnalytic cdsm = FACTORY.makeMultiIMMCDS(tradeDate, firstTenor, lastTenor);
    for (int i = 0; i < nTenors; ++i) {
      final double end = cdsm.getProtectionEnd(i);

      final Period tStep = prd.multipliedBy(i + month1 / 3);
      final LocalDate endDat = IMMDateLogic.getNextIMMDate(tradeDate).plus(tStep);
      final double exp = accDCC.getDayCountFraction(tradeDate, endDat, calendar);
      assertEquals(exp, end);
    }

    /*
     * Error caught
     */
    try {
      FACTORY.makeMultiIMMCDS(tradeDate, Period.ofMonths(2), lastTenor);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      FACTORY.makeMultiIMMCDS(tradeDate, firstTenor, Period.ofMonths(19));
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    try {
      FACTORY.makeMultiIMMCDS(tradeDate, 5, 4);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      FACTORY.makeMultiIMMCDS(tradeDate, -1, 4);
      throw new RuntimeException();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

  }
}
