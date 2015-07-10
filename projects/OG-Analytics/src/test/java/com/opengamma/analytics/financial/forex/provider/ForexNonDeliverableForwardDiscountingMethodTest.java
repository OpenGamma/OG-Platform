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
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.calculator.discounting.CurrencyExposureDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the valuation of non-deliverable forward by discounting.
 */
@Test(groups = TestGroup.UNIT)
public class ForexNonDeliverableForwardDiscountingMethodTest {

  private static final MulticurveProviderInterface MULTICURVES = MulticurveProviderDiscountForexDataSets.createMulticurvesForex();

  private static final Currency KRW = Currency.of("KRW");
  private static final Currency USD = Currency.USD;
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2012, 5, 2);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2012, 5, 4);
  private static final double NOMINAL_USD = 100000000; // 1m
  private static final double FX_RATE = 1123.45;
  private static final ForexNonDeliverableForwardDefinition NDF_DEFINITION = new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_DATE,
      PAYMENT_DATE);
  private static final ForexDefinition FOREX_DEFINITION = new ForexDefinition(USD, KRW, PAYMENT_DATE, NOMINAL_USD, FX_RATE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 11, 10);

  //  private static final double USD_KRW = 1111.11;
  private static final FXMatrix FX_MATRIX = MulticurveProviderDiscountForexDataSets.fxMatrix();

  private static final ForexNonDeliverableForward NDF = NDF_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final Forex FOREX = FOREX_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FX = ForexDiscountingMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final CurrencyExposureDiscountingCalculator CEC = CurrencyExposureDiscountingCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  /**
   * Tests the currency exposure.
   */
  public void currencyExposure() {
    final MultipleCurrencyAmount ce = METHOD_NDF.currencyExposure(NDF, MULTICURVES);
    final double df1 = MULTICURVES.getDiscountFactor(KRW, NDF.getPaymentTime());
    final double df2 = MULTICURVES.getDiscountFactor(USD, NDF.getPaymentTime());
    final double ce1 = -NOMINAL_USD * df1 * FX_RATE;
    final double ce2 = NOMINAL_USD * df2;
    assertEquals("Currency exposure - non-deliverable forward", ce1, ce.getAmount(KRW), TOLERANCE_PV);
    assertEquals("Currency exposure - non-deliverable forward", ce2, ce.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Checks that the NDF currency exposure is the same as the standard FX forward currency exposure.
   */
  public void currencyExposureVsForex() {
    final MultipleCurrencyAmount ceNDF = METHOD_NDF.currencyExposure(NDF, MULTICURVES);
    final MultipleCurrencyAmount ceFX = METHOD_FX.currencyExposure(FOREX, MULTICURVES);
    assertEquals("Currency exposure - non-deliverable forward", ceFX, ceNDF);
  }

  @Test
  /**
   * Checks that the NDF present value calculator is coherent with present value method.
   */
  public void currencyExposureMethodVsCalculator() {
    final MultipleCurrencyAmount ceMethod = METHOD_NDF.currencyExposure(NDF, MULTICURVES);
    final MultipleCurrencyAmount ceCalculator = NDF.accept(CEC, MULTICURVES);
    assertEquals("Currency exposure - non-deliverable forward", ceMethod, ceCalculator);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValue() {
    final MultipleCurrencyAmount ce = METHOD_NDF.currencyExposure(NDF, MULTICURVES);
    final MultipleCurrencyAmount pv = METHOD_NDF.presentValue(NDF, MULTICURVES);
    final double pvExpected = ce.getAmount(KRW) * FX_MATRIX.getFxRate(KRW, USD) + ce.getAmount(USD);
    assertEquals("Present value - non-deliverable forward", pvExpected, pv.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Checks that the NDF present value is coherent with the standard FX forward present value.
   */
  public void presentValueVsForex() {
    final MultipleCurrencyAmount pvNDF = METHOD_NDF.presentValue(NDF, MULTICURVES);
    final MultipleCurrencyAmount pvFX = METHOD_FX.presentValue(FOREX, MULTICURVES);
    assertEquals("Present value - non-deliverable forward", pvFX.getAmount(USD) + pvFX.getAmount(KRW) * FX_MATRIX.getFxRate(KRW, USD), pvNDF.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /**
   * Checks that the NDF present value calculator is coherent with present value method.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_NDF.presentValue(NDF, MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = NDF.accept(PVDC, MULTICURVES);
    assertEquals("Present value - non-deliverable forward", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Checks that the NDF forward rate is coherent with the standard FX forward present value.
   */
  public void forwardRateVsForex() {
    final double fwdNDF = METHOD_NDF.forwardForexRate(NDF, MULTICURVES);
    final double fwdFX = METHOD_FX.forwardForexRate(FOREX, MULTICURVES);
    assertEquals("Forward rate - non-deliverable forward", fwdNDF, fwdFX, TOLERANCE_PV);
  }

  //  @Test
  //  /**
  //   * Tests the forward Forex rate through the method and through the calculator.
  //   */
  //  public void forwardRateMethodVsCalculator() {
  //    final double fwdMethod = METHOD_NDF.forwardForexRate(NDF, MULTICURVES);
  //    final ForwardRateForexCalculator FWDC = ForwardRateForexCalculator.getInstance();
  //    final double fwdCalculator = NDF.accept(FWDC, MULTICURVES);
  //    assertEquals("Forex: forward rate", fwdMethod, fwdCalculator, 1.0E-10);
  //  }

  @Test
  /**
   * Tests the present value curve sensitivity using the Forex instrument curve sensitivity as reference.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyMulticurveSensitivity pvcsNDF = METHOD_NDF.presentValueCurveSensitivity(NDF, MULTICURVES).cleaned();
    final MultipleCurrencyMulticurveSensitivity pvcsFX = METHOD_FX.presentValueCurveSensitivity(FOREX, MULTICURVES).cleaned();
    final MultipleCurrencyMulticurveSensitivity pvcsFXConverted = pvcsFX.converted(USD, FX_MATRIX).cleaned();
    AssertSensitivityObjects.assertEquals("ForexNonDeliverableForwardDiscountingMethod: presentValueCurveSensitivity", pvcsFXConverted, pvcsNDF, TOLERANCE_PV);
  }

}
