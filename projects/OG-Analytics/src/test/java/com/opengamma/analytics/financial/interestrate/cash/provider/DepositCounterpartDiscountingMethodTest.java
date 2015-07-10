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

import com.opengamma.analytics.financial.instrument.cash.DepositCounterpartDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.generator.EURDeposit;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositCounterpart;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.calculator.generic.TodayPaymentCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ParSpreadMarketQuoteIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.ParameterSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.ParameterSensitivityIssuerDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.SimpleParameterSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.SimpleParameterSensitivityIssuerDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
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
  private static final LegalEntity ISSUER = new LegalEntity(null, ISSUER_NAME, null, null, null);

  private static final IssuerProviderDiscount PROVIDER_ISSUER = MulticurveProviderDiscountDataSets.createIssuerProvider();
  private static final DepositCounterpartDiscountingMethod METHOD_DEPOSIT = DepositCounterpartDiscountingMethod.getInstance();

  private static final double SHIFT_FD = 1.0E-6;

  private static final PresentValueIssuerCalculator PVC = PresentValueIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();
  private static final ParameterSensitivityIssuerCalculator PS_PV_C = new ParameterSensitivityIssuerCalculator(PVCSIC);
  private static final ParameterSensitivityIssuerDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityIssuerDiscountInterpolatedFDCalculator(PVC, SHIFT_FD);

  private static final ParSpreadMarketQuoteIssuerDiscountingCalculator PSMQIDC = ParSpreadMarketQuoteIssuerDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator PSMQCSIDC = ParSpreadMarketQuoteCurveSensitivityIssuerDiscountingCalculator.getInstance();
  private static final SimpleParameterSensitivityIssuerCalculator<ParameterIssuerProviderInterface> PS_PSMQ_C = new SimpleParameterSensitivityIssuerCalculator<>(PSMQCSIDC);
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
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    final double dfEnd = PROVIDER_ISSUER.getDiscountFactor(ISSUER, deposit.getEndTime());
    final double dfStart = PROVIDER_ISSUER.getDiscountFactor(ISSUER, deposit.getStartTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value when the valuation date is on trade date. Compare Method to Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvMethod = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    final MultipleCurrencyAmount pvCalculator = deposit.accept(PVC, PROVIDER_ISSUER);
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenTradeAndSettle() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 13);
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    final double dfEnd = PROVIDER_ISSUER.getDiscountFactor(ISSUER, deposit.getEndTime());
    final double dfStart = PROVIDER_ISSUER.getDiscountFactor(ISSUER, deposit.getStartTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueSettle() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    final double dfEnd = PROVIDER_ISSUER.getDiscountFactor(ISSUER, deposit.getEndTime());
    final double dfStart = PROVIDER_ISSUER.getDiscountFactor(ISSUER, deposit.getStartTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd - NOTIONAL * dfStart;
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    final double dfEnd = PROVIDER_ISSUER.getDiscountFactor(ISSUER, deposit.getEndTime());
    final double pvExpected = (NOTIONAL + deposit.getInterestAmount()) * dfEnd;
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value.
   */
  public void presentValueMaturity() {
    final ZonedDateTime referenceDate = END_DATE;
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_ISSUER);
    final double pvExpected = NOTIONAL + deposit.getInterestAmount();
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PS_PV_C.calculateSensitivity(deposit, PROVIDER_ISSUER, PROVIDER_ISSUER.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PS_PV_FDC.calculateSensitivity(deposit, PROVIDER_ISSUER);
    AssertSensitivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is between settle date and maturity.
   */
  public void presentValueCurveSensitivityBetweenSettleMaturity() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 20);
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PS_PV_C.calculateSensitivity(deposit, PROVIDER_ISSUER, PROVIDER_ISSUER.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PS_PV_FDC.calculateSensitivity(deposit, PROVIDER_ISSUER);
    AssertSensitivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests parSpread when the present is before the deposit start date.
   */
  public void parSpreadBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER_ISSUER);
    final DepositCounterpartDefinition deposit0Definition = DepositCounterpartDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE + parSpread, GENERATOR, ISSUER_NAME);
    final DepositCounterpart deposit0 = deposit0Definition.toDerivative(referenceDate);
    final MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER_ISSUER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread when the present date is on the start date.
   */
  public void parSpreadOnStart() {
    final ZonedDateTime referenceDate = DEPOSIT_CPTY_DEFINITION.getStartDate();
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER_ISSUER);
    final DepositCounterpartDefinition deposit0Definition = DepositCounterpartDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE + parSpread, GENERATOR, ISSUER_NAME);
    final DepositCounterpart deposit0 = deposit0Definition.toDerivative(referenceDate);
    final MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER_ISSUER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread when the present date is after the start: .
   */
  public void parSpreadAfterStart() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(DEPOSIT_CPTY_DEFINITION.getStartDate(), 1, TARGET);
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER_ISSUER); // Spread will be -(1/delta+rate), as there is no initial amount
    final DepositCounterpartDefinition deposit0Definition = DepositCounterpartDefinition.fromStart(SPOT_DATE, DEPOSIT_PERIOD, NOTIONAL, RATE + parSpread, GENERATOR, ISSUER_NAME);
    final DepositCounterpart deposit0 = deposit0Definition.toDerivative(referenceDate);
    final MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER_ISSUER);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread.
   */
  public void parSpreadMethodVsCalculator() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final double parSpreadMethod = METHOD_DEPOSIT.parSpread(deposit, PROVIDER_ISSUER);
    final double parSpreadCalculator = deposit.accept(PSMQIDC, PROVIDER_ISSUER);
    assertEquals("DepositDefinition: present value", parSpreadMethod, parSpreadCalculator, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivity() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final SimpleParameterSensitivity pspsDepositExact = PS_PSMQ_C.calculateSensitivity(deposit, PROVIDER_ISSUER, PROVIDER_ISSUER.getAllNames());
    final SimpleParameterSensitivity pspsDepositFD = PS_PSMQ_FDC.calculateSensitivity(deposit, PROVIDER_ISSUER);
    AssertSensitivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pspsDepositExact, pspsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivityMethodVsCalculator() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final MulticurveSensitivity pscsMethod = METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, PROVIDER_ISSUER);
    final MulticurveSensitivity pscsCalculator = deposit.accept(PSMQCSIDC, PROVIDER_ISSUER);
    AssertSensitivityObjects.assertEquals("CashDiscountingProviderMethod: parSpreadCurveSensitivity", pscsMethod, pscsCalculator, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests today payment amount when the present is before the deposit start date.
   */
  public void todayPaymentBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
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
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
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
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
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
    final DepositCounterpart deposit = DEPOSIT_CPTY_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount cash = deposit.accept(TPC);
    assertEquals("DepositDefinition: today payment", NOTIONAL + deposit.getInterestAmount(), cash.getAmount(deposit.getCurrency()), TOLERANCE_PV);
    assertEquals("DepositDefinition: today payment", 1, cash.getCurrencyAmounts().length);
  }

}
