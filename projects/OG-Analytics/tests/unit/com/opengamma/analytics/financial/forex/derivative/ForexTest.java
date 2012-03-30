/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;

/**
 * Tests related to the construction of Forex instruments.
 */
public class ForexTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final double PAYMENT_TIME = 1.0;
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;

  private static final String DISCOUNTING_CURVE_NAME_CUR_1 = "Discounting EUR";
  private static final String DISCOUNTING_CURVE_NAME_CUR_2 = "Discounting USD";

  private static final PaymentFixed PAY_1 = new PaymentFixed(CUR_1, PAYMENT_TIME, NOMINAL_1, DISCOUNTING_CURVE_NAME_CUR_1);
  private static final PaymentFixed PAY_2 = new PaymentFixed(CUR_2, PAYMENT_TIME, -NOMINAL_1 * FX_RATE, DISCOUNTING_CURVE_NAME_CUR_2);

  private static final Forex FX = new Forex(PAY_1, PAY_2);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayment1() {
    new Forex(null, PAY_2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayment2() {
    new Forex(PAY_1, null);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(PAY_1, FX.getPaymentCurrency1());
    assertEquals(PAY_2, FX.getPaymentCurrency2());
  }

  @Test
  /**
   * Tests the class equal and hashCode
   */
  public void equalHash() {
    assertTrue(FX.equals(FX));
    Forex newFx = new Forex(PAY_1, PAY_2);
    assertTrue(FX.equals(newFx));
    assertTrue(FX.hashCode() == newFx.hashCode());
    Forex modifiedFx;
    PaymentFixed payModified1 = new PaymentFixed(CUR_1, PAYMENT_TIME, NOMINAL_1 * 10.0, DISCOUNTING_CURVE_NAME_CUR_1);
    PaymentFixed payModified2 = new PaymentFixed(CUR_2, PAYMENT_TIME, -NOMINAL_1 * 10.0, DISCOUNTING_CURVE_NAME_CUR_2);
    modifiedFx = new Forex(payModified1, PAY_2);
    assertFalse(FX.equals(modifiedFx));
    modifiedFx = new Forex(PAY_1, payModified2);
    assertFalse(FX.equals(modifiedFx));
    assertFalse(FX.equals(CUR_1));
    assertFalse(FX.equals(null));
  }

}
