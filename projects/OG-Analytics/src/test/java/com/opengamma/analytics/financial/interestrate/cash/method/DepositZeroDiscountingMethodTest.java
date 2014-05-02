/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.DepositZeroDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRate;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
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
 * Tests related to the pricing of deposits zero-coupon by discounting.
 * @deprecated The class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DepositZeroDiscountingMethodTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorDeposit GENERATOR = new EURDeposit(TARGET);
  private static final Currency EUR = GENERATOR.getCurrency();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, GENERATOR.getSpotLag(), TARGET);

  private static final double NOTIONAL = 100000000;
  private static final double RATE_FIGURE = 0.0250;
  private static final InterestRate RATE = new PeriodicInterestRate(RATE_FIGURE, 1);
  private static final Period DEPOSIT_PERIOD = Period.ofMonths(6);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, DEPOSIT_PERIOD, GENERATOR);
  private static final double DEPOSIT_AF = GENERATOR.getDayCount().getDayCountFraction(SPOT_DATE, END_DATE);
  private static final Calendar CALENDAR = new NoHolidayCalendar();
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ISDA;
  private static final DepositZeroDefinition DEPOSIT_DEFINITION = new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, RATE, CALENDAR, DAY_COUNT);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVES_NAME = TestsDataSetsSABR.curves2Names();

  private static final DepositZeroDiscountingMethod METHOD_DEPOSIT = DepositZeroDiscountingMethod.getInstance();
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final ParRateCurveSensitivityCalculator PRCSC = ParRateCurveSensitivityCalculator.getInstance();
  private static final ParSpreadMarketQuoteCalculator PSC = ParSpreadMarketQuoteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSCSC = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double TOLERANCE_TIME = 1.0E-6;
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-10;

  @Test
  /**
   * Tests present value when the valuation date is on trade date.
   */
  public void presentValueTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pvMethod = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    final double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    final double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvMethod.getAmount(), TOLERANCE_PRICE);
    final double pvCalculator = deposit.accept(PVC, CURVES);
    assertEquals("DepositDefinition: present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenTradeAndSettle() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    final double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    final double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueSettle() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pvMethod = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    final double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    final double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvMethod.getAmount(), TOLERANCE_PRICE);
    final double pvCalculator = deposit.accept(PVC, CURVES);
    assertEquals("DepositDefinition: present value", pvMethod.getAmount(), pvCalculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    final double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(DAY_COUNT.getDayCountFraction(SPOT_DATE, referenceDate));
    final double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(DAY_COUNT.getDayCountFraction(referenceDate, END_DATE, CALENDAR));
    final double pvExpected = (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd - deposit.getInitialAmount() * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueMaturity() {
    final ZonedDateTime referenceDate = END_DATE;
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    final double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(DAY_COUNT.getDayCountFraction(SPOT_DATE, referenceDate));
    final double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(DAY_COUNT.getDayCountFraction(referenceDate, END_DATE, CALENDAR));
    final double pvExpected = (deposit.getNotional() + deposit.getInterestAmount()) * dfEnd - deposit.getInitialAmount() * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final InterestRateCurveSensitivity pvcsMethod = METHOD_DEPOSIT.presentValueCurveSensitivity(deposit, CURVES);
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsMethod.getSensitivities().size());
    assertEquals("DepositDefinition: present value curve sensitivity", 2, pvcsMethod.getSensitivities().get(CURVES_NAME[0]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // Discounting curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final DepositZero depositBunped = DEPOSIT_DEFINITION.toDerivative(referenceDate, bumpedCurveName);
    final double[] nodeTimesDisc = new double[] {deposit.getStartTime(), deposit.getEndTime() };
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(depositBunped, CURVES, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_DEPOSIT);
    final List<DoublesPair> sensiPvDisc = pvcsMethod.getSensitivities().get(CURVES_NAME[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
    final InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(deposit.accept(PVCSC, CURVES));
    AssertSensitivityObjects.assertEquals("DepositZero: present value curve sensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    InterestRateCurveSensitivity pvcsMethod = METHOD_DEPOSIT.presentValueCurveSensitivity(deposit, CURVES);
    pvcsMethod = pvcsMethod.cleaned(0.0, 1.0E-4);
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsMethod.getSensitivities().size());
    assertEquals("DepositDefinition: present value curve sensitivity", 2, pvcsMethod.getSensitivities().get(CURVES_NAME[0]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // Discounting curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final DepositZero depositBumped = DEPOSIT_DEFINITION.toDerivative(referenceDate, bumpedCurveName);
    final double[] nodeTimesDisc = new double[] {deposit.getStartTime(), deposit.getEndTime()};
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(depositBumped, CURVES, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_DEPOSIT);
    final List<DoublesPair> sensiPvDisc = pvcsMethod.getSensitivities().get(CURVES_NAME[0]);
    final DoublesPair pairPv = sensiPvDisc.get(0);
    assertEquals("Sensitivity coupon pv to forward curve: Node " + 0, nodeTimesDisc[0], pairPv.getFirst(), 1E-8);
    AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[0], deltaTolerancePrice);
    InterestRateCurveSensitivity pvcsCalculator = new InterestRateCurveSensitivity(deposit.accept(PVCSC, CURVES));
    pvcsCalculator = pvcsCalculator.cleaned(0.0, 1.0E-4);
    AssertSensitivityObjects.assertEquals("DepositZero: present value curve sensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the par rate when the valuation date is on trade date.
   */
  public void parRateTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final double prMethod = METHOD_DEPOSIT.parRate(deposit, CURVES);
    final double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    final double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    final double rcc = Math.log(dfStart / dfEnd) / deposit.getPaymentAccrualFactor();
    final double prExpected = deposit.getRate().fromContinuous(new ContinuousInterestRate(rcc)).getRate();
    assertEquals("DepositZero: par rate", prExpected, prMethod, TOLERANCE_RATE);
    final double prCalculator = deposit.accept(PRC, CURVES);
    assertEquals("DepositZero: par rate", prMethod, prCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the par rate when the valuation date is on trade date.
   */
  public void parRateSettle() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final double prMethod = METHOD_DEPOSIT.parRate(deposit, CURVES);
    final double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    final double dfStart = 1.0;
    final double rcc = Math.log(dfStart / dfEnd) / deposit.getPaymentAccrualFactor();
    final double prExpected = deposit.getRate().fromContinuous(new ContinuousInterestRate(rcc)).getRate();
    assertEquals("DepositZero: par rate", prExpected, prMethod, TOLERANCE_RATE);
    final double prCalculator = deposit.accept(PRC, CURVES);
    assertEquals("DepositZero: par rate", prMethod, prCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the par rate curve sensitivity when the valuation date is on trade date.
   */
  public void parRateCurveSensitivityTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final InterestRateCurveSensitivity prcsMethod = METHOD_DEPOSIT.parRateCurveSensitivity(deposit, CURVES);
    final List<DoublesPair> sensiPvDisc = prcsMethod.getSensitivities().get(CURVES_NAME[0]);
    final double pr = METHOD_DEPOSIT.parRate(deposit, CURVES);
    final YieldAndDiscountCurve curveToBump = CURVES.getCurve(CURVES_NAME[0]);
    final double deltaShift = 0.0001;
    final int nbNode = 2;
    final double[] result = new double[nbNode];
    final double[] nodeTimesExtended = new double[nbNode + 1];
    nodeTimesExtended[1] = deposit.getStartTime();
    nodeTimesExtended[2] = deposit.getEndTime();
    final double[] yields = new double[nbNode + 1];
    yields[0] = curveToBump.getInterestRate(0.0);
    yields[1] = curveToBump.getInterestRate(nodeTimesExtended[1]);
    yields[2] = curveToBump.getInterestRate(nodeTimesExtended[2]);
    final YieldAndDiscountCurve curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
      CURVES.replaceCurve(CURVES_NAME[0], curveBumped);
      final double prBumped = METHOD_DEPOSIT.parRate(deposit, CURVES);
      result[loopnode] = (prBumped - pr) / deltaShift;
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesExtended[loopnode + 1], pairPv.getFirst(), TOLERANCE_TIME);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, result[loopnode], TOLERANCE_PRICE);
    }
    CURVES.replaceCurve(CURVES_NAME[0], curveToBump);
    InterestRateCurveSensitivity prcsCalculator = new InterestRateCurveSensitivity(deposit.accept(PRCSC, CURVES));
    prcsCalculator = prcsCalculator.cleaned(0.0, 1.0E-4);
    AssertSensitivityObjects.assertEquals("DepositZero: par rate curve sensitivity", prcsMethod, prcsCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the par rate curve sensitivity when the valuation date is on trade date.
   */
  public void parRateCurveSensitivitySettle() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final InterestRateCurveSensitivity prcsMethod = METHOD_DEPOSIT.parRateCurveSensitivity(deposit, CURVES);
    final List<DoublesPair> sensiPvDisc = prcsMethod.getSensitivities().get(CURVES_NAME[0]);
    final double pr = METHOD_DEPOSIT.parRate(deposit, CURVES);
    final YieldAndDiscountCurve curveToBump = CURVES.getCurve(CURVES_NAME[0]);
    final double deltaShift = 0.0001;
    final int nbNode = 2;
    final double[] result = new double[nbNode];
    final double[] nodeTimesExtended = new double[nbNode];
    nodeTimesExtended[0] = deposit.getStartTime();
    nodeTimesExtended[1] = deposit.getEndTime();
    final double[] yields = new double[nbNode];
    yields[0] = curveToBump.getInterestRate(nodeTimesExtended[0]);
    yields[1] = curveToBump.getInterestRate(nodeTimesExtended[1]);
    final YieldAndDiscountCurve curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode], deltaShift);
      CURVES.replaceCurve(CURVES_NAME[0], curveBumped);
      final double prBumped = METHOD_DEPOSIT.parRate(deposit, CURVES);
      result[loopnode] = (prBumped - pr) / deltaShift;
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesExtended[loopnode], pairPv.getFirst(), TOLERANCE_TIME);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, result[loopnode], TOLERANCE_PRICE);
    }
    CURVES.replaceCurve(CURVES_NAME[0], curveToBump);
    final InterestRateCurveSensitivity prcsCalculator = new InterestRateCurveSensitivity(deposit.accept(PRCSC, CURVES));
    AssertSensitivityObjects.assertEquals("DepositZero: par rate curve sensitivity", prcsMethod, prcsCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the par spread when the valuation date is on trade date.
   */
  public void parSpreadTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final double psMethod = METHOD_DEPOSIT.parSpread(deposit, CURVES);
    final DepositZeroDefinition deposit0Definition = new DepositZeroDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, DEPOSIT_AF, new PeriodicInterestRate(RATE_FIGURE + psMethod, 1), CALENDAR, DAY_COUNT);
    final DepositZero deposit0 = deposit0Definition.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, CURVES);
    assertEquals("DepositZero: par spread", 0, pv0.getAmount(), TOLERANCE_PRICE);
    final double psCalculator = deposit.accept(PSC, CURVES);
    assertEquals("DepositZero: par rate", psMethod, psCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivity() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositZero deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final InterestRateCurveSensitivity pscsMethod = METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, CURVES);
    final List<DoublesPair> sensiPvDisc = pscsMethod.getSensitivities().get(CURVES_NAME[0]);
    final double ps = METHOD_DEPOSIT.parSpread(deposit, CURVES);
    final YieldAndDiscountCurve curveToBump = CURVES.getCurve(CURVES_NAME[0]);
    final double deltaShift = 0.0001;
    final int nbNode = 2;
    final double[] result = new double[nbNode];
    final double[] nodeTimesExtended = new double[nbNode + 1];
    nodeTimesExtended[1] = deposit.getStartTime();
    nodeTimesExtended[2] = deposit.getEndTime();
    final double[] yields = new double[nbNode + 1];
    yields[0] = curveToBump.getInterestRate(0.0);
    yields[1] = curveToBump.getInterestRate(nodeTimesExtended[1]);
    yields[2] = curveToBump.getInterestRate(nodeTimesExtended[2]);
    final YieldAndDiscountCurve curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
      CURVES.replaceCurve(CURVES_NAME[0], curveBumped);
      final double psBumped = METHOD_DEPOSIT.parSpread(deposit, CURVES);
      result[loopnode] = (psBumped - ps) / deltaShift;
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity par spread to curve: Node " + loopnode, nodeTimesExtended[loopnode + 1], pairPv.getFirst(), TOLERANCE_TIME);
      assertEquals("Sensitivity par spread to curve: Node", pairPv.second, result[loopnode], TOLERANCE_PRICE);
    }
    CURVES.replaceCurve(CURVES_NAME[0], curveToBump);
    InterestRateCurveSensitivity prcsCalculator = deposit.accept(PSCSC, CURVES);
    prcsCalculator = prcsCalculator.cleaned(0.0, 1.0E-4);
    AssertSensitivityObjects.assertEquals("DepositZero: par rate curve sensitivity", pscsMethod, prcsCalculator, TOLERANCE_SPREAD_DELTA);
  }

}
