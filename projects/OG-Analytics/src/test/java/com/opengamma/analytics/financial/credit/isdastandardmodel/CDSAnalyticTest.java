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

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
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

}
