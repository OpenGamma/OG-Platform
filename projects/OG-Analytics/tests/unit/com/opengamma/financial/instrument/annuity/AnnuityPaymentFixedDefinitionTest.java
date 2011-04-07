/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

public class AnnuityPaymentFixedDefinitionTest {
  private static final Currency CUR = Currency.USD;
  private static final ZonedDateTime[] PAYMENT_DATE = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 7, 13), DateUtil.getUTCDate(2011, 10, 13), DateUtil.getUTCDate(2012, 1, 13)};
  private static final double[] PAYMENT_AMOUNT = new double[] {100.0, 150.0, 1.0};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayment() {
    new AnnuityPaymentFixedDefinition(null);
  }

  @Test
  public void testGetter() {
    PaymentFixedDefinition[] payment = new PaymentFixedDefinition[PAYMENT_DATE.length];
    for (int looppay = 0; looppay < PAYMENT_DATE.length; looppay++) {
      payment[looppay] = new PaymentFixedDefinition(CUR, PAYMENT_DATE[looppay], PAYMENT_AMOUNT[looppay]);
    }
    AnnuityPaymentFixedDefinition annuity = new AnnuityPaymentFixedDefinition(payment);
    assertEquals(CUR, annuity.getCurrency());
  }
}
