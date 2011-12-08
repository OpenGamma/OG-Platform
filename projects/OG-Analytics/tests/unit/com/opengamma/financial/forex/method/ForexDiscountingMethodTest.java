/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.calculator.CurrencyExposureForexCalculator;
import com.opengamma.financial.forex.calculator.ForwardRateForexCalculator;
import com.opengamma.financial.forex.calculator.PresentValueCurveSensitivityForexCalculator;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test related to the method for Forex transaction by discounting on each payment.
 */
public class ForexDiscountingMethodTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 5, 24);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  private static final YieldCurveBundle CURVES = ForexTestsDataSets.createCurvesForex();
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[0]);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 20);
  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final PaymentFixedDefinition PAY_DEFINITION_1 = new PaymentFixedDefinition(CUR_1, PAYMENT_DATE, NOMINAL_1);
  private static final PaymentFixed PAY_1 = PAY_DEFINITION_1.toDerivative(REFERENCE_DATE, CURVES_NAME[0]);
  private static final PaymentFixedDefinition PAY_DEFINITION_2 = new PaymentFixedDefinition(CUR_2, PAYMENT_DATE, -NOMINAL_1 * FX_RATE);
  private static final PaymentFixed PAY_2 = PAY_DEFINITION_2.toDerivative(REFERENCE_DATE, CURVES_NAME[1]);

  private static final ForexDiscountingMethod METHOD = ForexDiscountingMethod.getInstance();
  private static final com.opengamma.financial.interestrate.PresentValueCalculator PVC_IR = com.opengamma.financial.interestrate.PresentValueCalculator.getInstance();
  private static final com.opengamma.financial.forex.calculator.PresentValueForexCalculator PVC_FX = com.opengamma.financial.forex.calculator.PresentValueForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PVSC = PresentValueCurveSensitivityCalculator.getInstance();
  private static final CurrencyExposureForexCalculator CEC_FX = CurrencyExposureForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityForexCalculator PVCSC_FX = PresentValueCurveSensitivityForexCalculator.getInstance();

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(FX, CURVES);
    final CurrencyAmount ca1 = CurrencyAmount.of(CUR_1, PVC_IR.visit(PAY_1, CURVES));
    final CurrencyAmount ca2 = CurrencyAmount.of(CUR_2, PVC_IR.visit(PAY_2, CURVES));
    assertEquals(ca1, pv.getCurrencyAmount(CUR_1));
    assertEquals(ca2, pv.getCurrencyAmount(CUR_2));
  }

  @Test
  /**
   * Test the present value through the method and through the calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.presentValue(FX, CURVES);
    final MultipleCurrencyAmount pvCalculator = PVC_FX.visit(FX, CURVES);
    assertEquals("Forex present value: Method vs Calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Test the present value sensitivity to interest rate.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyInterestRateCurveSensitivity pvcs = METHOD.presentValueCurveSensitivity(FX, CURVES);
    final Map<String, List<DoublesPair>> pvs1 = PVSC.visit(PAY_1, CURVES);
    final Map<String, List<DoublesPair>> pvs2 = PVSC.visit(PAY_2, CURVES);
    assertEquals(pvs1.get(CURVES_NAME[0]), pvcs.getSensitivity(CUR_1).getSensitivities().get(CURVES_NAME[0]));
    assertEquals(pvs2.get(CURVES_NAME[1]), pvcs.getSensitivity(CUR_2).getSensitivities().get(CURVES_NAME[1]));
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyInterestRateCurveSensitivity pvcsMethod = METHOD.presentValueCurveSensitivity(FX, CURVES);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsCalculator = PVCSC_FX.visit(FX, CURVES);
    assertEquals("Forex present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

  @Test
  /**
   * Test the present value of EUR/USD is the same as an USD/EUR.
   */
  public void presentValueReverse() {
    final ForexDefinition fxReverseDefinition = new ForexDefinition(CUR_2, CUR_1, PAYMENT_DATE, -NOMINAL_1 * FX_RATE, 1.0 / FX_RATE);
    final Forex fxReverse = fxReverseDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[1], CURVES_NAME[0]});
    final MultipleCurrencyAmount pv = METHOD.presentValue(FX, CURVES);
    final MultipleCurrencyAmount pvReverse = METHOD.presentValue(fxReverse, CURVES);
    assertEquals("Forex present value: Reverse description", pv.getAmount(CUR_1), pvReverse.getAmount(CUR_1), 1.0E-2);
    assertEquals("Forex present value: Reverse description", pv.getAmount(CUR_2), pvReverse.getAmount(CUR_2), 1.0E-2);
  }

  @Test
  /**
   * Tests the currency exposure computation.
   */
  public void currencyExposure() {
    final MultipleCurrencyAmount exposureMethod = METHOD.currencyExposure(FX, CURVES);
    final MultipleCurrencyAmount pv = METHOD.presentValue(FX, CURVES);
    assertEquals("Currency exposure", pv, exposureMethod);
    final MultipleCurrencyAmount exposureCalculator = CEC_FX.visit(FX, CURVES);
    assertEquals("Currency exposure: Method vs Calculator", exposureMethod, exposureCalculator);
  }

  @Test
  /**
   * Tests the forward Forex rate computation.
   */
  public void forwardRate() {
    double fxToday = 1.4123;
    FXMatrix fxMatrix = new FXMatrix(CUR_1, CUR_2, fxToday);
    YieldCurveWithFXBundle curvesFx = new YieldCurveWithFXBundle(fxMatrix, CURVES);
    double fwd = METHOD.forwardForexRate(FX, curvesFx);
    double dfDomestic = CURVES.getCurve(CURVES_NAME[1]).getDiscountFactor(FX.getPaymentTime());
    double dfForeign = CURVES.getCurve(CURVES_NAME[0]).getDiscountFactor(FX.getPaymentTime());
    double fwdExpected = fxToday * dfForeign / dfDomestic;
    assertEquals("Forex: forward rate", fwdExpected, fwd, 1.0E-10);
  }

  @Test
  /**
   * Tests the forward Forex rate through the method and through the calculator.
   */
  public void forwardRateMethodVsCalculator() {
    double fxToday = 1.4123;
    FXMatrix fxMatrix = new FXMatrix(CUR_1, CUR_2, fxToday);
    YieldCurveWithFXBundle curvesFx = new YieldCurveWithFXBundle(fxMatrix, CURVES);
    double fwdMethod = METHOD.forwardForexRate(FX, curvesFx);
    ForwardRateForexCalculator FWDC = ForwardRateForexCalculator.getInstance();
    double fwdCalculator = FWDC.visit(FX, curvesFx);
    assertEquals("Forex: forward rate", fwdMethod, fwdCalculator, 1.0E-10);
  }

}
