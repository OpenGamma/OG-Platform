/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests related to the construction of ForexDefinition and it conversion to derivative.
 */
public class ForexDefinitionTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 5, 24);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final ForexDefinition FX = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  private static final PaymentFixedDefinition PAY_1 = new PaymentFixedDefinition(CUR_1, PAYMENT_DATE, NOMINAL_1);
  private static final PaymentFixedDefinition PAY_2 = new PaymentFixedDefinition(CUR_2, PAYMENT_DATE, -NOMINAL_1 * FX_RATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency1() {
    new ForexDefinition(null, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency2() {
    new ForexDefinition(CUR_1, null, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    new ForexDefinition(CUR_1, CUR_2, null, NOMINAL_1, FX_RATE);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(CUR_1, FX.getCurrency1());
    assertEquals(CUR_2, FX.getCurrency2());
    assertEquals(PAYMENT_DATE, FX.getExchangeDate());
    assertEquals(NOMINAL_1, FX.getPaymentCurrency1().getAmount());
    assertEquals(-NOMINAL_1 * FX_RATE, FX.getPaymentCurrency2().getAmount());
    assertEquals(new PaymentFixedDefinition(CUR_1, PAYMENT_DATE, NOMINAL_1), FX.getPaymentCurrency1());
    assertEquals(new PaymentFixedDefinition(CUR_2, PAYMENT_DATE, -NOMINAL_1 * FX_RATE), FX.getPaymentCurrency2());
  }

  @Test
  /**
   * Tests the class equal and hashCode
   */
  public void equalHash() {
    ForexDefinition newFx = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
    assertTrue(FX.equals(newFx));
    assertTrue(FX.hashCode() == newFx.hashCode());
    ForexDefinition modifiedFx;
    modifiedFx = new ForexDefinition(CUR_2, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
    assertFalse(FX.equals(modifiedFx));
    modifiedFx = new ForexDefinition(CUR_1, CUR_1, PAYMENT_DATE, NOMINAL_1, FX_RATE);
    assertFalse(FX.equals(modifiedFx));
    modifiedFx = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1 + 10.0, FX_RATE);
    assertFalse(FX.equals(modifiedFx));
    modifiedFx = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, 1.1 * FX_RATE);
    assertFalse(FX.equals(modifiedFx));
  }

  @Test
  /**
   * Tests the constructor from payments.
   */
  public void constructorFromPayments() {
    ForexDefinition fxPayment = new ForexDefinition(PAY_1, PAY_2);
    assertEquals(FX, fxPayment);
  }

  @Test
  /**
   * Tests the conversion to derivative.
   */
  public void toDerivative() {
    String discountingEUR = "Discounting EUR";
    String discountingUSD = "Discounting USD";
    String[] curves = new String[] {discountingEUR, discountingUSD};
    ZonedDateTime referenceDate = DateUtil.getUTCDate(2011, 5, 20);
    Forex fxConverted = FX.toDerivative(referenceDate, curves);
    PaymentFixed pay1 = PAY_1.toDerivative(referenceDate, discountingEUR);
    PaymentFixed pay2 = PAY_2.toDerivative(referenceDate, discountingUSD);
    Forex fxComparison = new Forex(pay1, pay2);
    assertEquals(fxComparison, fxConverted);
  }

}
