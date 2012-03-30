/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

public class AnnuityPaymentFixedDefinitionTest {
  private static final Currency CUR = Currency.USD;
  private static final ZonedDateTime[] PAYMENT_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 7, 13), DateUtils.getUTCDate(2011, 10, 13), DateUtils.getUTCDate(2012, 1, 13)};
  private static final double[] PAYMENT_AMOUNT = new double[] {100.0, 150.0, 1.0};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayment() {
    new AnnuityPaymentFixedDefinition(null);
  }

  @Test
  public void testGetter() {
    final PaymentFixedDefinition[] payment = new PaymentFixedDefinition[PAYMENT_DATE.length];
    for (int looppay = 0; looppay < PAYMENT_DATE.length; looppay++) {
      payment[looppay] = new PaymentFixedDefinition(CUR, PAYMENT_DATE[looppay], PAYMENT_AMOUNT[looppay]);
    }
    final AnnuityPaymentFixedDefinition annuity = new AnnuityPaymentFixedDefinition(payment);
    assertEquals(CUR, annuity.getCurrency());
  }

  @Test
  public void testConverter() {
    final PaymentFixedDefinition[] annuityDefinitions = new PaymentFixedDefinition[PAYMENT_DATE.length];
    PaymentFixed[] payment = new PaymentFixed[PAYMENT_DATE.length];
    ZonedDateTime date = DateUtils.getUTCDate(2011, 6, 19);
    final String name = "A";
    for (int looppay = 0; looppay < PAYMENT_DATE.length; looppay++) {
      annuityDefinitions[looppay] = new PaymentFixedDefinition(CUR, PAYMENT_DATE[looppay], PAYMENT_AMOUNT[looppay]);
      payment[looppay] = annuityDefinitions[looppay].toDerivative(date, name);
    }
    final AnnuityPaymentFixedDefinition definition = new AnnuityPaymentFixedDefinition(annuityDefinitions);
    AnnuityPaymentFixed annuity = new AnnuityPaymentFixed(payment);
    assertEquals(annuity, definition.toDerivative(date, name));
    date = DateUtils.getUTCDate(2011, 8, 19);
    payment = new PaymentFixed[PAYMENT_DATE.length - 1];
    for (int looppay = 0; looppay < PAYMENT_DATE.length; looppay++) {
      annuityDefinitions[looppay] = new PaymentFixedDefinition(CUR, PAYMENT_DATE[looppay], PAYMENT_AMOUNT[looppay]);
      if (looppay > 0) {
        payment[looppay - 1] = annuityDefinitions[looppay].toDerivative(date, name);
      }
    }
    annuity = new AnnuityPaymentFixed(payment);
    assertEquals(annuity, definition.toDerivative(date, name));
  }
}
