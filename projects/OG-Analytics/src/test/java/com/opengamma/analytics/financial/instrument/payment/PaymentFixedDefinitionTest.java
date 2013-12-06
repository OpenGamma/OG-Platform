/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PaymentFixedDefinitionTest {

  private static final Currency CUR = Currency.EUR;
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
    assertEquals(FIXED_PAYMENT.getReferenceAmount(), AMOUNT, 1E-2);
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

  @SuppressWarnings("deprecation")
  @Test
  public void testToDerivativeDeprecated() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE.toLocalDate(), LocalTime.MIDNIGHT), ZoneOffset.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    final String fundingCurve = "Funding";
    final PaymentFixed paymentFixed = new PaymentFixed(CUR, paymentTime, AMOUNT, fundingCurve);
    final PaymentFixed convertedDefinition = FIXED_PAYMENT.toDerivative(REFERENCE_DATE, fundingCurve);
    assertEquals(paymentFixed, convertedDefinition);
  }

  @Test
  public void testToDerivative() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE.toLocalDate(), LocalTime.MIDNIGHT), ZoneOffset.UTC);
    final double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    final PaymentFixed paymentFixed = new PaymentFixed(CUR, paymentTime, AMOUNT);
    final PaymentFixed convertedDefinition = FIXED_PAYMENT.toDerivative(REFERENCE_DATE);
    assertEquals(paymentFixed, convertedDefinition);
  }
}
