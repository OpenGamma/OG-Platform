/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class PaymentFixedDefinitionTest {

  private static final Currency CUR = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double AMOUNT = 1000000; //1m
  private static final PaymentFixedDefinition FIXED_PAYMENT = new PaymentFixedDefinition(CUR, PAYMENT_DATE, AMOUNT);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27); //For conversion to derivative

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new PaymentFixedDefinition(null, PAYMENT_DATE, AMOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new PaymentFixedDefinition(CUR, null, AMOUNT);
  }

  @Test
  public void test() {
    assertEquals(FIXED_PAYMENT.getPaymentDate(), PAYMENT_DATE);
    assertEquals(FIXED_PAYMENT.getAmount(), AMOUNT, 1E-2);
  }

  //TODO: test equal/hashCode
  @Test
  public void testEqualHashCode() {
    final PaymentFixedDefinition comparedPayment = new PaymentFixedDefinition(CUR, PAYMENT_DATE, AMOUNT);
    assertEquals(FIXED_PAYMENT, comparedPayment);
    assertEquals(FIXED_PAYMENT.hashCode(), comparedPayment.hashCode());
    PaymentFixedDefinition modifiedPayment = new PaymentFixedDefinition(CUR, PAYMENT_DATE, AMOUNT + 1.0);
    assertFalse(FIXED_PAYMENT.equals(modifiedPayment));
    final ZonedDateTime modifiedDate = PAYMENT_DATE.minusDays(1);
    modifiedPayment = new PaymentFixedDefinition(CUR, modifiedDate, AMOUNT);
    assertFalse(FIXED_PAYMENT.equals(modifiedPayment));
    final Currency modifiedCurrency = Currency.AUD;
    modifiedPayment = new PaymentFixedDefinition(modifiedCurrency, PAYMENT_DATE, AMOUNT);
    assertFalse(FIXED_PAYMENT.equals(modifiedPayment));
  }

  @Test
  public void testToDerivative() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    //    double paymentTime = 7.0 / 365.0; //TODO: precision?
    final String fundingCurve = "Funding";
    final PaymentFixed paymentFixed = new PaymentFixed(CUR, paymentTime, AMOUNT, fundingCurve);
    final PaymentFixed convertedDefinition = FIXED_PAYMENT.toDerivative(REFERENCE_DATE, fundingCurve);
    assertEquals(paymentFixed, convertedDefinition);
  }

}
