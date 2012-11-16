/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.provider;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.TodayPaymentCalculator;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.provider.calculator.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.ParameterSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.ParameterSensitivityIssuerDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.SimpleParameterSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests related to the pricing of cash deposits by discounting.
 */
public class DepositCounterpartDiscountingMethodTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorDeposit GENERATOR = new EURDeposit(TARGET);
  private static final Currency EUR = GENERATOR.getCurrency();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, GENERATOR.getSpotLag(), TARGET);

  private static final String ISSUER_NAME = MulticurveProviderDiscountDataSets.getIssuerNames()[2];
  private static final double NOTIONAL = 100000000;
  private static final double RATE = 0.0250;
  private static final Period DEPOSIT_PERIOD = Period.ofMonths(6);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_DATE, DEPOSIT_PERIOD, GENERATOR);
  private static final DepositCounterpartDefinition DEPOSIT_CPTY_DEFINITION = DepositCounterpartDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE, GENERATOR, ISSUER_NAME);
  private static final Pair<String, Currency> ISSUER_CCY = new ObjectsPair<String, Currency>(ISSUER_NAME, EUR);

  private static final IssuerProviderDiscount PROVIDER_ISSUER = MulticurveProviderDiscountDataSets.createIssuerProvider();
  private static final String[] NOT_USED = new String[] {"Not used 1"};

  private static final DepositCounterpartDiscountingMethod METHOD_DEPOSIT = DepositCounterpartDiscountingMethod.getInstance();

  private static final double SHIFT_FD = 1.0E-6;

  private static final PresentValueIssuerCalculator PVC = PresentValueIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final ParameterSensitivityIssuerCalculator PS_PV_C = new ParameterSensitivityIssuerCalculator(PVCSIC);
  private static final ParameterSensitivityIssuerDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityIssuerDiscountInterpolatedFDCalculator(PVC, SHIFT_FD);

  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator PSMQIDC = ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSMQCSIDC = ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();
  private static final SimpleParameterSensitivityIssuerCalculator PS_PSMQ_C = new SimpleParameterSensitivityIssuerCalculator(PSMQCSIDC);
  private static final SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator PS_PSMQ_FDC = new SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator(PSMQIDC, SHIFT_FD);

  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_SPREAD = 1.0E-10;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  /**
   * Tests present value when the valuation date is on trade date.
   */
  public void presentValueTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    double dfEnd = PROVIDER_ISSUER.getDiscountFactor(ISSUER_CCY, deposit.getEndTime());
    double dfStart = PROVIDER_ISSUER.getDiscountFactor(ISSUER_CCY, deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value when the valuation date is on trade date. Compare Method to Calculator.
   */
  public void presentValueMethodVsCalculator() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvMethod = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    MultipleCurrencyAmount pvCalculator = PVC.visit(deposit, PROVIDER_ISSUER);
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenTradeAndSettle() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    double dfEnd = PROVIDER_ISSUER.getDiscountFactor(ISSUER_CCY, deposit.getEndTime());
    double dfStart = PROVIDER_ISSUER.getDiscountFactor(ISSUER_CCY, deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueSettle() {
    ZonedDateTime referenceDate = SPOT_DATE;
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    double dfEnd = PROVIDER_ISSUER.getDiscountFactor(ISSUER_CCY, deposit.getEndTime());
    double dfStart = PROVIDER_ISSUER.getDiscountFactor(ISSUER_CCY, deposit.getStartTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    double dfEnd = PROVIDER_ISSUER.getDiscountFactor(ISSUER_CCY, deposit.getEndTime());
    double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd;
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueMaturity() {
    ZonedDateTime referenceDate = END_DATE;
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    double pvExpected = NOTIONAL + deposit.getInterestAmount();
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityTrade() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyParameterSensitivity pvpsDepositExact = PS_PV_C.calculateSensitivity(deposit, PROVIDER_ISSUER, PROVIDER_ISSUER.getAllNames());
    MultipleCurrencyParameterSensitivity pvpsDepositFD = PS_PV_FDC.calculateSensitivity(deposit, PROVIDER_ISSUER);
    AssertSensivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is between settle date and maturity.
   */
  public void presentValueCurveSensitivityBetweenSettleMaturity() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyParameterSensitivity pvpsDepositExact = PS_PV_C.calculateSensitivity(deposit, PROVIDER_ISSUER, PROVIDER_ISSUER.getAllNames());
    MultipleCurrencyParameterSensitivity pvpsDepositFD = PS_PV_FDC.calculateSensitivity(deposit, PROVIDER_ISSUER);
    AssertSensivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests parSpread when the present is before the deposit start date.
   */
  public void parSpreadBeforeStart() {
    ZonedDateTime referenceDate = TRADE_DATE;
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER_ISSUER);
    DepositCounterpartDefinition deposit0Definition = DepositCounterpartDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE + parSpread, GENERATOR, ISSUER_NAME);
    DepositCounterpart deposit0 = deposit0Definition.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER_ISSUER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread when the present date is on the start date.
   */
  public void parSpreadOnStart() {
    ZonedDateTime referenceDate = DEPOSIT_CPTY_DEFINITION.getStartDate();
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER_ISSUER);
    DepositCounterpartDefinition deposit0Definition = DepositCounterpartDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE + parSpread, GENERATOR, ISSUER_NAME);
    DepositCounterpart deposit0 = deposit0Definition.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER_ISSUER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread when the present date is after the start: .
   */
  public void parSpreadAfterStart() {
    ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(DEPOSIT_CPTY_DEFINITION.getStartDate(), 1, TARGET);
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER_ISSUER); // Spread will be -(1/delta+rate), as there is no initial amount
    DepositCounterpartDefinition deposit0Definition = DepositCounterpartDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE + parSpread, GENERATOR, ISSUER_NAME);
    DepositCounterpart deposit0 = deposit0Definition.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER_ISSUER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread.
   */
  public void parSpreadMethodVsCalculator() {
    ZonedDateTime referenceDate = TRADE_DATE;
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    double parSpreadMethod = METHOD_DEPOSIT.parSpread(deposit, PROVIDER_ISSUER);
    double parSpreadCalculator = PSMQIDC.visit(deposit, PROVIDER_ISSUER);
    assertEquals("DepositDefinition: present value", parSpreadMethod, parSpreadCalculator, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivity() {
    ZonedDateTime referenceDate = TRADE_DATE;
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    SimpleParameterSensitivity pspsDepositExact = PS_PSMQ_C.calculateSensitivity(deposit, PROVIDER_ISSUER, PROVIDER_ISSUER.getAllNames());
    SimpleParameterSensitivity pspsDepositFD = PS_PSMQ_FDC.calculateSensitivity(deposit, PROVIDER_ISSUER);
    AssertSensivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pspsDepositExact, pspsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivityMethodVsCalculator() {
    ZonedDateTime referenceDate = TRADE_DATE;
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MulticurveSensitivity pscsMethod = METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, PROVIDER_ISSUER);
    MulticurveSensitivity pscsCalculator = PSMQCSIDC.visit(deposit, PROVIDER_ISSUER);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: parSpreadCurveSensitivity", pscsMethod, pscsCalculator, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests today payment amount when the present is before the deposit start date.
   */
  public void todayPaymentBeforeStart() {
    ZonedDateTime referenceDate = TRADE_DATE;
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
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
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
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
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
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
    DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate, NOT_USED);
    MultipleCurrencyAmount cash = TPC.visit(deposit);
    assertEquals("DepositDefinition: today payment", NOTIONAL + deposit.getInterestAmount(), cash.getAmount(deposit.getCurrency()), TOLERANCE_PV);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

}
