/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * A wrapper class for a AnnuityDefinition containing PaymentFixedDefinition.
 */
public class AnnuityPaymentFixedDefinition extends AnnuityDefinition<PaymentFixedDefinition> {

  /**
   * Constructor from a list of fixed payments.
   * @param payments The fixed coupons.
   * @param calendar The calendar
   */
  public AnnuityPaymentFixedDefinition(final PaymentFixedDefinition[] payments, final Calendar calendar) {
    super(payments, calendar);
  }

  /**
   * Remove the payments paying on or before the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  @Override
  public AnnuityPaymentFixedDefinition trimBefore(final ZonedDateTime trimDate) {
    final List<PaymentFixedDefinition> list = new ArrayList<>();
    for (final PaymentFixedDefinition payment : getPayments()) {
      if (payment.getPaymentDate().isAfter(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityPaymentFixedDefinition(list.toArray(new PaymentFixedDefinition[list.size()]), getCalendar());
  }

  @Override
  public AnnuityPaymentFixed toDerivative(final ZonedDateTime date) {
    final List<PaymentFixed> resultList = new ArrayList<>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getNthPayment(loopcoupon).getPaymentDate())) {
        resultList.add(getNthPayment(loopcoupon).toDerivative(date));
      }
    }
    return new AnnuityPaymentFixed(resultList.toArray(new PaymentFixed[resultList.size()]));
  }

}
