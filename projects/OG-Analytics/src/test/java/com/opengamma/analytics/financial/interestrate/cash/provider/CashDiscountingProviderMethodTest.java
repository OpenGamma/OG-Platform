/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TreeSet;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.curve.sensitivity.ParameterSensitivity;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.TodayPaymentCalculator;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.market.description.ProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.calculator.ParSpreadMarketQuoteCurveSensitivityDiscountingProviderCalculator;
import com.opengamma.analytics.financial.provider.calculator.ParSpreadMarketQuoteDiscountingProviderCalculator;
import com.opengamma.analytics.financial.provider.calculator.PresentValueCurveSensitivityDiscountingProviderCalculator;
import com.opengamma.analytics.financial.provider.calculator.PresentValueDiscountingProviderCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.ParameterSensitivityDiscountInterpolatedFDProviderCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.ParameterSensitivityProviderCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of cash deposits by discounting.
 */
public class CashDiscountingProviderMethodTest {

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

  private static final MulticurveProviderDiscount PROVIDER = ProviderDiscountDataSets.createProvider3();
  private static final String[] NOT_USED = new String[] {"Not used 1"};

  private static final CashDiscountingProviderMethod METHOD_DEPOSIT = CashDiscountingProviderMethod.getInstance();
  private static final ParSpreadMarketQuoteDiscountingProviderCalculator PSMQC = ParSpreadMarketQuoteDiscountingProviderCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingProviderCalculator PSMQCSC = ParSpreadMarketQuoteCurveSensitivityDiscountingProviderCalculator.getInstance();
  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();

  private static final PresentValueCurveSensitivityDiscountingProviderCalculator PVCSC = PresentValueCurveSensitivityDiscountingProviderCalculator.getInstance();
  private static final PresentValueDiscountingProviderCalculator PVC = PresentValueDiscountingProviderCalculator.getInstance();
  private static final ParameterSensitivityProviderCalculator PSC = new ParameterSensitivityProviderCalculator(PVCSC);
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityDiscountInterpolatedFDProviderCalculator PSC_DSC_FD = new ParameterSensitivityDiscountInterpolatedFDProviderCalculator(PVC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_SPREAD = 1.0E-10;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  /**
   * Tests present value when the valuation date is on trade date.
   */
  public void presentValueTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER);
    double dfEnd = PROVIDER.getDiscountFactor(EUR, deposit.getEndTime());
    double dfStart = PROVIDER.getDiscountFactor(EUR, deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenTradeAndSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER);
    double dfEnd = PROVIDER.getDiscountFactor(EUR, deposit.getEndTime());
    double dfStart = PROVIDER.getDiscountFactor(EUR, deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueSettle() {
    ZonedDateTime referenceDate = SPOT_DATE;
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER);
    double dfEnd = PROVIDER.getDiscountFactor(EUR, deposit.getEndTime());
    double dfStart = PROVIDER.getDiscountFactor(EUR, deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER);
    double dfEnd = PROVIDER.getDiscountFactor(EUR, deposit.getEndTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueMaturity() {
    ZonedDateTime referenceDate = END_DATE;
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER);
    double pvExpected = NOTIONAL + deposit.getInterestAmount();
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    ParameterSensitivity pvpsDepositExact = PSC.calculateSensitivity(deposit, new TreeSet<String>(), PROVIDER);
    ParameterSensitivity pvpsDepositFD = PSC_DSC_FD.calculateSensitivity(deposit, PROVIDER);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is between settle date and maturity.
   */
  public void presentValueCurveSensitivityBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    ParameterSensitivity pvpsDepositExact = PSC.calculateSensitivity(deposit, new TreeSet<String>(), PROVIDER);
    ParameterSensitivity pvpsDepositFD = PSC_DSC_FD.calculateSensitivity(deposit, PROVIDER);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests parSpread when the present is before the deposit start date.
   */
  public void parSpreadBeforeStart() {
    ZonedDateTime referenceDate = TRADE_DATE;
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER);
    CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE + parSpread, DEPOSIT_AF);
    Cash deposit0 = deposit0Definition.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread when the present date is on the start date.
   */
  public void parSpreadOnStart() {
    ZonedDateTime referenceDate = DEPOSIT_DEFINITION.getStartDate();
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER);
    CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE + parSpread, DEPOSIT_AF);
    Cash deposit0 = deposit0Definition.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread when the present date is after the start: .
   */
  public void parSpreadAfterStart() {
    ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(DEPOSIT_DEFINITION.getStartDate(), 1, TARGET);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER); // Spread will be -(1/delta+rate), as there is no initial amount
    CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE + parSpread, DEPOSIT_AF);
    Cash deposit0 = deposit0Definition.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread.
   */
  public void parSpreadMethodVsCalculator() {
    ZonedDateTime referenceDate = TRADE_DATE;
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    double parSpreadMethod = METHOD_DEPOSIT.parSpread(deposit, PROVIDER);
    double parSpreadCalculator = PSMQC.visit(deposit, PROVIDER);
    assertEquals("DepositDefinition: present value", parSpreadMethod, parSpreadCalculator, TOLERANCE_SPREAD);
  }

  //    @Test TODO
  //    /**
  //     * Tests parSpread curve sensitivity.
  //     */
  //    public void parSpreadCurveSensitivity() {
  //      ZonedDateTime referenceDate = TRADE_DATE;
  //      Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
  //      CurveSensitivityMarket pscsMethod = METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, PROVIDER);
  //      final List<DoublesPair> sensiPvDisc = pscsMethod.getYieldDiscountingSensitivities().get(EUR);
  //      double ps = METHOD_DEPOSIT.parSpread(deposit, PROVIDER);
  //      final YieldAndDiscountCurve curveToBump = PROVIDER.getCurve(NOT_USED);
  //      double deltaShift = 0.0001;
  //      int nbNode = 2;
  //      double[] result = new double[nbNode];
  //      final double[] nodeTimesExtended = new double[nbNode + 1];
  //      nodeTimesExtended[1] = deposit.getStartTime();
  //      nodeTimesExtended[2] = deposit.getEndTime();
  //      final double[] yields = new double[nbNode + 1];
  //      yields[0] = curveToBump.getInterestRate(0.0);
  //      yields[1] = curveToBump.getInterestRate(nodeTimesExtended[1]);
  //      yields[2] = curveToBump.getInterestRate(nodeTimesExtended[2]);
  //      final YieldAndDiscountCurve curveNode = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
  //      for (int loopnode = 0; loopnode < nbNode; loopnode++) {
  //        final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
  //        PROVIDER.replaceCurve(NOT_USED, curveBumped);
  //        final double psBumped = METHOD_DEPOSIT.parSpread(deposit, PROVIDER);
  //        result[loopnode] = (psBumped - ps) / deltaShift;
  //        final DoublesPair pairPv = sensiPvDisc.get(loopnode);
  //        assertEquals("Sensitivity par spread to curve: Node " + loopnode, nodeTimesExtended[loopnode + 1], pairPv.getFirst(), TOLERANCE_TIME);
  //        assertEquals("Sensitivity par spread to curve: Node", pairPv.second, result[loopnode], TOLERANCE_PRICE);
  //      }
  //      PROVIDER.replaceCurve(NOT_USED, curveToBump);
  //      CurveSensitivityMarket prcsCalculator = PSMQCSC.visit(deposit, PROVIDER);
  //      prcsCalculator = prcsCalculator.cleaned(0.0, 1.0E-4);
  //      AssertSensivityObjects.assertEquals("DepositZero: par rate curve sensitivity", pscsMethod, prcsCalculator, TOLERANCE_SPREAD_DELTA);
  //    }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivityMethodVsCalculator() {
    ZonedDateTime referenceDate = TRADE_DATE;
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    CurveSensitivityMarket pscsMethod = METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, PROVIDER);
    CurveSensitivityMarket pscsCalculator = PSMQCSC.visit(deposit, PROVIDER);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: parSpreadCurveSensitivity", pscsMethod, pscsCalculator, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests today payment amount when the present is before the deposit start date.
   */
  public void todayPaymentBeforeStart() {
    ZonedDateTime referenceDate = TRADE_DATE;
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount cash = TPC.visit(deposit);
    assertEquals("DepositDefinition: today payment", 0.0, cash.getAmount(deposit.getCurrency()), TOLERANCE_PV);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests today payment amount when the present is on the deposit start date.
   */
  public void todayPaymentOnStart() {
    ZonedDateTime referenceDate = SPOT_DATE;
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount cash = TPC.visit(deposit);
    assertEquals("DepositDefinition: today payment", -NOTIONAL, cash.getAmount(deposit.getCurrency()), TOLERANCE_PV);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests today payment amount when the present is on the deposit start date.
   */
  public void todayPaymentBetweenStartAndEnd() {
    ZonedDateTime referenceDate = SPOT_DATE.plusDays(2);
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount cash = TPC.visit(deposit);
    assertEquals("DepositDefinition: today payment", 0.0, cash.getAmount(deposit.getCurrency()), TOLERANCE_PV);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests today payment amount when the present is on the deposit end date.
   */
  public void todayPaymentOnEnd() {
    ZonedDateTime referenceDate = END_DATE;
    Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount cash = TPC.visit(deposit);
    assertEquals("DepositDefinition: today payment", NOTIONAL + deposit.getInterestAmount(), cash.getAmount(deposit.getCurrency()), TOLERANCE_PV);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

}
