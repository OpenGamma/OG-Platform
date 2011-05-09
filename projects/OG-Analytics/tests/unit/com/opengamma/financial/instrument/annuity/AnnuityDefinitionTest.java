/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class AnnuityDefinitionTest {
  private static final PaymentFixedDefinition[] PAYMENTS;
  private static final Currency CCY = Currency.AUD;
  private static final AnnuityDefinition<PaymentFixedDefinition> DEFINITION;

  static {
    final int n = 10;
    PAYMENTS = new PaymentFixedDefinition[n];
    ZonedDateTime date = DateUtil.getUTCDate(2011, 1, 1);
    for (int i = 0; i < n; i++) {
      PAYMENTS[i] = new PaymentFixedDefinition(CCY, date, 1000);
      date = date.plusDays(1);
    }
    DEFINITION = new AnnuityDefinition<PaymentFixedDefinition>(PAYMENTS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayments() {
    new AnnuityDefinition<PaymentFixedDefinition>((PaymentFixedDefinition[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithNullPayment() {
    final PaymentFixedDefinition[] payments = Arrays.copyOf(PAYMENTS, PAYMENTS.length);
    payments[0] = null;
    new AnnuityDefinition<PaymentFixedDefinition>(payments);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyPayments() {
    new AnnuityDefinition<PaymentFixedDefinition>(new PaymentFixedDefinition[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentCurrencyPayments() {
    final PaymentFixedDefinition[] payments = Arrays.copyOf(PAYMENTS, PAYMENTS.length);
    payments[0] = new PaymentFixedDefinition(Currency.CAD, DateUtil.getUTCDate(2011, 1, 1), 1000);
    new AnnuityDefinition<PaymentFixedDefinition>(payments);
  }

  @Test
  public void testObject() {
    assertEquals(PAYMENTS, DEFINITION.getPayments());
    for (int i = 0; i < PAYMENTS.length; i++) {
      assertEquals(PAYMENTS[i], DEFINITION.getNthPayment(i));
    }
    assertEquals(CCY, DEFINITION.getCurrency());
    assertEquals(PAYMENTS.length, DEFINITION.getNumberOfPayments());
    AnnuityDefinition<PaymentFixedDefinition> other = new AnnuityDefinition<PaymentFixedDefinition>(PAYMENTS);
    assertEquals(DEFINITION, other);
    assertEquals(DEFINITION.hashCode(), other.hashCode());
    final PaymentFixedDefinition[] payments = Arrays.copyOf(PAYMENTS, PAYMENTS.length);
    payments[0] = new PaymentFixedDefinition(CCY, DateUtil.getUTCDate(2011, 1, 1), 10000);
    other = new AnnuityDefinition<PaymentFixedDefinition>(payments);
    assertFalse(other.equals(DEFINITION));
  }

  @Test
  public void testPayer() {
    assertFalse(DEFINITION.isPayer());
    final PaymentFixedDefinition[] payments = new PaymentFixedDefinition[PAYMENTS.length];
    for (int i = 0; i < PAYMENTS.length; i++) {
      payments[i] = new PaymentFixedDefinition(CCY, DateUtil.getUTCDate(2011, 1, 1), -1000);
    }
    assertTrue(new AnnuityDefinition<PaymentFixedDefinition>(payments).isPayer());
  }
}
