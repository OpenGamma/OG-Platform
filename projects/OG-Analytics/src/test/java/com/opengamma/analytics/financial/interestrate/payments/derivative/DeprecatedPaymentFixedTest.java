/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedPaymentFixedTest {

  private static final double PAYMENT_TIME = 0.67;
  private static final double AMOUNT = 45.6;
  private static final String CURVE_NAME = "vfsmngsdjkflsadfk";
  private static final Currency CUR = Currency.EUR;
  private static final PaymentFixed PAYMENT = new PaymentFixed(CUR, PAYMENT_TIME, AMOUNT, CURVE_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new PaymentFixed(CUR, -1, AMOUNT, CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveName() {
    new PaymentFixed(CUR, PAYMENT_TIME, AMOUNT, null);
  }

  @Test
  public void testHashCodeAndEquals() {
    PaymentFixed other = new PaymentFixed(CUR, PAYMENT_TIME, AMOUNT, CURVE_NAME);
    assertEquals(other, PAYMENT);
    assertEquals(other.hashCode(), PAYMENT.hashCode());
    other = new PaymentFixed(CUR, PAYMENT_TIME - 0.01, AMOUNT, CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new PaymentFixed(CUR, PAYMENT_TIME, AMOUNT + 0.01, CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new PaymentFixed(CUR, PAYMENT_TIME, AMOUNT, "hklhkldf");
    assertFalse(other.equals(PAYMENT));
  }

  @Test
  public void testGetters() {
    assertEquals(PAYMENT.getPaymentTime(), PAYMENT_TIME, 0);
    assertEquals(PAYMENT.getAmount(), AMOUNT, 0);
    assertEquals(PAYMENT.getFundingCurveName(), CURVE_NAME);
  }

}
