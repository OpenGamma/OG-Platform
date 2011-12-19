/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.cash.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.cash.DepositDefinition;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.instrument.index.generator.EURDeposit;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.cash.derivative.Cash;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing of cash deposits by disocunting.
 */
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
  private static final DepositDefinition DEPOSIT_DEFINITION = new DepositDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE, DEPOSIT_AF);

  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves2();
  private static final String[] CURVES_NAME = TestsDataSets.curves2Names();

  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-2;

  @Test
  /**
   * Tests present value when the valuation date is on trade date.
   */
  public void presentValueTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenTradeAndSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueSettle() {
    ZonedDateTime referenceDate = SPOT_DATE;
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    double dfStart = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    double dfEnd = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(deposit.getEndTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueMaturity() {
    ZonedDateTime referenceDate = END_DATE;
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    CurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, CURVES);
    double pvExpected = NOTIONAL + deposit.getInterestAmount();
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(), TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    InterestRateCurveSensitivity pvcsComputed = METHOD_DEPOSIT.presentValueCurveSensitivity(deposit, CURVES);
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsComputed.getSensitivities().size());
    assertEquals("DepositDefinition: present value curve sensitivity", 2, pvcsComputed.getSensitivities().get(CURVES_NAME[0]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // Discounting curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    Cash depositBunped = DEPOSIT_DEFINITION.toDerivative(referenceDate, bumpedCurveName);
    final double[] nodeTimesDisc = new double[] {deposit.getStartTime(), deposit.getEndTime()};
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(depositBunped, CURVES, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_DEPOSIT);
    final List<DoublesPair> sensiPvDisc = pvcsComputed.getSensitivities().get(CURVES_NAME[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, CURVES_NAME[0]);
    InterestRateCurveSensitivity pvcsComputed = METHOD_DEPOSIT.presentValueCurveSensitivity(deposit, CURVES);
    pvcsComputed = pvcsComputed.clean(0.0, 1.0E-4);
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsComputed.getSensitivities().size());
    assertEquals("DepositDefinition: present value curve sensitivity", 1, pvcsComputed.getSensitivities().get(CURVES_NAME[0]).size());
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // Discounting curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    Cash depositBunped = DEPOSIT_DEFINITION.toDerivative(referenceDate, bumpedCurveName);
    final double[] nodeTimesDisc = new double[] {deposit.getEndTime()};
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(depositBunped, CURVES, CURVES_NAME[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD_DEPOSIT);
    final List<DoublesPair> sensiPvDisc = pvcsComputed.getSensitivities().get(CURVES_NAME[0]);
    final DoublesPair pairPv = sensiPvDisc.get(0);
    assertEquals("Sensitivity coupon pv to forward curve: Node " + 0, nodeTimesDisc[0], pairPv.getFirst(), 1E-8);
    AssertJUnit.assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[0], deltaTolerancePrice);
  }

}
