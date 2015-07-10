/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Utilities related to the end-to-end tests.
 */
public class E2EUtils {

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-8;
  
  /**
   * Test the present value versus a hard-coded number.
   * @param ins The instrument to test.
   * @param multicurve The multi-curve provider.
   * @param ccy The currency of the expected PV.
   * @param expectedPv The expected PV amount.
   * @param msg The assert message.
   */
  public static void presentValueTest(InstrumentDerivative ins, MulticurveProviderDiscount multicurve, Currency ccy, 
      double expectedPv, String msg) {
    MultipleCurrencyAmount pvComputed = ins.accept(PVDC, multicurve);
    assertEquals(msg, expectedPv, multicurve.getFxRates().convert(pvComputed, ccy).getAmount(), TOLERANCE_PV);
  }
  
  /**
   * Test the present value versus a hard-coded number.
   * @param ins The instrument to test.
   * @param multicurve The multi-curve provider.
   * @param ccy The currency of the expected PV.
   * @param expectedPv The expected PV amount.
   * @param msg The assert message.
   */
  public static void parRateTest(InstrumentDerivative ins, MulticurveProviderDiscount multicurve, 
      double expectedPr, String msg) {
    double prComputed = ins.accept(PRDC, multicurve);
    assertEquals(msg, expectedPr, prComputed, TOLERANCE_RATE);
  }

}
