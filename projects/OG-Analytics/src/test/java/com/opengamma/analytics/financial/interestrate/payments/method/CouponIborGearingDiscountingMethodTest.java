/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing and sensitivities of Ibor coupon with gearing factor and spread in the discounting method.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CouponIborGearingDiscountingMethodTest {
  // The index: Libor 3m
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Coupon
  private static final DayCount DAY_COUNT_COUPON = DayCounts.ACT_365;
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final double FACTOR = 2.0;
  private static final double SPREAD = 0.0050;
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final CouponIborGearingDefinition COUPON_DEFINITION = new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
      FIXING_DATE, INDEX, SPREAD, FACTOR, CALENDAR);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final YieldCurveBundle CURVES_BUNDLE = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVES_NAMES = CURVES_BUNDLE.getAllNames().toArray(new String[CURVES_BUNDLE.size()]);
  private static final CouponIborGearingDiscountingMethod METHOD = CouponIborGearingDiscountingMethod.getInstance();
  private static final CouponIborGearing COUPON = COUPON_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAMES);
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();

  @Test
  /**
   * Tests the present value.
   */
  public void presentValue() {
    final CurrencyAmount pv = METHOD.presentValue(COUPON, CURVES_BUNDLE);
    final CouponIborDefinition couponIborDefinition = new CouponIborDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX, CALENDAR);
    final Payment couponIbor = couponIborDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    final CouponFixedDefinition couponFixedDefinition = new CouponFixedDefinition(couponIborDefinition, SPREAD);
    final Payment couponFixed = couponFixedDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    final PresentValueCalculator pvc = PresentValueCalculator.getInstance();
    final double pvIbor = couponIbor.accept(pvc, CURVES_BUNDLE);
    final double pvFixed = couponFixed.accept(pvc, CURVES_BUNDLE);
    assertEquals("Present value by discounting", pvIbor * FACTOR + pvFixed, pv.getAmount());
  }

  @Test
  /**
   * Tests the present value in method vs calculator.
   */
  public void presentValueMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD.presentValue(COUPON, CURVES_BUNDLE);
    final double pvCalculator = COUPON.accept(PVC, CURVES_BUNDLE);
    assertEquals("Coupon with gearing and spread - present value: Method vs Calculator", pvMethod.getAmount(), pvCalculator);
  }

  @Test
  /**
   * Test the present value curves sensitivity.
   */
  public void presentValueCurveSensitivity() {
    InterestRateCurveSensitivity pvsFuture = METHOD.presentValueCurveSensitivity(COUPON, CURVES_BUNDLE);
    pvsFuture = pvsFuture.cleaned();
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final Payment couponBumpedForward = COUPON_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAMES[0], bumpedCurveName });
    final double[] nodeTimesForward = new double[] {COUPON.getFixingPeriodStartTime(), COUPON.getFixingPeriodEndTime() };
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(couponBumpedForward, CURVES_BUNDLE, CURVES_NAMES[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvsFuture.getSensitivities().get(CURVES_NAMES[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final Payment couponBumpedDisc = COUPON_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {bumpedCurveName, CURVES_NAMES[1] });
    final double[] nodeTimesDisc = new double[] {COUPON.getPaymentTime() };
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(couponBumpedDisc, CURVES_BUNDLE, CURVES_NAMES[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiDiscMethod.length);
    final List<DoublesPair> sensiPvDisc = pvsFuture.getSensitivities().get(CURVES_NAMES[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Tests the present value curve sensitivity in method vs calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final InterestRateCurveSensitivity pvcsMethod = METHOD.presentValueCurveSensitivity(COUPON, CURVES_BUNDLE);
    final Map<String, List<DoublesPair>> pvcsCalculator = COUPON.accept(PVCSC, CURVES_BUNDLE);
    assertEquals("Coupon with gearing and spread - present value curve sensitivity: Method vs Calculator", pvcsMethod.getSensitivities(), pvcsCalculator);
  }

}
