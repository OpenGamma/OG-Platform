/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cash.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.SimpleParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of cash deposits by discounting.
 */
@Test(groups = TestGroup.UNIT)
public class DepositIborDiscountingMethodTest {

  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 12, 12);

  private static final double NOTIONAL = 100000000;
  private static final double RATE = 0.0250;
  private static final DepositIborDefinition DEPOSIT_IBOR_DEFINITION = DepositIborDefinition.fromTrade(TRADE_DATE, NOTIONAL, RATE, EURIBOR3M, CALENDAR);

  private static final MulticurveProviderDiscount PROVIDER_MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT = DepositIborDiscountingMethod.getInstance();

  private static final double SHIFT_FD = 1.0E-6;

  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PS_PV_C = new ParameterSensitivityParameterCalculator<>(PVCSC);
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PS_PV_FDC = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVC, SHIFT_FD);

  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final SimpleParameterSensitivityParameterCalculator<ParameterProviderInterface> PS_PSMQ_C = new SimpleParameterSensitivityParameterCalculator<>(PSMQCSDC);
  private static final SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PS_PSMQ_FDC = new SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PSMQDC, SHIFT_FD);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_SPREAD = 1.0E-10;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  /**
   * Tests present value when the valuation date is on trade date.
   */
  public void presentValueTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositIbor deposit = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvComputed = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_MULTICURVES);
    final double dfEnd = PROVIDER_MULTICURVES.getDiscountFactor(deposit.getCurrency(), deposit.getEndTime());
    final double forward = PROVIDER_MULTICURVES.getSimplyCompoundForwardRate(deposit.getIndex(), deposit.getStartTime(), deposit.getEndTime(), deposit.getAccrualFactor());
    final double pvExpected = deposit.getAccrualFactor() * (deposit.getRate() - forward) * dfEnd;
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value when the valuation date is on trade date. Compare Method to Calculator.
   */
  public void presentValueMethodVsCalculator() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositIbor deposit = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyAmount pvMethod = METHOD_DEPOSIT.presentValue(deposit, PROVIDER_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = deposit.accept(PVC, PROVIDER_MULTICURVES);
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivityTrade() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositIbor deposit = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PS_PV_C.calculateSensitivity(deposit, PROVIDER_MULTICURVES, PROVIDER_MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PS_PV_FDC.calculateSensitivity(deposit, PROVIDER_MULTICURVES);
    AssertSensitivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date. Compare Method to Calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 12, 12);
    final DepositIbor deposit = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_DEPOSIT.presentValueCurveSensitivity(deposit, PROVIDER_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = deposit.accept(PVCSC, PROVIDER_MULTICURVES);
    assertEquals("DepositCounterpartDiscountingMethod: present value", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Tests parSpread when the present is before the deposit start date.
   */
  public void parSpreadBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositIbor deposit = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final double parSpread = METHOD_DEPOSIT.parSpread(deposit, PROVIDER_MULTICURVES);
    final DepositIborDefinition deposit0Definition = DepositIborDefinition.fromTrade(referenceDate, NOTIONAL, RATE + parSpread, EURIBOR3M, CALENDAR);
    final DepositIbor deposit0 = deposit0Definition.toDerivative(referenceDate);
    final MultipleCurrencyAmount pv0 = METHOD_DEPOSIT.presentValue(deposit0, PROVIDER_MULTICURVES);
    assertEquals("DepositDefinition: present value", 0, pv0.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests parSpread.
   */
  public void parSpreadMethodVsCalculator() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositIbor deposit = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final double parSpreadMethod = METHOD_DEPOSIT.parSpread(deposit, PROVIDER_MULTICURVES);
    final double parSpreadCalculator = deposit.accept(PSMQDC, PROVIDER_MULTICURVES);
    assertEquals("DepositDefinition: present value", parSpreadMethod, parSpreadCalculator, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests parRate when the present is before the deposit start date.
   */
  public void parRateBeforeStart() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositIbor deposit = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final double parRate = METHOD_DEPOSIT.parRate(deposit, PROVIDER_MULTICURVES);
    double parRateExpected = PROVIDER_MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, deposit.getStartTime(), deposit.getEndTime(), deposit.getAccrualFactor());
    assertEquals("DepositDefinition: present value", parRateExpected, parRate, TOLERANCE_SPREAD);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivity() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositIbor deposit = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final SimpleParameterSensitivity pspsDepositExact = PS_PSMQ_C.calculateSensitivity(deposit, PROVIDER_MULTICURVES, PROVIDER_MULTICURVES.getAllNames());
    final SimpleParameterSensitivity pspsDepositFD = PS_PSMQ_FDC.calculateSensitivity(deposit, PROVIDER_MULTICURVES);
    AssertSensitivityObjects.assertEquals("DepositCounterpartDiscountingMethod: presentValueCurveSensitivity ", pspsDepositExact, pspsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests parSpread curve sensitivity.
   */
  public void parSpreadCurveSensitivityMethodVsCalculator() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final DepositIbor deposit = DEPOSIT_IBOR_DEFINITION.toDerivative(referenceDate);
    final MulticurveSensitivity pscsMethod = METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, PROVIDER_MULTICURVES);
    final MulticurveSensitivity pscsCalculator = deposit.accept(PSMQCSDC, PROVIDER_MULTICURVES);
    AssertSensitivityObjects.assertEquals("CashDiscountingProviderMethod: parSpreadCurveSensitivity", pscsMethod, pscsCalculator, TOLERANCE_SPREAD);
  }

}
