/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueParallelCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.amount.StringAmount;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the methods related to fixed coupons.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CouponFixedDiscountingMethodTest {

  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("TARGET");

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final GeneratorDeposit DEPOSIT_EUR = new EURDeposit(EUR_CALENDAR);

  private static final Period START_PERIOD = Period.ofMonths(6);
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, START_PERIOD, DEPOSIT_EUR.getBusinessDayConvention(), EUR_CALENDAR, DEPOSIT_EUR.isEndOfMonth());
  private static final Period CPN_TENOR = Period.ofMonths(12);
  private static final double NOTIONAL = 100000000;
  private static final double FIXED_RATE = 0.0250;
  private static final CouponFixedDefinition CPN_REC_DEFINITION = CouponFixedDefinition.from(START_DATE, CPN_TENOR, DEPOSIT_EUR, NOTIONAL, FIXED_RATE);
  private static final CouponFixedDefinition CPN_PAY_DEFINITION = CouponFixedDefinition.from(START_DATE, CPN_TENOR, DEPOSIT_EUR, -NOTIONAL, FIXED_RATE);

  private static final YieldCurveBundle YC_BUNDLE = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVES_NAME = TestsDataSetsSABR.curves2Names();

  private static final CouponFixed CPN_REC = CPN_REC_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponFixed CPN_PAY = CPN_PAY_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final CouponFixedDiscountingMethod METHOD = CouponFixedDiscountingMethod.getInstance();
  private static final PresentValueParallelCurveSensitivityCalculator PVPCSC = PresentValueParallelCurveSensitivityCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  /**
   * Tests the present value of fixed coupons.
   */
  public void presentValue() {
    final CurrencyAmount pvRecComputed = METHOD.presentValue(CPN_REC, YC_BUNDLE);
    final double pvExpected = CPN_REC.getAmount() * YC_BUNDLE.getCurve(CURVES_NAME[0]).getDiscountFactor(CPN_REC.getPaymentTime());
    assertEquals("CouponFixed: Present value by discounting", pvExpected, pvRecComputed.getAmount(), TOLERANCE_PV);
    final CurrencyAmount pvPayComputed = METHOD.presentValue(CPN_PAY, YC_BUNDLE);
    assertEquals("CouponFixed: Present value by discounting", -pvRecComputed.getAmount(), pvPayComputed.getAmount(), TOLERANCE_PV);
    final CurrencyAmount pvPosPayComputed = METHOD.presentValuePositiveNotional(CPN_REC, YC_BUNDLE);
    assertEquals("CouponFixed: Present value by discounting", pvRecComputed.getAmount(), pvPosPayComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed coupons.
   */
  public void presentValueParallelCurveSensitivity() {
    final StringAmount pvpcsComputed = METHOD.presentValueParallelCurveSensitivity(CPN_PAY, YC_BUNDLE);
    final double pvpcsExpected = -CPN_PAY.getPaymentTime() * CPN_PAY.getAmount() * YC_BUNDLE.getCurve(CURVES_NAME[0]).getDiscountFactor(CPN_PAY.getPaymentTime());
    assertEquals("CouponFixed: Present value parallel curve sensitivity by discounting", 1, pvpcsComputed.getMap().size());
    assertEquals("CouponFixed: Present value parallel curve sensitivity by discounting", pvpcsExpected, pvpcsComputed.getMap().get(CURVES_NAME[0]), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed coupons.
   */
  public void presentValueParallelCurveSensitivityMethodVsCalculator() {
    final StringAmount pvpcsMethod = METHOD.presentValueParallelCurveSensitivity(CPN_PAY, YC_BUNDLE);
    final StringAmount pvpcsCalculator = CPN_PAY.accept(PVPCSC, YC_BUNDLE);
    assertTrue("CouponFixed: Present value parallel curve sensitivity by discounting", StringAmount.compare(pvpcsMethod, pvpcsCalculator, 1.0E-5));
  }

}
