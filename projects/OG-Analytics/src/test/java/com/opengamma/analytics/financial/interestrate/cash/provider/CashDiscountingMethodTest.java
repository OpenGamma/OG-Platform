/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.TodayPaymentCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.SimpleParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of cash deposits by discounting.
 */
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

  private static final MulticurveProviderDiscount PROVIDER = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();

  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();

  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();

  private static final double SHIFT_FD = 1.0E-6;

  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PS_PV_C = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVC, SHIFT_FD);

  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final SimpleParameterSensitivityParameterCalculator<MulticurveProviderInterface> PS_PSMQ_C = new SimpleParameterSensitivityParameterCalculator<>(PSMQCSDC);
  private static final SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PS_PSMQ_FDC = new SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PSMQDC, SHIFT_FD);

  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move on 100m.

  @Test
  /**
   * Tests present value when the valuation date is on trade date.
   */
  public void presentValueTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER);
    final double dfEnd = PROVIDER.getDiscountFactor(EUR, deposit.getEndTime());
    final double dfStart = PROVIDER.getDiscountFactor(EUR, deposit.getStartTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenTradeAndSettle() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER);
    final double dfEnd = PROVIDER.getDiscountFactor(EUR, deposit.getEndTime());
    final double dfStart = PROVIDER.getDiscountFactor(EUR, deposit.getStartTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueSettle() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER);
    final double dfEnd = PROVIDER.getDiscountFactor(EUR, deposit.getEndTime());
    final double dfStart = PROVIDER.getDiscountFactor(EUR, deposit.getStartTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER);
    final double dfEnd = PROVIDER.getDiscountFactor(EUR, deposit.getEndTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd;
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueMaturity() {
    final ZonedDateTime referenceDate = END_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER);
    final double pvExpected = NOTIONAL + deposit.getInterestAmount();
    assertEquals("DepositDefinition: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PS_PV_C.calculateSensitivity(deposit, PROVIDER, PROVIDER.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PS_PV_FDC.calculateSensitivity(deposit, PROVIDER);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is between settle date and maturity.
   */
  public void presentValueCurveSensitivityBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PS_PV_C.calculateSensitivity(deposit, PROVIDER, PROVIDER.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PS_PV_FDC.calculateSensitivity(deposit, PROVIDER);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests parRate when the present is before the deposit start date.
   */
  public void parRateBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double parRate = METHOD_DEPOSIT.parRate(deposit, PROVIDER);
    final CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, parRate, DEPOSIT_AF);
    final Cash deposit0 = deposit0Definition.toDerivative(referenceDate);
    final MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER);
    assertEquals("DepositDefinition: par rate", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parRate method vs calculator.
   */
  public void parRateMethodVsCalculator() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double parRateMethod = METHOD_DEPOSIT.parRate(deposit, PROVIDER);
    final double parRateCalculator = deposit.accept(PRDC, PROVIDER);
    assertEquals("DepositDefinition: par rate", parRateMethod, parRateCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests parSpread when the present is before the deposit start date.
   */
  public void parSpreadBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER);
    final CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE + parSpread, DEPOSIT_AF);
    final Cash deposit0 = deposit0Definition.toDerivative(referenceDate);
    final MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER);
    assertEquals("DepositDefinition: par spread", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread when the present date is on the start date.
   */
  public void parSpreadOnStart() {
    final ZonedDateTime referenceDate = DEPOSIT_DEFINITION.getStartDate();
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER);
    final CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE + parSpread, DEPOSIT_AF);
    final Cash deposit0 = deposit0Definition.toDerivative(referenceDate);
    final MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread when the present date is after the start: .
   */
  public void parSpreadAfterStart() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(DEPOSIT_DEFINITION.getStartDate(), 1, TARGET);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER); // Spread will be -(1/delta+rate), as there is no initial amount
    final CashDefinition deposit0Definition = new CashDefinition(EUR, SPOT_DATE, END_DATE, NOTIONAL, RATE + parSpread, DEPOSIT_AF);
    final Cash deposit0 = deposit0Definition.toDerivative(referenceDate);
    final MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread.
   */
  public void parSpreadMethodVsCalculator() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final double parSpreadMethod = METHOD_DEPOSIT.parSpread(deposit, PROVIDER);
    final double parSpreadCalculator = deposit.accept(PSMQDC, PROVIDER);
    assertEquals("DepositDefinition: present value", parSpreadMethod, parSpreadCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivity() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final SimpleParameterSensitivity pspsDepositExact = PS_PSMQ_C.calculateSensitivity(deposit, PROVIDER, PROVIDER.getAllNames());
    final SimpleParameterSensitivity pspsDepositFD = PS_PSMQ_FDC.calculateSensitivity(deposit, PROVIDER);
    AssertSensivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pspsDepositExact, pspsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivityMethodVsCalculator() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MulticurveSensitivity pscsMethod = METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, PROVIDER);
    final MulticurveSensitivity pscsCalculator = deposit.accept(PSMQCSDC, PROVIDER);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: parSpreadCurveSensitivity", pscsMethod, pscsCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests today payment amount when the present is before the deposit start date.
   */
  public void todayPaymentBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount cash = deposit.accept(TPC);
    assertEquals("DepositDefinition: today payment", 0.0, cash.getAmount(deposit.getCurrency()), TOLERANCE_PV);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests today payment amount when the present is on the deposit start date.
   */
  public void todayPaymentOnStart() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount cash = deposit.accept(TPC);
    assertEquals("DepositDefinition: today payment", -NOTIONAL, cash.getAmount(deposit.getCurrency()), TOLERANCE_PV);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests today payment amount when the present is on the deposit start date.
   */
  public void todayPaymentBetweenStartAndEnd() {
    final ZonedDateTime referenceDate = SPOT_DATE.plusDays(2);
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount cash = deposit.accept(TPC);
    assertEquals("DepositDefinition: today payment", 0.0, cash.getAmount(deposit.getCurrency()), TOLERANCE_PV);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

  @Test
  /**
   * Tests today payment amount when the present is on the deposit end date.
   */
  public void todayPaymentOnEnd() {
    final ZonedDateTime referenceDate = END_DATE;
    final Cash deposit = DEPOSIT_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount cash = deposit.accept(TPC);
    assertEquals("DepositDefinition: today payment", NOTIONAL + deposit.getInterestAmount(), cash.getAmount(deposit.getCurrency()), TOLERANCE_PV);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

}
