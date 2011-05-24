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

import com.opengamma.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtil;

/**
 * Test related to the method for Forex Swap transaction by discounting on each payment.
 */
public class ForexSwapDiscountingMethodTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime NEAR_DATE = DateUtil.getUTCDate(2011, 5, 26);
  private static final ZonedDateTime FAR_DATE = DateUtil.getUTCDate(2011, 6, 27); // 1m
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final double FORWARD_POINTS = -0.0007;
  private static final ForexSwapDefinition FX_SWAP_DEFINITION_FIN = new ForexSwapDefinition(CUR_1, CUR_2, NEAR_DATE, FAR_DATE, NOMINAL_1, FX_RATE, FORWARD_POINTS);

  private static final YieldCurveBundle CURVES = ForexTestsDataSets.createCurvesForex();
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[0]);
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2011, 5, 20);
  private static final ForexSwap FX_SWAP = FX_SWAP_DEFINITION_FIN.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final ForexSwapDiscountingMethod METHOD = new ForexSwapDiscountingMethod();
  private static final ForexDiscountingMethod METHOD_FX = new ForexDiscountingMethod();
  private static final com.opengamma.financial.forex.calculator.PresentValueCalculator PVC_FX = com.opengamma.financial.forex.calculator.PresentValueCalculator.getInstance();

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValue() {
    MultipleCurrencyAmount pv = METHOD.presentValue(FX_SWAP, CURVES);
    MultipleCurrencyAmount pvNear = METHOD_FX.presentValue(FX_SWAP.getNearLeg(), CURVES);
    MultipleCurrencyAmount pvFar = METHOD_FX.presentValue(FX_SWAP.getFarLeg(), CURVES);
    assertEquals(pvNear.getAmountFor(CUR_1) + pvFar.getAmountFor(CUR_1), pv.getAmountFor(CUR_1));
    assertEquals(pvNear.getAmountFor(CUR_2) + pvFar.getAmountFor(CUR_2), pv.getAmountFor(CUR_2));
  }

  @Test
  /**
   * Test the present value through the method and through the calculator.
   */
  public void presentValueMethodVsCalculator() {
    MultipleCurrencyAmount pvMethod = METHOD.presentValue(FX_SWAP, CURVES);
    MultipleCurrencyAmount pvCalculator = PVC_FX.visit(FX_SWAP, CURVES);
    assertEquals("Forex present value: Method vs Calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Test the present value sensitivity to interest rate.
   */
  public void presentValueCurveSensitivity() {
    PresentValueSensitivity pvs = METHOD.presentValueCurveSensitivity(FX_SWAP, CURVES);
    pvs.clean();
    PresentValueSensitivity pvsNear = METHOD_FX.presentValueCurveSensitivity(FX_SWAP.getNearLeg(), CURVES);
    PresentValueSensitivity pvsFar = METHOD_FX.presentValueCurveSensitivity(FX_SWAP.getFarLeg(), CURVES);
    pvsNear = pvsNear.add(pvsFar);
    pvsNear.clean();
    assertTrue(pvs.equals(pvsNear));
  }
}
