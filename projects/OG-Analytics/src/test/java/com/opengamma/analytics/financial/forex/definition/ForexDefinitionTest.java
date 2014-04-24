/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of ForexDefinition and it conversion to derivative.
 */
@Test(groups = TestGroup.UNIT)
public class ForexDefinitionTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 5, 24);
  private static final ZonedDateTime PAYMENT_DATE_OTHER = DateUtils.getUTCDate(2011, 5, 25);
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeFXRate() {
    new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, -FX_RATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongSign() {
    new ForexDefinition(PAY_1, new PaymentFixedDefinition(CUR_2, PAYMENT_DATE, NOMINAL_1 * FX_RATE));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void differentDates() {
    new ForexDefinition(PAY_1, new PaymentFixedDefinition(CUR_2, PAYMENT_DATE_OTHER, NOMINAL_1 * -FX_RATE));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongSign2() {
    ForexDefinition.fromAmounts(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, NOMINAL_1);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals("ForexDefinition getter", CUR_1, FX.getCurrency1());
    assertEquals(CUR_2, FX.getCurrency2());
    assertEquals(PAYMENT_DATE, FX.getExchangeDate());
    assertEquals(NOMINAL_1, FX.getPaymentCurrency1().getReferenceAmount());
    assertEquals(-NOMINAL_1 * FX_RATE, FX.getPaymentCurrency2().getReferenceAmount());
    assertEquals(new PaymentFixedDefinition(CUR_1, PAYMENT_DATE, NOMINAL_1), FX.getPaymentCurrency1());
    assertEquals(new PaymentFixedDefinition(CUR_2, PAYMENT_DATE, -NOMINAL_1 * FX_RATE), FX.getPaymentCurrency2());
  }

  @Test
  /**
   * Tests the class builder.
   */
  public void from() {
    final ForexDefinition fXfrom = ForexDefinition.fromAmounts(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, -FX_RATE * NOMINAL_1);
    assertEquals("ForexDefinition builder", FX, fXfrom);
  }

  @Test
  /**
   * Tests the class equal and hashCode
   */
  public void equalHash() {
    final ForexDefinition newFx = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
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
    assertFalse(FX.equals(CUR_1));
    assertFalse(FX.equals(null));
  }

  @Test
  /**
   * Tests the constructor from payments.
   */
  public void constructorFromPayments() {
    final ForexDefinition fxPayment = new ForexDefinition(PAY_1, PAY_2);
    assertEquals(FX, fxPayment);
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the conversion to derivative.
   */
  public void toDerivativeDeprecated() {
    final String discountingEUR = "Discounting EUR";
    final String discountingUSD = "Discounting USD";
    final String[] curves = new String[] {discountingEUR, discountingUSD};
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 5, 20);
    final Forex fxConverted = FX.toDerivative(referenceDate, curves);
    final PaymentFixed pay1 = PAY_1.toDerivative(referenceDate, discountingEUR);
    final PaymentFixed pay2 = PAY_2.toDerivative(referenceDate, discountingUSD);
    final Forex fxComparison = new Forex(pay1, pay2);
    assertEquals(fxComparison, fxConverted);
  }

  @Test
  /**
   * Tests the conversion to derivative.
   */
  public void toDerivative() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 5, 20);
    final Forex fxConverted = FX.toDerivative(referenceDate);
    final PaymentFixed pay1 = PAY_1.toDerivative(referenceDate);
    final PaymentFixed pay2 = PAY_2.toDerivative(referenceDate);
    final Forex fxComparison = new Forex(pay1, pay2);
    assertEquals(fxComparison, fxConverted);
  }

  @Test
  /**
   * Tests the to string method.
   */
  public void testToString() {
    final String fxToString = FX.toString();
    final String expected = "Forex transaction:\nCurrency 1 payment: \nPayment Currency = EUR, Date = 2011-05-24T00:00ZAmount = 1.0E8"
        + "\nCurrency 2 payment: \nPayment Currency = USD, Date = 2011-05-24T00:00ZAmount = -1.4177E8";
    assertEquals(expected, fxToString);
  }

}
