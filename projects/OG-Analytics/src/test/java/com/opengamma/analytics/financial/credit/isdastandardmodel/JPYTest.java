/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;
import static org.threeten.bp.Month.OCTOBER;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class JPYTest extends ISDABaseTest {

  private static final TYOCalendar TYO_CAL = new TYOCalendar("TYO");
  private static final MarketQuoteConverter CONVERTER = new MarketQuoteConverter();
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory(0.35).with(TYO_CAL);
  private static final FiniteDifferenceSpreadSensitivityCalculator CS01_CAL = new FiniteDifferenceSpreadSensitivityCalculator();

  @Test
  public void test() {
    final double coupon = 0.01;
    final double tradeLevel = 0.012;
    final QuotedSpread qs = new QuotedSpread(coupon, tradeLevel);
    final double notional = 1e13;

    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.001107, 0.001279, 0.001429, 0.002111, 0.003943, 0.002163, 0.002525, 0.003075, 0.003763, 0.004575, 0.00545, 0.006375, 0.007288, 0.008213, 0.010088, 0.012763,
      0.01585, 0.017925 };

    final LocalDate tradeDate = LocalDate.of(2013, OCTOBER, 16);
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 3, TYO_CAL);

    final ISDACompliantYieldCurve yieldCurve = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, ACT_ACT_ISDA, Period.ofMonths(6), TYO_CAL);
    final CDSAnalytic cds = FACTORY.makeIMMCDS(tradeDate, Period.ofYears(5));

    final PointsUpFront puf = CONVERTER.convert(cds, qs, yieldCurve);
    final double accAmt = notional * cds.getAccruedPremium(coupon);
    final double cashSettle = notional * puf.getPointsUpFront() - accAmt;
    final double cs01 = notional * ONE_BP * CS01_CAL.parallelCS01(cds, qs, yieldCurve, ONE_BP);

    assertEquals(27, cds.getAccuredDays());
    assertEquals(7.5e9, accAmt);
    assertEquals("cashSettle", 91814571779.0, cashSettle, 1);
    assertEquals("CS01", 4924458158.0, cs01, 1);

    //    System.out.println("Accrued Days: " + cds.getAccuredDays());
    //    System.out.println("Accrued Amt: " + accAmt);
    //    System.out.println("PUF: " + 100 * puf.getPointsUpFront() + "%");
    //    System.out.println("clean price: " + 100 * (1 - puf.getPointsUpFront()) + "%");
    //    System.out.println("Cash Settlement: " + cashSettle);
    //    System.out.println("CS01: " + cs01);
  }

}
