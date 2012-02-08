/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.calculator.CurrencyExposureForexCalculator;
import com.opengamma.financial.forex.calculator.ForwardRateForexCalculator;
import com.opengamma.financial.forex.calculator.PresentValueForexCalculator;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the valuation of non-deliverable forward by discounting.
 */
public class ForexNonDeliverableForwardDiscountingMethodTest {

  private static final Currency KRW = Currency.of("KRW");
  private static final Currency USD = Currency.USD;
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2012, 5, 2);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2012, 5, 4);
  private static final double NOMINAL_USD = 100000000; // 1m
  private static final double FX_RATE = 1123.45;
  private static final ForexNonDeliverableForwardDefinition NDF_DEFINITION = new ForexNonDeliverableForwardDefinition(KRW, USD, NOMINAL_USD, FX_RATE, FIXING_DATE, PAYMENT_DATE);
  private static final ForexDefinition FOREX_DEFINITION = new ForexDefinition(USD, KRW, PAYMENT_DATE, NOMINAL_USD, FX_RATE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 11, 10);

  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final String[] CURVE_NAMES = TestsDataSetsForex.curveNames();
  private static final double USD_KRW = 1111.11;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD, KRW, USD_KRW);
  private static final YieldCurveWithFXBundle CURVESFX = new YieldCurveWithFXBundle(FX_MATRIX, CURVES);

  private static final ForexNonDeliverableForward NDF = NDF_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVE_NAMES[3], CURVE_NAMES[1]});
  private static final Forex FOREX = FOREX_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVE_NAMES[1], CURVE_NAMES[3]});

  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FX = ForexDiscountingMethod.getInstance();

  private static final PresentValueForexCalculator PVC_FX = PresentValueForexCalculator.getInstance();
  private static final CurrencyExposureForexCalculator CE_FX = CurrencyExposureForexCalculator.getInstance();

  @Test
  /**
   * Tests the currency exposure.
   */
  public void currencyExposure() {
    MultipleCurrencyAmount ce = METHOD_NDF.currencyExposure(NDF, CURVES);
    double df1 = CURVES.getCurve(CURVE_NAMES[3]).getDiscountFactor(NDF.getPaymentTime());
    double df2 = CURVES.getCurve(CURVE_NAMES[1]).getDiscountFactor(NDF.getPaymentTime());
    double ce1 = -NOMINAL_USD * df1 * FX_RATE;
    double ce2 = NOMINAL_USD * df2;
    assertEquals("Currency exposure - non-deliverable forward", ce1, ce.getAmount(KRW), 1.0E-2);
    assertEquals("Currency exposure - non-deliverable forward", ce2, ce.getAmount(USD), 1.0E-2);
  }

  @Test
  /**
   * Checks that the NDF currency exposure is the same as the standard FX forward currency exposure.
   */
  public void currencyExposureVsForex() {
    MultipleCurrencyAmount ceNDF = METHOD_NDF.currencyExposure(NDF, CURVES);
    MultipleCurrencyAmount ceFX = METHOD_FX.currencyExposure(FOREX, CURVES);
    assertEquals("Currency exposure - non-deliverable forward", ceFX, ceNDF);
  }

  @Test
  /**
   * Checks that the NDF present value calculator is coherent with present value method.
   */
  public void currencyExposureMethodVsCalculator() {
    MultipleCurrencyAmount ceMethod = METHOD_NDF.currencyExposure(NDF, CURVESFX);
    MultipleCurrencyAmount ceCalculator = CE_FX.visit(NDF, CURVESFX);
    assertEquals("Currency exposure - non-deliverable forward", ceMethod, ceCalculator);
  }

  @Test
  /**
   * Tests the present value.
   */
  public void presentValue() {
    MultipleCurrencyAmount ce = METHOD_NDF.currencyExposure(NDF, CURVES);
    MultipleCurrencyAmount pv = METHOD_NDF.presentValue(NDF, CURVESFX);
    double pvExpected = ce.getAmount(KRW) * FX_MATRIX.getFxRate(KRW, USD) + ce.getAmount(USD);
    assertEquals("Present value - non-deliverable forward", pvExpected, pv.getAmount(USD), 1.0E-2);
  }

  @Test
  /**
   * Checks that the NDF present value is coherent with the standard FX forward present value.
   */
  public void presentValueVsForex() {
    MultipleCurrencyAmount pvNDF = METHOD_NDF.presentValue(NDF, CURVESFX);
    MultipleCurrencyAmount pvFX = METHOD_FX.presentValue(FOREX, CURVES);
    assertEquals("Present value - non-deliverable forward", pvFX.getAmount(USD) + pvFX.getAmount(KRW) * FX_MATRIX.getFxRate(KRW, USD), pvNDF.getAmount(USD), 1.0E-2);
  }

  @Test
  /**
   * Checks that the NDF present value calculator is coherent with present value method.
   */
  public void presentValueMethodVsCalculator() {
    MultipleCurrencyAmount pvMethod = METHOD_NDF.presentValue(NDF, CURVESFX);
    MultipleCurrencyAmount pvCalculator = PVC_FX.visit(NDF, CURVESFX);
    assertEquals("Present value - non-deliverable forward", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Checks that the NDF forward rate is coherent with the standard FX forward present value.
   */
  public void forwardRateVsForex() {
    double fwdNDF = METHOD_NDF.forwardForexRate(NDF, CURVESFX);
    double fwdFX = METHOD_FX.forwardForexRate(FOREX, CURVESFX);
    assertEquals("Forward rate - non-deliverable forward", fwdNDF, fwdFX, 1.0E-2);
  }

  @Test
  /**
   * Tests the forward Forex rate through the method and through the calculator.
   */
  public void forwardRateMethodVsCalculator() {
    double fwdMethod = METHOD_NDF.forwardForexRate(NDF, CURVESFX);
    ForwardRateForexCalculator FWDC = ForwardRateForexCalculator.getInstance();
    double fwdCalculator = FWDC.visit(NDF, CURVESFX);
    assertEquals("Forex: forward rate", fwdMethod, fwdCalculator, 1.0E-10);
  }

  @Test
  /**
   * Tests the present value curve sensitivity using the Forex instrument curve sensitivity as reference.
   */
  public void presentValueCurveSensitivity() {
    double tolerance = 1.0E-2;
    MultipleCurrencyInterestRateCurveSensitivity pvcsNDF = METHOD_NDF.presentValueCurveSensitivity(NDF, CURVESFX);
    MultipleCurrencyInterestRateCurveSensitivity pvcsFX = METHOD_FX.presentValueCurveSensitivity(FOREX, CURVES);
    assertTrue("Present value curve sensitivity - non-deliverable forward",
        InterestRateCurveSensitivityUtils.compare(pvcsFX.getSensitivity(USD).getSensitivities().get(CURVE_NAMES[1]), pvcsNDF.getSensitivity(USD).getSensitivities().get(CURVE_NAMES[1]), tolerance));
    // The NDF sensitivity with respect to curve1 is in currency2; the FX sensitivity with respect to curve1 is in currency1; both sensitivity are in line with their respective PV.
    assertTrue(
        "Present value curve sensitivity - non-deliverable forward",
        InterestRateCurveSensitivityUtils.compare(
            InterestRateCurveSensitivityUtils.multiplySensitivity(pvcsFX.getSensitivity(KRW).getSensitivities().get(CURVE_NAMES[3]), FX_MATRIX.getFxRate(KRW, USD)), pvcsNDF.getSensitivity(USD)
                .getSensitivities().get(CURVE_NAMES[3]), tolerance));
  }

}
