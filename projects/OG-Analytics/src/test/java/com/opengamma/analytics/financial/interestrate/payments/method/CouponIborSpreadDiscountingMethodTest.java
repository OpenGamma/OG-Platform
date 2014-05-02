/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing and sensitivities of Ibor coupon with spread in the discounting method.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CouponIborSpreadDiscountingMethodTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final IborIndex EURIBOR3M = IndexIborMaster.getInstance().getIndex("EURIBOR3M");

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 31);
  private static final Period START_TENOR = Period.ofMonths(6);
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, START_TENOR, EURIBOR3M, TARGET);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, EURIBOR3M, TARGET);
  private static final double NOTIONAL = -123000000;
  private static final double ACCURAL = 0.25;
  private static final double SPREAD = 0.0010;

  private static final CouponIborSpreadDefinition CPN_IBOR_SPREAD_DEFINITION = CouponIborSpreadDefinition.from(START_DATE, END_DATE, ACCURAL, NOTIONAL, EURIBOR3M, SPREAD, TARGET);
  private static final CouponIborDefinition CPN_IBOR_DEFINITION = CouponIborDefinition.from(START_DATE, END_DATE, ACCURAL, NOTIONAL, EURIBOR3M, TARGET);
  private static final CouponFixedDefinition CPN_FIXED_DEFINITION = CouponFixedDefinition.from(CPN_IBOR_DEFINITION, SPREAD);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves1Names();

  private static final CouponIborSpread CPN_IBOR_SPREAD = (CouponIborSpread) CPN_IBOR_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
  private static final CouponIbor CPN_IBOR = (CouponIbor) CPN_IBOR_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
  private static final CouponFixed CPN_FIXED = CPN_FIXED_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);

  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingMethod METHOD_FIXED = CouponFixedDiscountingMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-8;

  @Test
  public void presentValue() {
    final CurrencyAmount pvComputed = METHOD_CPN_IBOR_SPREAD.presentValue(CPN_IBOR_SPREAD, CURVES);
    final double forward = (CURVES.getCurve(CURVE_NAMES[1]).getDiscountFactor(CPN_IBOR_SPREAD.getFixingPeriodStartTime())
        / CURVES.getCurve(CURVE_NAMES[1]).getDiscountFactor(CPN_IBOR_SPREAD.getFixingPeriodEndTime()) - 1.0)
        / CPN_IBOR_SPREAD.getFixingAccrualFactor();
    final double pv = NOTIONAL * CPN_IBOR_SPREAD.getPaymentYearFraction() * (forward + SPREAD) * CURVES.getCurve(CURVE_NAMES[0]).getDiscountFactor(CPN_IBOR_SPREAD.getPaymentTime());
    final CurrencyAmount pvExpected = CurrencyAmount.of(EURIBOR3M.getCurrency(), pv);
    assertEquals("CouponIborSpreadDiscountingMethod: present value", pvExpected.getAmount(), pvComputed.getAmount(), TOLERANCE_PV);
    final CurrencyAmount pvIbor = METHOD_CPN_IBOR.presentValue(CPN_IBOR, CURVES);
    final CurrencyAmount pvFixed = METHOD_FIXED.presentValue(CPN_FIXED, CURVES);
    assertEquals("CouponIborSpreadDiscountingMethod: present value", pvIbor.plus(pvFixed).getAmount(), pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueNoSpreadPositivNotional() {
    final CurrencyAmount pvComputed = METHOD_CPN_IBOR_SPREAD.presentValueNoSpreadPositiveNotional(CPN_IBOR_SPREAD, CURVES);
    final double forward = (CURVES.getCurve(CURVE_NAMES[1]).getDiscountFactor(CPN_IBOR_SPREAD.getFixingPeriodStartTime())
        / CURVES.getCurve(CURVE_NAMES[1]).getDiscountFactor(CPN_IBOR_SPREAD.getFixingPeriodEndTime()) - 1.0)
        / CPN_IBOR_SPREAD.getFixingAccrualFactor();
    final double pv = -NOTIONAL * CPN_IBOR_SPREAD.getPaymentYearFraction() * forward * CURVES.getCurve(CURVE_NAMES[0]).getDiscountFactor(CPN_IBOR_SPREAD.getPaymentTime());
    final CurrencyAmount pvExpected = CurrencyAmount.of(EURIBOR3M.getCurrency(), pv);
    assertEquals("CouponIborSpreadDiscountingMethod: present value", pvExpected.getAmount(), pvComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    InterestRateCurveSensitivity pvcsComputed = METHOD_CPN_IBOR_SPREAD.presentValueCurveSensitivity(CPN_IBOR_SPREAD, CURVES);
    pvcsComputed = pvcsComputed.cleaned();
    final InterestRateCurveSensitivity pvcsIbor = METHOD_CPN_IBOR.presentValueCurveSensitivity(CPN_IBOR, CURVES);
    final InterestRateCurveSensitivity pvcsFixed = METHOD_FIXED.presentValueCurveSensitivity(CPN_FIXED, CURVES);
    final InterestRateCurveSensitivity pvcsExpected = pvcsIbor.plus(pvcsFixed).cleaned();
    AssertSensitivityObjects.assertEquals("CouponIborSpreadDiscountingMethod: present value curve sensitivity", pvcsExpected, pvcsComputed, TOLERANCE_PV);
  }

  @Test
  public void parRate() {
    final double prComputed = METHOD_CPN_IBOR_SPREAD.parRate(CPN_IBOR_SPREAD, CURVES);
    final double prExpected = METHOD_CPN_IBOR.parRate(CPN_IBOR, CURVES);
    assertEquals("CouponIborSpreadDiscountingMethod: par rate", prExpected, prComputed, TOLERANCE_RATE);
  }

  @Test
  public void parRateCurveSensitivity() {
    InterestRateCurveSensitivity prcsComputed = METHOD_CPN_IBOR_SPREAD.parRateCurveSensitivity(CPN_IBOR_SPREAD, CURVES);
    prcsComputed = prcsComputed.cleaned();
    InterestRateCurveSensitivity prcsIbor = METHOD_CPN_IBOR.parRateCurveSensitivity(CPN_IBOR, CURVES);
    prcsIbor = prcsIbor.cleaned();
    AssertSensitivityObjects.assertEquals("CouponIborSpreadDiscountingMethod: present value curve sensitivity", prcsIbor, prcsComputed, TOLERANCE_PV);
  }
}
