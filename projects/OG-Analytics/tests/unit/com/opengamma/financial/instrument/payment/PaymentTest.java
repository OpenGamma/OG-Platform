/**
 * Copyright (C) 2011 - present by OpenGamma Inc.
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
public class PaymentTest {

  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 1, 3);

  private static final PaymentDefinition PAYMENT = new PaymentDefinition(PAYMENT_DATE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new PaymentDefinition(null);
  }

  @Test
  public void test() {
    assertEquals(PAYMENT.getPaymentDate(), PAYMENT_DATE);
  }
}
