/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.instrument.payment.CouponOISSimplifiedDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedOISSimplifiedDefinition;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of OIS coupon by discounting.
 */
public class CouponOISDiscountingMethodTest {
  private static final int EUR_SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention EUR_BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean EUR_IS_EOM = true;
  //EUR Eonia
  private static final String EUR_OIS_NAME = "EUR EONIA";
  private static final Currency EUR_CUR = Currency.EUR;
  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("EUR");
  private static final int EUR_PUBLICATION_LAG = 0;
  private static final DayCount EUR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IndexON EUR_OIS = new IndexON(EUR_OIS_NAME, EUR_CUR, EUR_DAY_COUNT, EUR_PUBLICATION_LAG, EUR_CALENDAR);
  // Coupon EONIA 3m
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, EUR_SETTLEMENT_DAYS, EUR_CALENDAR);
  private static final Period EUR_CPN_TENOR = Period.ofMonths(3);
  private static final ZonedDateTime START_ACCRUAL_DATE = SPOT_DATE;
  private static final ZonedDateTime END_ACCRUAL_DATE = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, EUR_CPN_TENOR, EUR_BUSINESS_DAY, EUR_CALENDAR, EUR_IS_EOM);
  private static ZonedDateTime LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(END_ACCRUAL_DATE, -1, EUR_CALENDAR); // Overnight
  static {
    LAST_FIXING_DATE = ScheduleCalculator.getAdjustedDate(LAST_FIXING_DATE, EUR_PUBLICATION_LAG, EUR_CALENDAR); // Lag
  }
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(LAST_FIXING_DATE, EUR_SETTLEMENT_DAYS, EUR_CALENDAR);
  private static final double PAYMENT_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final double NOTIONAL = 100000000;
  private static final double FIXING_YEAR_FRACTION = EUR_DAY_COUNT.getDayCountFraction(START_ACCRUAL_DATE, END_ACCRUAL_DATE);
  private static final CouponOISSimplifiedDefinition EONIA_COUPON_DEFINITION = new CouponOISSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION,
      NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVES_NAMES = CURVES.getAllNames().toArray(new String[0]);

  private static final ZonedDateTime REFERENCE_DATE_1 = TRADE_DATE;
  private static final double PAYMENT_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, PAYMENT_DATE);
  private static final double START_ACCRUAL_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, START_ACCRUAL_DATE);
  private static final double END_ACCRUAL_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, END_ACCRUAL_DATE);
  private static final CouponOIS EONIA_COUPON_NOTSTARTED = new CouponOIS(EUR_CUR, PAYMENT_TIME_1, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_TIME_1, END_ACCRUAL_TIME_1,
      FIXING_YEAR_FRACTION, NOTIONAL, CURVES_NAMES[1]);

  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2011, 10, 7);
  private static final ZonedDateTime NEXT_FIXING_DATE_2 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_2, 1, EUR_CALENDAR); // Overnight
  private static final double PAYMENT_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, PAYMENT_DATE);
  private static final double START_FIXING_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, NEXT_FIXING_DATE_2);
  private static final double END_FIXING_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, END_ACCRUAL_DATE);
  private static final double FIXING_YEAR_FRACTION_2 = EUR_DAY_COUNT.getDayCountFraction(NEXT_FIXING_DATE_2, END_ACCRUAL_DATE);
  private static final double NOTIONAL_WITH_ACCRUED = NOTIONAL * (1.0 + 0.01 / 12); // 1% over a month (roughly)
  private static final CouponOIS EONIA_COUPON_STARTED = new CouponOIS(EUR_CUR, PAYMENT_TIME_2, CURVES_NAMES[0], FIXING_YEAR_FRACTION_2, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2,
      FIXING_YEAR_FRACTION_2, NOTIONAL_WITH_ACCRUED, CURVES_NAMES[1]);

  private static final CouponOISDiscountingMethod METHOD_OIS = new CouponOISDiscountingMethod();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueNotStarted() {
    final CurrencyAmount pv = METHOD_OIS.presentValue(EONIA_COUPON_NOTSTARTED, CURVES);
    double dfForwardStart = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(START_ACCRUAL_TIME_1);
    double dfForwardEnd = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(END_ACCRUAL_TIME_1);
    double dfPayment = CURVES.getCurve(CURVES_NAMES[0]).getDiscountFactor(PAYMENT_TIME_1);
    double forward = (dfForwardStart / dfForwardEnd - 1.0) / PAYMENT_YEAR_FRACTION;
    double pvExpected = forward * PAYMENT_YEAR_FRACTION * NOTIONAL * dfPayment;
    AssertJUnit.assertEquals("CouponOIS: Present value by discounting", pvExpected, pv.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueNotStartedMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD_OIS.presentValue(EONIA_COUPON_NOTSTARTED, CURVES);
    double pvCalculator = PVC.visit(EONIA_COUPON_NOTSTARTED, CURVES);
    AssertJUnit.assertEquals("CouponOIS: Present value by discounting", pvCalculator, pvMethod.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueStarted() {
    final CurrencyAmount pv = METHOD_OIS.presentValue(EONIA_COUPON_STARTED, CURVES);
    double dfForwardStart = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(START_FIXING_TIME_2);
    double dfForwardEnd = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(END_FIXING_TIME_2);
    double dfPayment = CURVES.getCurve(CURVES_NAMES[0]).getDiscountFactor(PAYMENT_TIME_2);
    double forward = (dfForwardStart / dfForwardEnd - 1.0) / FIXING_YEAR_FRACTION_2;
    double pvExpected = forward * FIXING_YEAR_FRACTION_2 * NOTIONAL_WITH_ACCRUED * dfPayment;
    AssertJUnit.assertEquals("CouponOIS: Present value by discounting", pvExpected, pv.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Tests the par rate.
   */
  public void parRateNotStarted() {
    final double pr = METHOD_OIS.parRate(EONIA_COUPON_NOTSTARTED, CURVES);
    double dfForwardStart = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(START_ACCRUAL_TIME_1);
    double dfForwardEnd = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(END_ACCRUAL_TIME_1);
    double forward = (dfForwardStart / dfForwardEnd - 1.0) / PAYMENT_YEAR_FRACTION;
    AssertJUnit.assertEquals("CouponOIS: par rate by discounting", forward, pr, 1.0E-10);
  }

  @Test
  /**
   * Tests the par rate.
   */
  public void parRateNotStartedMethodVsCalculator() {
    final double prMethod = METHOD_OIS.parRate(EONIA_COUPON_NOTSTARTED, CURVES);
    double prCalculator = PRC.visit(EONIA_COUPON_NOTSTARTED, CURVES);
    AssertJUnit.assertEquals("CouponOIS: par rate by discounting", prMethod, prCalculator, 1.0E-10);
  }

  @Test
  /**
   * Tests the present value rate sensitivity.
   */
  public void presentValueCurveSensitivityNotStarted() {
    final InterestRateCurveSensitivity pvcs = METHOD_OIS.presentValueCurveSensitivity(EONIA_COUPON_NOTSTARTED, CURVES);
    pvcs.clean();
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final Payment couponBumpedForward = EONIA_COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1, new String[] {CURVES_NAMES[0], bumpedCurveName});
    final double[] nodeTimesForward = new double[] {EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime(), EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime()};
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(couponBumpedForward, CURVES, CURVES_NAMES[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD_OIS);
    AssertJUnit.assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvcs.getSensitivities().get(CURVES_NAMES[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final Payment couponBumpedDisc = EONIA_COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1, new String[] {bumpedCurveName, CURVES_NAMES[1]});
    final double[] nodeTimesDisc = new double[] {EONIA_COUPON_NOTSTARTED.getPaymentTime()};
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(couponBumpedDisc, CURVES, CURVES_NAMES[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_OIS);
    AssertJUnit.assertEquals("Sensitivity finite difference method: number of node", 1, sensiDiscMethod.length);
    final List<DoublesPair> sensiPvDisc = pvcs.getSensitivities().get(CURVES_NAMES[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Tests the present value rate sensitivity: two curves identical.
   */
  public void presentValueCurveSensitivityOneCurveNotStarted() {
    CouponOIS eoniaCouponNotStartedOneCurve = new CouponOIS(EUR_CUR, PAYMENT_TIME_1, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_TIME_1, END_ACCRUAL_TIME_1,
        FIXING_YEAR_FRACTION, NOTIONAL, CURVES_NAMES[0]);
    final InterestRateCurveSensitivity pvcs = METHOD_OIS.presentValueCurveSensitivity(eoniaCouponNotStartedOneCurve, CURVES);
    pvcs.clean();
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    //    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    //    final Payment couponBumpedForward = EONIA_COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1, new String[] {CURVES_NAMES[0], bumpedCurveName});
    //    final double[] nodeTimesForward = new double[] {EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime(), EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime()};
    //    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(couponBumpedForward, CURVES, CURVES_NAMES[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD_OIS);
    //    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    //    final List<DoublesPair> sensiPvForward = pvcs.getSensitivities().get(CURVES_NAMES[1]);
    //    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
    //      final DoublesPair pairPv = sensiPvForward.get(loopnode);
    //      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
    //      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    //    }
    // Unique curve sensitivity
    final Payment couponBumpedDisc = EONIA_COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1, new String[] {bumpedCurveName, bumpedCurveName});
    final double[] nodeTimesDisc = new double[] {EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime(), EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime(), EONIA_COUPON_NOTSTARTED.getPaymentTime()};
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(couponBumpedDisc, CURVES, CURVES_NAMES[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_OIS);
    final List<DoublesPair> sensiPvDisc = pvcs.getSensitivities().get(CURVES_NAMES[0]);
    AssertJUnit.assertEquals("Sensitivity finite difference method: number of node", 3, sensiPvDisc.size());
    AssertJUnit.assertEquals("Sensitivity coupon pv to forward curve", nodeTimesDisc[0], sensiPvDisc.get(1).first, 1E-8);
    AssertJUnit.assertEquals("Sensitivity coupon pv to forward curve", nodeTimesDisc[1], sensiPvDisc.get(2).first, 1E-8);
    AssertJUnit.assertEquals("Sensitivity coupon pv to forward curve", nodeTimesDisc[2], sensiPvDisc.get(0).first, 1E-8);
    AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", sensiDiscMethod[0], sensiPvDisc.get(1).second, deltaTolerancePrice);
    AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", sensiDiscMethod[1], sensiPvDisc.get(2).second, deltaTolerancePrice);
    AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", sensiDiscMethod[2], sensiPvDisc.get(0).second, deltaTolerancePrice);
  }

  @Test
  /**
   * Tests the present value rate sensitivity.
   */
  public void presentValueCurveSensitivityNotStartedMethodVsCalculator() {
    InterestRateCurveSensitivity pvcsMethod = METHOD_OIS.presentValueCurveSensitivity(EONIA_COUPON_NOTSTARTED, CURVES);
    Map<String, List<DoublesPair>> pvcsCalculator = PVCSC.visit(EONIA_COUPON_NOTSTARTED, CURVES);
    AssertJUnit.assertEquals(pvcsCalculator.size(), pvcsMethod.getSensitivities().size());
    for (Map.Entry<String, List<DoublesPair>> entry : pvcsCalculator.entrySet()) {
      AssertJUnit.assertTrue(pvcsMethod.getSensitivities().containsKey(entry.getKey()));
      List<DoublesPair> firstPairs = entry.getValue();
      List<DoublesPair> secondPairs = pvcsMethod.getSensitivities().get(entry.getKey());
      AssertJUnit.assertEquals(firstPairs.size(), secondPairs.size());
      for(int i = 0; i < firstPairs.size(); i++) {
        assertEquals("CouponOIS: present value sensitivity by discounting", firstPairs.get(i).first, secondPairs.get(i).first, 1e-8);
        assertEquals("CouponOIS: present value sensitivity by discounting", firstPairs.get(i).second, secondPairs.get(i).second, 1e-8);
      }
    }
  }

  @Test
  /**
   * Tests the present value rate sensitivity.
   */
  public void parRateCurveSensitivityNotStarted() {
    final InterestRateCurveSensitivity prcs = METHOD_OIS.parRateCurveSensitivity(EONIA_COUPON_NOTSTARTED, CURVES);
    prcs.clean();
    final double deltaTolerancePrice = 1.0E-10;
    // 1. Forward curve sensitivity
    final double[] nodeTimesForward = new double[] {EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime(), EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime()};
    double dfForwardStart = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(START_ACCRUAL_TIME_1);
    double dfForwardEnd = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(END_ACCRUAL_TIME_1);
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / EONIA_COUPON_NOTSTARTED.getFixingPeriodAccrualFactor();
    final double dfForwardStartBar = 1.0 / (EONIA_COUPON_NOTSTARTED.getFixingPeriodAccrualFactor() * dfForwardEnd);
    final double[] sensiForwardMethod = new double[] {-EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar,
        -EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar};
    AssertJUnit.assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = prcs.getSensitivities().get(CURVES_NAMES[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", sensiForwardMethod[loopnode], pairPv.second, deltaTolerancePrice);
    }
  }

  // OIS swap
  // Swap EONIA 3M
  private static final double FIXED_RATE = 0.01;
  private static final boolean IS_PAYER = true;
  private static final Period EUR_SWAP_3M_TENOR = Period.ofMonths(3);
  private static final SwapFixedOISSimplifiedDefinition EONIA_SWAP_3M_DEFINITION = SwapFixedOISSimplifiedDefinition.from(SPOT_DATE, EUR_SWAP_3M_TENOR, EUR_SWAP_3M_TENOR, NOTIONAL, EUR_OIS,
      FIXED_RATE, IS_PAYER, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_IS_EOM);
  private static final Swap<? extends Payment, ? extends Payment> EONIA_SWAP_3M = EONIA_SWAP_3M_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAMES);
  //Swap EONIA 3Y
  private static final Period EUR_SWAP_3Y_TENOR = Period.ofYears(3);
  private static final Period EUR_COUPON_TENOR = Period.ofMonths(12);
  private static final SwapFixedOISSimplifiedDefinition EONIA_SWAP_3Y_DEFINITION = SwapFixedOISSimplifiedDefinition.from(SPOT_DATE, EUR_SWAP_3Y_TENOR, EUR_COUPON_TENOR, NOTIONAL, EUR_OIS, FIXED_RATE,
      IS_PAYER, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_IS_EOM);
  private static final Swap<? extends Payment, ? extends Payment> EONIA_SWAP_3Y = EONIA_SWAP_3Y_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAMES);

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueSwap3M() {
    double pv = PVC.visit(EONIA_SWAP_3M, CURVES);
    double pvExpected = PVC.visit(EONIA_SWAP_3M.getFirstLeg(), CURVES);
    pvExpected += PVC.visit(EONIA_SWAP_3M.getSecondLeg().getNthPayment(0), CURVES);
    AssertJUnit.assertEquals("OIS swap: present value", pvExpected, pv, 1.0E-2);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueSwap3Y() {
    double pv = PVC.visit(EONIA_SWAP_3Y, CURVES);
    double pvExpected = PVC.visit(EONIA_SWAP_3Y.getFirstLeg(), CURVES);
    for (int loopcpn = 0; loopcpn < EONIA_SWAP_3Y.getSecondLeg().getNumberOfPayments(); loopcpn++) {
      pvExpected += PVC.visit(EONIA_SWAP_3Y.getSecondLeg().getNthPayment(loopcpn), CURVES);
    }
    AssertJUnit.assertEquals("OIS swap: present value", pvExpected, pv, 1.0E-2);
  }

}
