/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class AnnuityForSpreadFunctionTest {

  private static final LocalDate TRADE_DATE = LocalDate.of(2011, Month.JUNE, 13);
  private static final double[] YIELD_CURVE_RATES = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017,
      0.03092, 0.0316, 0.03231, 0.03367, 0.03419, 0.03411, 0.03412 };

  private static final Period TENOR = Period.ofYears(10);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("Weekend_Only");

  private static final ISDACompliantYieldCurve YIELD_CURVE;

  static {
    int num;
    final int[] mmMonths = new int[] {1, 2, 3, 6, 9, 12 };
    final int[] swapYears = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 20, 25, 30 };
    final int nMoneyMarket = mmMonths.length;
    final int nSwaps = swapYears.length;
    num = nMoneyMarket + nSwaps;

    final ISDAInstrumentTypes[] instrumentTypes = new ISDAInstrumentTypes[num];
    final Period[] tenors = new Period[num];
    for (int i = 0; i < nMoneyMarket; i++) {
      instrumentTypes[i] = ISDAInstrumentTypes.MoneyMarket;
      tenors[i] = Period.ofMonths(mmMonths[i]);
    }
    for (int i = nMoneyMarket; i < num; i++) {
      instrumentTypes[i] = ISDAInstrumentTypes.Swap;
      tenors[i] = Period.ofYears(swapYears[i - nMoneyMarket]);
    }

    final ISDACompliantYieldCurveBuild builder = new ISDACompliantYieldCurveBuild(TRADE_DATE, TRADE_DATE.plusDays(2), instrumentTypes, tenors, DayCounts.ACT_360, DayCounts.THIRTY_U_360,
        Period.ofYears(1), DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, CALENDAR);
    YIELD_CURVE = builder.build(YIELD_CURVE_RATES);
  }

  /**
   * 
   */
  @Test
  public void shortAccPeriodTest() {
    final Period interval = Period.ofDays(4);

    final CDSAnalytic cds = new CDSAnalytic(TRADE_DATE, TRADE_DATE.plusDays(1), addWorkDays(TRADE_DATE, 3, CALENDAR), IMMDateLogic.getPrevIMMDate(TRADE_DATE), IMMDateLogic.getNextIMMDate(TRADE_DATE)
        .plus(TENOR), true, interval, StubType.FRONTSHORT, true, 0.4);
    final AnnuityForSpreadContPemiumApproxFunction contPrem = new AnnuityForSpreadContPemiumApproxFunction(cds, YIELD_CURVE);
    final AnnuityForSpreadISDAFunction isda = new AnnuityForSpreadISDAFunction(cds, YIELD_CURVE);
    final AnnuityForSpreadApproxFunction approx = new AnnuityForSpreadApproxFunction(cds, YIELD_CURVE);

    final double spread = 100 * 1.e-4;

    final double integralWithTriangle = contPrem.evaluate(spread);
    final double sumWithCalibrate = isda.evaluate(spread);
    final double sumWithTriangle = approx.evaluate(spread);
    final double ref = sumWithCalibrate * 1.e-3;
    assertEquals(sumWithCalibrate, integralWithTriangle, ref);
    assertEquals(sumWithCalibrate, sumWithTriangle, ref);
  }

  /**
   * 
   */
  @Test
  public void shortTenorTest() {
    final Period interval = Period.ofMonths(3);
    final Period tenor = Period.ofMonths(3);

    final CDSAnalytic cds = new CDSAnalytic(TRADE_DATE, TRADE_DATE.plusDays(1), addWorkDays(TRADE_DATE, 3, CALENDAR), IMMDateLogic.getPrevIMMDate(TRADE_DATE), IMMDateLogic.getNextIMMDate(TRADE_DATE)
        .plus(tenor), true, interval, StubType.FRONTSHORT, true, 0.4);
    final AnnuityForSpreadContPemiumApproxFunction contPrem = new AnnuityForSpreadContPemiumApproxFunction(cds, YIELD_CURVE);
    final AnnuityForSpreadISDAFunction isda = new AnnuityForSpreadISDAFunction(cds, YIELD_CURVE);
    final AnnuityForSpreadApproxFunction approx = new AnnuityForSpreadApproxFunction(cds, YIELD_CURVE);

    final double spread = 100 * 1.e-4;

    final double integralWithTriangle = contPrem.evaluate(spread);
    final double sumWithCalibrate = isda.evaluate(spread);
    final double sumWithTriangle = approx.evaluate(spread);
    final double ref = sumWithCalibrate * 1.e-2;
    assertEquals(sumWithCalibrate, integralWithTriangle, ref);
    assertEquals(sumWithCalibrate, sumWithTriangle, ref);
  }
}
