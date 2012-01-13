/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.instrument.index.generator.EURDeposit;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.PresentValueParallelCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.surface.StringValue;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the methods related to fixed coupons.
 */
public class PaymentFixedDiscountingMethodTest {

  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final GeneratorDeposit DEPOSIT_EUR = new EURDeposit(EUR_CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 12, 12);

  private static final Period PAYMENT_PERIOD = Period.ofMonths(12);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator
      .getAdjustedDate(REFERENCE_DATE, PAYMENT_PERIOD, DEPOSIT_EUR.getBusinessDayConvention(), EUR_CALENDAR, DEPOSIT_EUR.isEndOfMonth());
  private static final double AMOUNT = 100000000;
  private static final PaymentFixedDefinition PAYMENT_DEFINITION = new PaymentFixedDefinition(DEPOSIT_EUR.getCurrency(), PAYMENT_DATE, AMOUNT);

  private static final YieldCurveBundle YC_BUNDLE = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVES_NAME = TestsDataSetsSABR.curves2Names();

  private static final PaymentFixed PAYMENT = PAYMENT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final PaymentFixedDiscountingMethod METHOD = PaymentFixedDiscountingMethod.getInstance();
  private static final PresentValueParallelCurveSensitivityCalculator PVPCSC = PresentValueParallelCurveSensitivityCalculator.getInstance();

  @Test
  /**
   * Tests the present value of fixed coupons.
   */
  public void presentValue() {
    CurrencyAmount pvComputed = METHOD.presentValue(PAYMENT, YC_BUNDLE);
    double pvExpected = PAYMENT.getAmount() * YC_BUNDLE.getCurve(CURVES_NAME[0]).getDiscountFactor(PAYMENT.getPaymentTime());
    assertEquals("CouponFixed: Present value by discounting", pvExpected, pvComputed.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed payments.
   */
  public void presentValueParallelCurveSensitivity() {
    StringValue pvpcsComputed = METHOD.presentValueParallelCurveSensitivity(PAYMENT, YC_BUNDLE);
    double pvpcsExpected = -PAYMENT.getPaymentTime() * PAYMENT.getAmount() * YC_BUNDLE.getCurve(CURVES_NAME[0]).getDiscountFactor(PAYMENT.getPaymentTime());
    assertEquals("CouponFixed: Present value parallel curve sensitivity by discounting", 1, pvpcsComputed.getMap().size());
    assertEquals("CouponFixed: Present value parallel curve sensitivity by discounting", pvpcsExpected, pvpcsComputed.getMap().get(CURVES_NAME[0]), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed payments.
   */
  public void presentValueParallelCurveSensitivityMethodVsCalculator() {
    StringValue pvpcsMethod = METHOD.presentValueParallelCurveSensitivity(PAYMENT, YC_BUNDLE);
    StringValue pvpcsCalculator = PVPCSC.visit(PAYMENT, YC_BUNDLE);
    assertTrue("CouponFixed: Present value parallel curve sensitivity by discounting", StringValue.compare(pvpcsMethod, pvpcsCalculator, 1.0E-5));
  }

}
