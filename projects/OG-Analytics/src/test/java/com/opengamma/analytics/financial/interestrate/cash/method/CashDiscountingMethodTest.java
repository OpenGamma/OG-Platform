/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.generic.TodayPaymentCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of cash deposits by discounting.
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CashDiscountingMethodTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorDeposit GENERATOR = new EURDeposit(TARGET);
  private static final Currency EUR = GENERATOR.getCurrency();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, GENERATOR.getSpotLag(), TARGET);

  private static final double NOTIONAL = 100000000;
  private static final double RATE = 0.0250;
  private static final Period DEPOSIT_PERIOD = Period.ofMonths(6);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, DEPOSIT_PERIOD, GENERATOR);
  private static final double DEPOSIT_AF = GENERATOR.getDayCount().getDayCountFraction(SPOT_DATE, END_DATE);
  private static final CashDefinition DEPOSIT_DEFINITION = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVES_NAME = TestsDataSetsSABR.curves2Names();

  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final ParSpreadMarketQuoteCalculator PSC = ParSpreadMarketQuoteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSCSC = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();
  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-2;
  private static final double TOLERANCE_SPREAD = 1.0E-10;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-10;
  private static final double TOLERANCE_TIME = 1.0E-6;

  @Test
  /**
   * Tests present value when the valuation date is on trade date.
   */
  public void presentValueTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
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
  public void presentValueBetweenTradeAndSettle() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
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
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
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
  public void presentValueBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    final double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueMaturity() {
    final ZonedDateTime referenceDate = END_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    final double pvExpected = NOTIONAL + deposit.getInterestAmount();
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final InterestRateCurveSensitivity pvcsComputed = METHOD_DEPOSIT.presentValueCurveSensitivity(deposit, CURVES);
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsComputed.getSensitivities().size());
    assertEquals("DepositDefinition: present value curve sensitivity", 2, pvcsComputed.getSensitivities().get(CURVES_NAME[0]).size());
    final double deltaShift = 1.0E-6;
    // Discounting curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final Cash depositBunped = DEPOSIT_DEFINITION.toDerivative(referenceDate, bumpedCurveName);
    final double[] nodeTimesDisc = new double[] {deposit.getStartTime(), deposit.getEndTime() };
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(depositBunped, CURVES, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_DEPOSIT);
    final List<DoublesPair> sensiPvDisc = pvcsComputed.getSensitivities().get(CURVES_NAME[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[loopnode], TOLERANCE_PRICE_DELTA);
    }
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    InterestRateCurveSensitivity pvcsComputed = METHOD_DEPOSIT.presentValueCurveSensitivity(deposit, CURVES);
    pvcsComputed = pvcsComputed.cleaned(0.0, 1.0E-4);
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsComputed.getSensitivities().size());
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsComputed.getSensitivities().get(CURVES_NAME[0]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // Discounting curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final Cash depositBunped = DEPOSIT_DEFINITION.toDerivative(referenceDate, bumpedCurveName);
    final double[] nodeTimesDisc = new double[] {deposit.getEndTime() };
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(depositBunped, CURVES, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_DEPOSIT);
    final List<DoublesPair> sensiPvDisc = pvcsComputed.getSensitivities().get(CURVES_NAME[0]);
    final DoublesPair pairPv = sensiPvDisc.get(0);
    assertEquals("Sensitivity coupon pv to forward curve: Node " + 0, nodeTimesDisc[0], pairPv.getFirst(), 1E-8);
    AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[0], deltaTolerancePrice);
  }

  @Test
  /**
   * Tests parRate when the present is before the deposit start date.
   */
  public void parRateBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final double parRate = METHOD_DEPOSIT.parRate(deposit, CURVES);
    final CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, parRate, DEPOSIT_AF);
    final Cash deposit0 = deposit0Definition.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, CURVES);
    assertEquals("DepositDefinition: par rate", 0, pv0.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests parSpread when the present is before the deposit start date.
   */
  public void parSpreadBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final double parSpread = METHOD_DEPOSIT.parSpread(deposit, CURVES);
    final CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE + parSpread, DEPOSIT_AF);
    final Cash deposit0 = deposit0Definition.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, CURVES);
    assertEquals("DepositDefinition: par spread", 0, pv0.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests parSpread when the present date is on the start date.
   */
  public void parSpreadOnStart() {
    final ZonedDateTime referenceDate = DEPOSIT_DEFINITION.getStartDate();
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final double parSpread = METHOD_DEPOSIT.parSpread(deposit, CURVES);
    final CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE + parSpread, DEPOSIT_AF);
    final Cash deposit0 = deposit0Definition.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, CURVES);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests parSpread when the present date is after the start: .
   */
  public void parSpreadAfterStart() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(DEPOSIT_DEFINITION.getStartDate(), 1, TARGET);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final double parSpread = METHOD_DEPOSIT.parSpread(deposit, CURVES); // Spread will be -(1/delta+rate), as there is no initial amount
    final CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE + parSpread, DEPOSIT_AF);
    final Cash deposit0 = deposit0Definition.toDerivative(referenceDate, CURVES_NAME[0]);
    final CurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, CURVES);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests parSpread.
   */
  public void parSpreadMethodVsCalculator() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final double parSpreadMethod = METHOD_DEPOSIT.parSpread(deposit, CURVES);
    final double parSpreadCalculator = deposit.accept(PSC, CURVES);
    assertEquals("DepositDefinition: present value", parSpreadMethod, parSpreadCalculator, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivity() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
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

  @Test
  /**
   * Tests today payment amount when the present is before the deposit start date.
   */
  public void todayPaymentBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final MultipleCurrencyAmount cash = deposit.accept(TPC);
    assertEquals("DepositDefinition: today payment", 0.0, cash.getAmount(deposit.getCurrency()), TOLERANCE_PRICE);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests today payment amount when the present is on the deposit start date.
   */
  public void todayPaymentOnStart() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final MultipleCurrencyAmount cash = deposit.accept(TPC);
    assertEquals("DepositDefinition: today payment", -NOTIONAL, cash.getAmount(deposit.getCurrency()), TOLERANCE_PRICE);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests today payment amount when the present is on the deposit start date.
   */
  public void todayPaymentBetweenStartAndEnd() {
    final ZonedDateTime referenceDate = SPOT_DATE.plusDays(2);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final MultipleCurrencyAmount cash = deposit.accept(TPC);
    assertEquals("DepositDefinition: today payment", 0.0, cash.getAmount(deposit.getCurrency()), TOLERANCE_PRICE);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests today payment amount when the present is on the deposit end date.
   */
  public void todayPaymentOnEnd() {
    final ZonedDateTime referenceDate = END_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    final MultipleCurrencyAmount cash = deposit.accept(TPC);
    assertEquals("DepositDefinition: today payment", NOTIONAL + deposit.getInterestAmount(), cash.getAmount(deposit.getCurrency()), TOLERANCE_PRICE);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

}
