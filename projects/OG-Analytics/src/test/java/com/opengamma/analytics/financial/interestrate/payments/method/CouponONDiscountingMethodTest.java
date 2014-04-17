/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONSimplifiedDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of OIS coupon by discounting.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CouponONDiscountingMethodTest {
  private static final int EUR_SETTLEMENT_DAYS = 2;
  private static final BusinessDayConvention EUR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean EUR_IS_EOM = true;
  //EUR Eonia
  private static final String EUR_OIS_NAME = "EUR EONIA";
  private static final Currency EUR_CUR = Currency.EUR;
  private static final Calendar EUR_CALENDAR = new MondayToFridayCalendar("EUR");
  private static final int EUR_PUBLICATION_LAG = 0;
  private static final DayCount EUR_DAY_COUNT = DayCounts.ACT_360;
  private static final IndexON EUR_OIS = new IndexON(EUR_OIS_NAME, EUR_CUR, EUR_DAY_COUNT, EUR_PUBLICATION_LAG);
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
  private static final CouponONSimplifiedDefinition EONIA_COUPON_DEFINITION = new CouponONSimplifiedDefinition(EUR_CUR, PAYMENT_DATE, START_ACCRUAL_DATE, END_ACCRUAL_DATE, PAYMENT_YEAR_FRACTION,
      NOTIONAL, EUR_OIS, START_ACCRUAL_DATE, END_ACCRUAL_DATE, FIXING_YEAR_FRACTION);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVES_NAMES = CURVES.getAllNames().toArray(new String[CURVES.size()]);

  private static final ZonedDateTime REFERENCE_DATE_1 = TRADE_DATE;
  private static final double PAYMENT_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, PAYMENT_DATE);
  private static final double START_ACCRUAL_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, START_ACCRUAL_DATE);
  private static final double END_ACCRUAL_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, END_ACCRUAL_DATE);
  private static final CouponON EONIA_COUPON_NOTSTARTED = new CouponON(EUR_CUR, PAYMENT_TIME_1, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_TIME_1, END_ACCRUAL_TIME_1,
      FIXING_YEAR_FRACTION, NOTIONAL, CURVES_NAMES[1]);

  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2011, 10, 7);
  private static final ZonedDateTime NEXT_FIXING_DATE_2 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_2, 1, EUR_CALENDAR); // Overnight
  private static final double PAYMENT_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, PAYMENT_DATE);
  private static final double START_FIXING_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, NEXT_FIXING_DATE_2);
  private static final double END_FIXING_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, END_ACCRUAL_DATE);
  private static final double FIXING_YEAR_FRACTION_2 = EUR_DAY_COUNT.getDayCountFraction(NEXT_FIXING_DATE_2, END_ACCRUAL_DATE);
  private static final double NOTIONAL_WITH_ACCRUED = NOTIONAL * (1.0 + 0.01 / 12); // 1% over a month (roughly)
  private static final CouponON EONIA_COUPON_STARTED = new CouponON(EUR_CUR, PAYMENT_TIME_2, CURVES_NAMES[0], FIXING_YEAR_FRACTION_2, NOTIONAL, EUR_OIS, START_FIXING_TIME_2, END_FIXING_TIME_2,
      FIXING_YEAR_FRACTION_2, NOTIONAL_WITH_ACCRUED, CURVES_NAMES[1]);

  private static final CouponONDiscountingMethod METHOD_OIS = CouponONDiscountingMethod.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final double TOLERANCE_PRICE = 1.0E-2;

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueNotStarted() {
    final CurrencyAmount pv = METHOD_OIS.presentValue(EONIA_COUPON_NOTSTARTED, CURVES);
    final double dfForwardStart = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(START_ACCRUAL_TIME_1);
    final double dfForwardEnd = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(END_ACCRUAL_TIME_1);
    final double dfPayment = CURVES.getCurve(CURVES_NAMES[0]).getDiscountFactor(PAYMENT_TIME_1);
    final double forward = (dfForwardStart / dfForwardEnd - 1.0) / PAYMENT_YEAR_FRACTION;
    final double pvExpected = forward * PAYMENT_YEAR_FRACTION * NOTIONAL * dfPayment;
    AssertJUnit.assertEquals("CouponOIS: Present value by discounting", pvExpected, pv.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueNotStartedMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD_OIS.presentValue(EONIA_COUPON_NOTSTARTED, CURVES);
    final double pvCalculator = EONIA_COUPON_NOTSTARTED.accept(PVC, CURVES);
    AssertJUnit.assertEquals("CouponOIS: Present value by discounting", pvCalculator, pvMethod.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueStarted() {
    final CurrencyAmount pv = METHOD_OIS.presentValue(EONIA_COUPON_STARTED, CURVES);
    final double dfRatioStart = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(START_FIXING_TIME_2);
    final double dfRatioEnd = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(END_FIXING_TIME_2);
    final double dfPayment = CURVES.getCurve(CURVES_NAMES[0]).getDiscountFactor(PAYMENT_TIME_2);
    final double dfRatio = dfRatioStart / dfRatioEnd;
    final double pvExpected = (NOTIONAL_WITH_ACCRUED * dfRatio - NOTIONAL) * dfPayment;
    AssertJUnit.assertEquals("CouponOIS: Present value by discounting", pvExpected, pv.getAmount(), 1.0E-2);
  }

  @Test
  /**
   * Tests the par rate.
   */
  public void parRateNotStarted() {
    final double pr = METHOD_OIS.parRate(EONIA_COUPON_NOTSTARTED, CURVES);
    final double dfForwardStart = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(START_ACCRUAL_TIME_1);
    final double dfForwardEnd = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(END_ACCRUAL_TIME_1);
    final double forward = (dfForwardStart / dfForwardEnd - 1.0) / PAYMENT_YEAR_FRACTION;
    AssertJUnit.assertEquals("CouponOIS: par rate by discounting", forward, pr, 1.0E-10);
  }

  @Test
  /**
   * Tests the par rate.
   */
  public void parRateNotStartedMethodVsCalculator() {
    final double prMethod = METHOD_OIS.parRate(EONIA_COUPON_NOTSTARTED, CURVES);
    final double prCalculator = EONIA_COUPON_NOTSTARTED.accept(PRC, CURVES);
    AssertJUnit.assertEquals("CouponOIS: par rate by discounting", prMethod, prCalculator, 1.0E-10);
  }

  @Test
  /**
   * Tests the present value rate sensitivity.
   */
  public void presentValueCurveSensitivityNotStarted() {
    InterestRateCurveSensitivity pvcs = METHOD_OIS.presentValueCurveSensitivity(EONIA_COUPON_NOTSTARTED, CURVES);
    pvcs = pvcs.cleaned();
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final Payment couponBumpedForward = EONIA_COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1, new String[] {CURVES_NAMES[0], bumpedCurveName });
    final double[] nodeTimesForward = new double[] {EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime(), EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime() };
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(couponBumpedForward, CURVES, CURVES_NAMES[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD_OIS);
    AssertJUnit.assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvcs.getSensitivities().get(CURVES_NAMES[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final Payment couponBumpedDisc = EONIA_COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1, new String[] {bumpedCurveName, CURVES_NAMES[1] });
    final double[] nodeTimesDisc = new double[] {EONIA_COUPON_NOTSTARTED.getPaymentTime() };
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
    final CouponON eoniaCouponNotStartedOneCurve = new CouponON(EUR_CUR, PAYMENT_TIME_1, CURVES_NAMES[0], PAYMENT_YEAR_FRACTION, NOTIONAL, EUR_OIS, START_ACCRUAL_TIME_1, END_ACCRUAL_TIME_1,
        FIXING_YEAR_FRACTION, NOTIONAL, CURVES_NAMES[0]);
    final InterestRateCurveSensitivity pvcs = METHOD_OIS.presentValueCurveSensitivity(eoniaCouponNotStartedOneCurve, CURVES);
    //pvcs = pvcs.cleaned();
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
    final Payment couponBumpedDisc = EONIA_COUPON_DEFINITION.toDerivative(REFERENCE_DATE_1, new String[] {bumpedCurveName, bumpedCurveName });
    final double[] nodeTimesDisc = new double[] {EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime(), EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime(), EONIA_COUPON_NOTSTARTED.getPaymentTime() };
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
    final InterestRateCurveSensitivity pvcsMethod = METHOD_OIS.presentValueCurveSensitivity(EONIA_COUPON_NOTSTARTED, CURVES);
    final Map<String, List<DoublesPair>> pvcsCalculator = EONIA_COUPON_NOTSTARTED.accept(PVCSC, CURVES);
    AssertJUnit.assertEquals(pvcsCalculator.size(), pvcsMethod.getSensitivities().size());
    for (final Map.Entry<String, List<DoublesPair>> entry : pvcsCalculator.entrySet()) {
      AssertJUnit.assertTrue(pvcsMethod.getSensitivities().containsKey(entry.getKey()));
      final List<DoublesPair> firstPairs = entry.getValue();
      final List<DoublesPair> secondPairs = pvcsMethod.getSensitivities().get(entry.getKey());
      AssertJUnit.assertEquals(firstPairs.size(), secondPairs.size());
      for (int i = 0; i < firstPairs.size(); i++) {
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
    InterestRateCurveSensitivity prcs = METHOD_OIS.parRateCurveSensitivity(EONIA_COUPON_NOTSTARTED, CURVES);
    prcs = prcs.cleaned();
    final double deltaTolerancePrice = 1.0E-10;
    // 1. Forward curve sensitivity
    final double[] nodeTimesForward = new double[] {EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime(), EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime() };
    final double dfForwardStart = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(START_ACCRUAL_TIME_1);
    final double dfForwardEnd = CURVES.getCurve(CURVES_NAMES[1]).getDiscountFactor(END_ACCRUAL_TIME_1);
    final double dfForwardEndBar = -dfForwardStart / (dfForwardEnd * dfForwardEnd) / EONIA_COUPON_NOTSTARTED.getFixingPeriodAccrualFactor();
    final double dfForwardStartBar = 1.0 / (EONIA_COUPON_NOTSTARTED.getFixingPeriodAccrualFactor() * dfForwardEnd);
    final double[] sensiForwardMethod = new double[] {-EONIA_COUPON_NOTSTARTED.getFixingPeriodStartTime() * dfForwardStart * dfForwardStartBar,
        -EONIA_COUPON_NOTSTARTED.getFixingPeriodEndTime() * dfForwardEnd * dfForwardEndBar };
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
  private static final double EUR_FIXED_RATE = 0.01;
  private static final boolean IS_PAYER = true;
  private static final Period EUR_SWAP_3M_TENOR = Period.ofMonths(3);
  private static final SwapFixedONSimplifiedDefinition EONIA_SWAP_3M_DEFINITION = SwapFixedONSimplifiedDefinition.from(SPOT_DATE, EUR_SWAP_3M_TENOR, EUR_SWAP_3M_TENOR, NOTIONAL, EUR_OIS,
      EUR_FIXED_RATE, IS_PAYER, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_DAY_COUNT, EUR_IS_EOM, EUR_CALENDAR);
  private static final Swap<? extends Payment, ? extends Payment> EONIA_SWAP_3M = EONIA_SWAP_3M_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAMES);
  //Swap EONIA 3Y
  private static final Period EUR_SWAP_3Y_TENOR = Period.ofYears(3);
  private static final Period EUR_COUPON_TENOR = Period.ofMonths(12);
  private static final SwapFixedONSimplifiedDefinition EONIA_SWAP_3Y_DEFINITION = SwapFixedONSimplifiedDefinition.from(SPOT_DATE, EUR_SWAP_3Y_TENOR, EUR_COUPON_TENOR, NOTIONAL, EUR_OIS,
      EUR_FIXED_RATE, IS_PAYER, EUR_SETTLEMENT_DAYS, EUR_BUSINESS_DAY, EUR_DAY_COUNT, EUR_IS_EOM, EUR_CALENDAR);
  private static final Swap<? extends Payment, ? extends Payment> EONIA_SWAP_3Y = EONIA_SWAP_3Y_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAMES);

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueSwap3M() {
    final double pv = EONIA_SWAP_3M.accept(PVC, CURVES);
    double pvExpected = EONIA_SWAP_3M.getFirstLeg().accept(PVC, CURVES);
    pvExpected += EONIA_SWAP_3M.getSecondLeg().getNthPayment(0).accept(PVC, CURVES);
    AssertJUnit.assertEquals("OIS swap: present value", pvExpected, pv, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValueSwap3Y() {
    final double pv = EONIA_SWAP_3Y.accept(PVC, CURVES);
    double pvExpected = EONIA_SWAP_3Y.getFirstLeg().accept(PVC, CURVES);
    for (int loopcpn = 0; loopcpn < EONIA_SWAP_3Y.getSecondLeg().getNumberOfPayments(); loopcpn++) {
      pvExpected += EONIA_SWAP_3Y.getSecondLeg().getNthPayment(loopcpn).accept(PVC, CURVES);
    }
    AssertJUnit.assertEquals("OIS swap: present value", pvExpected, pv, TOLERANCE_PRICE);
  }

  // Swap Fed Fund 6M (the fixing take place at the end of the period; in some cases a negative time discount factor is required).
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedON OIS_USD_GENERATOR = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", NYC);
  private static final double USD_FIXED_RATE = 0.0050;
  private static final Period TENOR_6M = Period.ofMonths(6);
  private static final SwapFixedONDefinition OIS_DEFINITION = SwapFixedONDefinition.from(START_ACCRUAL_DATE, TENOR_6M, NOTIONAL, OIS_USD_GENERATOR, USD_FIXED_RATE, IS_PAYER);
  private static final YieldCurveBundle CURVES_2 = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVES_NAMES_2 = TestsDataSetsSABR.curves2Names();

  @Test
  /**
   * Tests the present value for USD OIS.
   */
  public void presentValueOISSwapUSDBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Swap<? extends Payment, ? extends Payment> ois = OIS_DEFINITION.toDerivative(referenceDate, CURVES_NAMES_2[0], CURVES_NAMES_2[0]);
    final double pv = ois.accept(PVC, CURVES_2);
    double pvExpected = ois.getFirstLeg().accept(PVC, CURVES_2);
    pvExpected += ois.getSecondLeg().getNthPayment(0).accept(PVC, CURVES_2);
    AssertJUnit.assertEquals("OIS swap: present value", pvExpected, pv, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value for USD OIS where valuation date == first publication date (one business day following the first fixing date)
   * AND the fixing has been published.
   */
  public void presentValueOISSwapUSDAfterFixing() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, 1, NYC);
    final double fixingRate = 0.0010;
    final ZonedDateTimeDoubleTimeSeries fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {START_ACCRUAL_DATE }, new double[] {fixingRate });
    final ZonedDateTimeDoubleTimeSeries[] fixingTS2 = new ZonedDateTimeDoubleTimeSeries[] {fixingTS };
    final Swap<? extends Payment, ? extends Payment> ois = OIS_DEFINITION.toDerivative(referenceDate, fixingTS2, CURVES_NAMES_2[0], CURVES_NAMES_2[0]);
    final double pv = ois.accept(PVC, CURVES_2);
    double pvExpected = ois.getFirstLeg().accept(PVC, CURVES_2);
    pvExpected += ois.getSecondLeg().getNthPayment(0).accept(PVC, CURVES_2);
    AssertJUnit.assertEquals("OIS swap: present value", pvExpected, pv, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the present value for USD OIS where valuation date == first publication date (one business day following the first fixing date)
   * BUT the fixing has not yet been published.
   */
  public void presentValueOISSwapUSDBeforeFixing() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(START_ACCRUAL_DATE, 1, NYC);
    final ZonedDateTimeDoubleTimeSeries[] fixingTS2 = new ZonedDateTimeDoubleTimeSeries[] {ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC) };
    final Swap<? extends Payment, ? extends Payment> ois = OIS_DEFINITION.toDerivative(referenceDate, fixingTS2, CURVES_NAMES_2[0], CURVES_NAMES_2[0]);
    final double pv = ois.accept(PVC, CURVES_2);
    double pvExpected = ois.getFirstLeg().accept(PVC, CURVES_2);
    pvExpected += ois.getSecondLeg().getNthPayment(0).accept(PVC, CURVES_2);
    AssertJUnit.assertEquals("OIS swap: present value", pvExpected, pv, TOLERANCE_PRICE);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    final ZonedDateTime referenceDate = TRADE_DATE;
    final Period tenor = Period.ofYears(50);
    SwapFixedONDefinition oidUsd5YDefinition;
    Swap<? extends Payment, ? extends Payment> ois;

    final double[] pv = new double[nbTest];
    final InterestRateCurveSensitivity[] pvcs = new InterestRateCurveSensitivity[nbTest];

    double totalValue = 0.0;

    startTime = System.currentTimeMillis();
    oidUsd5YDefinition = SwapFixedONDefinition.from(START_ACCRUAL_DATE, tenor, NOTIONAL, OIS_USD_GENERATOR, USD_FIXED_RATE, IS_PAYER);
    ois = oidUsd5YDefinition.toDerivative(referenceDate, CURVES_NAMES_2[0], CURVES_NAMES_2[0]);
    endTime = System.currentTimeMillis();
    System.out.println("OIS swap " + tenor + " (construction): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = ois.accept(PVC, CURVES_2);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " OIS swap " + tenor + " (price): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcs[looptest] = new InterestRateCurveSensitivity(ois.accept(PVCSC, CURVES_2));
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " OIS swap " + tenor + " (delta): " + (endTime - startTime) + " ms");

    // Performance note: price: 28-Feb-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 15 ms for 100 OIS.
    // Performance note: delta: 28-Feb-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 115 ms for 100 OIS.
    for (int looptest = 0; looptest < nbTest; looptest++) {
      totalValue += pv[looptest];
    }
    startTime = System.currentTimeMillis();
    oidUsd5YDefinition = SwapFixedONDefinition.from(START_ACCRUAL_DATE, tenor, NOTIONAL, OIS_USD_GENERATOR, USD_FIXED_RATE, IS_PAYER);
    ois = oidUsd5YDefinition.toDerivative(referenceDate, CURVES_NAMES_2[0], CURVES_NAMES_2[0]);
    endTime = System.currentTimeMillis();
    System.out.println("OIS swap " + tenor + " (construction): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = ois.accept(PVC, CURVES_2);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " OIS swap " + tenor + " (price): " + (endTime - startTime) + " ms");

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvcs[looptest] = new InterestRateCurveSensitivity(ois.accept(PVCSC, CURVES_2));
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " OIS swap " + tenor + " (delta): " + (endTime - startTime) + " ms");

    // Performance note: price: 28-Feb-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 OIS.
    // Performance note: delta: 28-Feb-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 OIS.
    for (int looptest = 0; looptest < nbTest; looptest++) {
      totalValue += pv[looptest];
    }
    System.out.println(totalValue);
  }

}
