/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AnnuityPaymentFixedDefinitionTest {
  private static final Calendar CALENDAR = new NoHolidayCalendar();
  private static final Currency CUR = Currency.EUR;
  private static final ZonedDateTime[] PAYMENT_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 7, 13), DateUtils.getUTCDate(2011, 10, 13), DateUtils.getUTCDate(2012, 1, 13)};
  private static final double[] PAYMENT_AMOUNT = new double[] {100.0, 150.0, 1.0};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPayment() {
    new AnnuityPaymentFixedDefinition(null, null);
  }

  @Test
  public void testGetter() {
    final PaymentFixedDefinition[] payment = new PaymentFixedDefinition[PAYMENT_DATE.length];
    for (int looppay = 0; looppay < PAYMENT_DATE.length; looppay++) {
      payment[looppay] = new PaymentFixedDefinition(CUR, PAYMENT_DATE[looppay], PAYMENT_AMOUNT[looppay]);
    }
    final AnnuityPaymentFixedDefinition annuity = new AnnuityPaymentFixedDefinition(payment, CALENDAR);
    assertEquals(CUR, annuity.getCurrency());
  }

  @Test
  public void testConverter() {
    final PaymentFixedDefinition[] annuityDefinitions = new PaymentFixedDefinition[PAYMENT_DATE.length];
    PaymentFixed[] payment = new PaymentFixed[PAYMENT_DATE.length];
    ZonedDateTime date = DateUtils.getUTCDate(2011, 6, 19);
    for (int looppay = 0; looppay < PAYMENT_DATE.length; looppay++) {
      annuityDefinitions[looppay] = new PaymentFixedDefinition(CUR, PAYMENT_DATE[looppay], PAYMENT_AMOUNT[looppay]);
      payment[looppay] = annuityDefinitions[looppay].toDerivative(date);
    }
    final AnnuityPaymentFixedDefinition definition = new AnnuityPaymentFixedDefinition(annuityDefinitions, CALENDAR);
    AnnuityPaymentFixed annuity = new AnnuityPaymentFixed(payment);
    assertEquals(annuity, definition.toDerivative(date));
    date = DateUtils.getUTCDate(2011, 8, 19);
    payment = new PaymentFixed[PAYMENT_DATE.length - 1];
    for (int looppay = 0; looppay < PAYMENT_DATE.length; looppay++) {
      annuityDefinitions[looppay] = new PaymentFixedDefinition(CUR, PAYMENT_DATE[looppay], PAYMENT_AMOUNT[looppay]);
      if (looppay > 0) {
        payment[looppay - 1] = annuityDefinitions[looppay].toDerivative(date);
      }
    }
    annuity = new AnnuityPaymentFixed(payment);
    assertEquals(annuity, definition.toDerivative(date));
  }
}
