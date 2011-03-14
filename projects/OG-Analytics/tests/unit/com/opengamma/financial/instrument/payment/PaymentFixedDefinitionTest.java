/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class PaymentFixedDefinitionTest {

  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 1, 3);
  private static final double AMOUNT = 1000000; //1m

  private static final PaymentFixedDefinition SIMPLE_PAYMENT = new PaymentFixedDefinition(PAYMENT_DATE, AMOUNT);

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new PaymentFixedDefinition(null, AMOUNT);
  }

  @Test
  public void test() {
    assertEquals(SIMPLE_PAYMENT.getPaymentDate(), PAYMENT_DATE);
    assertEquals(SIMPLE_PAYMENT.getAmount(), AMOUNT, 1E-2);
  }

}
