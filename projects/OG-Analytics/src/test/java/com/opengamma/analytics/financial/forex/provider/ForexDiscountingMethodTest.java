/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.CurrencyExposureDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.TodayPaymentCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.SimpleParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the method for Forex transaction by discounting on each payment.
 */
@Test(groups = TestGroup.UNIT)
public class ForexDiscountingMethodTest {
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountForexDataSets.createMulticurvesForex();
  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 5, 24);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 20);

  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final PaymentFixedDefinition PAY_DEFINITION_1 = new PaymentFixedDefinition(CUR_1, PAYMENT_DATE, NOMINAL_1);
  private static final PaymentFixed PAY_1 = PAY_DEFINITION_1.toDerivative(REFERENCE_DATE);
  private static final PaymentFixedDefinition PAY_DEFINITION_2 = new PaymentFixedDefinition(CUR_2, PAYMENT_DATE, -NOMINAL_1 * FX_RATE);
  private static final PaymentFixed PAY_2 = PAY_DEFINITION_2.toDerivative(REFERENCE_DATE);

  private static final ForexDiscountingMethod METHOD_FX = ForexDiscountingMethod.getInstance();
  private static final PaymentFixedDiscountingMethod METHOD_PAY = PaymentFixedDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVSCDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final CurrencyExposureDiscountingCalculator CEDC = CurrencyExposureDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final SimpleParameterSensitivityParameterCalculator<ParameterProviderInterface> PSPSC = new SimpleParameterSensitivityParameterCalculator<>(
      PSMQCSDC);
  private static final double SHIFT = 1.0E-7;
  private static final SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSMQCS_FDC = new SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(
      PSMQDC, SHIFT);
  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();
  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_RATE_DELTA = 1.0E-8;

  /**
   * Tests the present value computation.
   */
  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD_FX.presentValue(FX, MULTICURVES);
    final MultipleCurrencyAmount ca1 = METHOD_PAY.presentValue(PAY_1, MULTICURVES);
    final MultipleCurrencyAmount ca2 = METHOD_PAY.presentValue(PAY_2, MULTICURVES);
    assertEquals("ForexDiscountingMethod: presentValue", ca1.plus(ca2), pv);
  }

  /**
   * Test the present value through the method and through the calculator.
   */
  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_FX.presentValue(FX, MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = FX.accept(PVDC, MULTICURVES);
    assertEquals("ForexDiscountingMethod: presentValue: Method vs Calculator", pvMethod, pvCalculator);
  }

  /**
   * Test the present value of EUR/USD is the same as an USD/EUR.
   */
  @Test
  public void presentValueReverse() {
    final ForexDefinition fxReverseDefinition = new ForexDefinition(CUR_2, CUR_1, PAYMENT_DATE, -NOMINAL_1 * FX_RATE, 1.0 / FX_RATE);
    final Forex fxReverse = fxReverseDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pv = METHOD_FX.presentValue(FX, MULTICURVES);
    final MultipleCurrencyAmount pvReverse = METHOD_FX.presentValue(fxReverse, MULTICURVES);
    assertEquals("Forex present value: Reverse description", pv.getAmount(CUR_1), pvReverse.getAmount(CUR_1), TOLERANCE_PV);
    assertEquals("Forex present value: Reverse description", pv.getAmount(CUR_2), pvReverse.getAmount(CUR_2), TOLERANCE_PV);
  }

  /**
   * Tests the currency exposure computation.
   */
  @Test
  public void currencyExposure() {
    final MultipleCurrencyAmount exposureMethod = METHOD_FX.currencyExposure(FX, MULTICURVES);
    final MultipleCurrencyAmount pv = METHOD_FX.presentValue(FX, MULTICURVES);
    assertEquals("Currency exposure", pv, exposureMethod);
    final MultipleCurrencyAmount exposureCalculator = FX.accept(CEDC, MULTICURVES);
    assertEquals("Currency exposure: Method vs Calculator", exposureMethod, exposureCalculator);
  }

  /**
   * Tests the forward Forex rate computation.
   */
  @Test
  public void forwardRate() {
    final double fwd = METHOD_FX.forwardForexRate(FX, MULTICURVES);
    final double dfDomestic = MULTICURVES.getDiscountFactor(FX.getCurrency1(), FX.getPaymentTime());
    final double dfForeign = MULTICURVES.getDiscountFactor(FX.getCurrency2(), FX.getPaymentTime());
    final double fwdExpected = MULTICURVES.getFxRate(FX.getCurrency1(), FX.getCurrency2()) * dfDomestic / dfForeign;
    assertEquals("Forex: forward rate", fwdExpected, fwd, TOLERANCE_RATE);
  }

  /**
   * Tests the forward Forex rate through the method and through the calculator.
   */
  @Test
  public void forwardRateMethodVsCalculator() {
    final double fwdMethod = METHOD_FX.forwardForexRate(FX, MULTICURVES);
    final ParRateDiscountingCalculator PRC = ParRateDiscountingCalculator.getInstance();
    final double fwdCalculator = FX.accept(PRC, MULTICURVES);
    assertEquals("Forex: forward rate", fwdMethod, fwdCalculator, TOLERANCE_RATE);
  }

  /**
   * Tests the parSpread for forex transactions.
   */
  @Test
  public void parSpread() {
    final double ps = METHOD_FX.parSpread(FX, MULTICURVES);
    final ForexDefinition fx0Definition = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE + ps);
    final Forex fx0 = fx0Definition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pv0 = fx0.accept(PVDC, MULTICURVES);
    assertEquals("ForexDiscountingMethod: parSpread", 0, MULTICURVES.getFxRates().convert(pv0, CUR_1).getAmount(), TOLERANCE_RATE);
  }

  /**
   * Tests the par spread method vs calculator
   */
  @Test
  public void parSpreadMethodVsCalculator() {
    final double parSpreadMethod = METHOD_FX.parSpread(FX, MULTICURVES);
    final double parSpreadCalculator = FX.accept(PSMQDC, MULTICURVES);
    assertEquals("Forex: par spread", parSpreadMethod, parSpreadCalculator, TOLERANCE_RATE);
  }

  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  @Test
  public void forexTodayPaymentBeforePayment() {
    final Forex fx = FX_DEFINITION.toDerivative(PAYMENT_DATE.minusDays(1));
    final MultipleCurrencyAmount cash = fx.accept(TPC);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(fx.getCurrency1()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 0.0, cash.getAmount(fx.getCurrency2()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  /**
   * Tests the TodayPaymentCalculator for forex transactions.
   */
  @Test
  public void forexTodayPaymentOnPayment() {
    final Forex fx = FX_DEFINITION.toDerivative(PAYMENT_DATE);
    final MultipleCurrencyAmount cash = fx.accept(TPC);
    assertEquals("TodayPaymentCalculator: forex", FX_DEFINITION.getPaymentCurrency1().getReferenceAmount(), cash.getAmount(fx.getCurrency1()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", FX_DEFINITION.getPaymentCurrency2().getReferenceAmount(), cash.getAmount(fx.getCurrency2()), TOLERANCE_PV);
    assertEquals("TodayPaymentCalculator: forex", 2, cash.getCurrencyAmounts().length);
  }

  /**
   * Test the present value sensitivity to interest rate.
   */
  @Test
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyMulticurveSensitivity pvcs = METHOD_FX.presentValueCurveSensitivity(FX, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvs1 = PAY_1.accept(PVSCDC, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvs2 = PAY_2.accept(PVSCDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("ForexDiscountingMethod: presentValueCurveSensitivity", pvs1.plus(pvs2).cleaned(), pvcs.cleaned(), TOLERANCE_PV_DELTA);
  }

  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_FX.presentValueCurveSensitivity(FX, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = FX.accept(PVSCDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

  /**
   * Tests the par spread curve sensitivity versus a finite difference computation.
   */
  @Test
  public void parSpreadCurveSensitivity() {
    final SimpleParameterSensitivity psComputed = PSPSC.calculateSensitivity(FX, MULTICURVES, MULTICURVES.getAllNames());
    final SimpleParameterSensitivity psFD = PSMQCS_FDC.calculateSensitivity(FX, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", psFD, psComputed, TOLERANCE_RATE_DELTA);
  }

  /**
   * Tests the par spread curve sensitivity through the method and through the calculator.
   */
  @Test
  public void parSpreadCurveSensitivityMethodVsCalculator() {
    final MulticurveSensitivity pvcsMethod = METHOD_FX.parSpreadCurveSensitivity(FX, MULTICURVES);
    final MulticurveSensitivity pvcsCalculator = FX.accept(PSMQCSDC, MULTICURVES);
    assertEquals("Forex swap present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

}
