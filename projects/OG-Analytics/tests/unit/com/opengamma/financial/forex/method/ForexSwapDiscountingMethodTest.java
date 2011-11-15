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
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueCurveSensitivityForexCalculator;
import com.opengamma.financial.forex.calculator.PresentValueForexCalculator;
import com.opengamma.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the method for Forex Swap transaction by discounting on each payment.
 */
public class ForexSwapDiscountingMethodTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime NEAR_DATE = DateUtils.getUTCDate(2011, 5, 26);
  private static final ZonedDateTime FAR_DATE = DateUtils.getUTCDate(2011, 6, 27); // 1m
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final double FORWARD_POINTS = -0.0007;
  private static final ForexSwapDefinition FX_SWAP_DEFINITION_FIN = new ForexSwapDefinition(CUR_1, CUR_2, NEAR_DATE, FAR_DATE, NOMINAL_1, FX_RATE, FORWARD_POINTS);

  private static final YieldCurveBundle CURVES = ForexTestsDataSets.createCurvesForex();
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[0]);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 20);
  private static final ForexSwap FX_SWAP = FX_SWAP_DEFINITION_FIN.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final ForexSwapDiscountingMethod METHOD = ForexSwapDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FX = ForexDiscountingMethod.getInstance();
  private static final PresentValueForexCalculator PVC_FX = PresentValueForexCalculator.getInstance();
  private static final CurrencyExposureForexCalculator CEC_FX = CurrencyExposureForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityForexCalculator PVCSC_FX = PresentValueCurveSensitivityForexCalculator.getInstance();

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD.presentValue(FX_SWAP, CURVES);
    final MultipleCurrencyAmount pvNear = METHOD_FX.presentValue(FX_SWAP.getNearLeg(), CURVES);
    final MultipleCurrencyAmount pvFar = METHOD_FX.presentValue(FX_SWAP.getFarLeg(), CURVES);
    assertEquals(pvNear.getAmount(CUR_1) + pvFar.getAmount(CUR_1), pv.getAmount(CUR_1));
    assertEquals(pvNear.getAmount(CUR_2) + pvFar.getAmount(CUR_2), pv.getAmount(CUR_2));
  }

  @Test
  /**
   * Test the present value through the method and through the calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD.presentValue(FX_SWAP, CURVES);
    final MultipleCurrencyAmount pvCalculator = PVC_FX.visit(FX_SWAP, CURVES);
    assertEquals("Forex present value: Method vs Calculator", pvMethod, pvCalculator);
    final ForexDerivative fxSwap = FX_SWAP;
    final MultipleCurrencyAmount pvMethod2 = METHOD.presentValue(fxSwap, CURVES);
    assertEquals("Forex present value: Method ForexSwap vs Method ForexDerivative", pvMethod, pvMethod2);
  }

  @Test
  /**
   * Tests the currency exposure computation.
   */
  public void currencyExposure() {
    final MultipleCurrencyAmount exposureMethod = METHOD.currencyExposure(FX_SWAP, CURVES);
    final MultipleCurrencyAmount pv = METHOD.presentValue(FX_SWAP, CURVES);
    assertEquals("Currency exposure", pv, exposureMethod);
    final MultipleCurrencyAmount exposureCalculator = CEC_FX.visit(FX_SWAP, CURVES);
    assertEquals("Currency exposure: Method vs Calculator", exposureMethod, exposureCalculator);
  }

  @Test
  /**
   * Test the present value sensitivity to interest rate.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyInterestRateCurveSensitivity pvs = METHOD.presentValueCurveSensitivity(FX_SWAP, CURVES);
    pvs.clean();
    MultipleCurrencyInterestRateCurveSensitivity pvsNear = METHOD_FX.presentValueCurveSensitivity(FX_SWAP.getNearLeg(), CURVES);
    final MultipleCurrencyInterestRateCurveSensitivity pvsFar = METHOD_FX.presentValueCurveSensitivity(FX_SWAP.getFarLeg(), CURVES);
    pvsNear = pvsNear.plus(pvsFar);
    pvsNear.clean();
    assertTrue("Forex swap present value curve sensitivity", pvs.equals(pvsNear));
  }

  @Test
  /**
   * Test the present value curve sensitivity through the method and through the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyInterestRateCurveSensitivity pvcsMethod = METHOD.presentValueCurveSensitivity(FX_SWAP, CURVES);
    final MultipleCurrencyInterestRateCurveSensitivity pvcsCalculator = PVCSC_FX.visit(FX_SWAP, CURVES);
    assertEquals("Forex swap present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }
}
