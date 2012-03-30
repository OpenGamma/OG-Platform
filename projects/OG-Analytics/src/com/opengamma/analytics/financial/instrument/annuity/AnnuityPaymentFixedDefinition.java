/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;

/**
 * A wrapper class for a AnnuityDefinition containing PaymentFixedDefinition.
 */
public class AnnuityPaymentFixedDefinition extends AnnuityDefinition<PaymentFixedDefinition> {

  /**
   * Constructor from a list of fixed payments.
   * @param payments The fixed coupons.
   */
  public AnnuityPaymentFixedDefinition(final PaymentFixedDefinition[] payments) {
    super(payments);
  }

  /**
   * Remove the payments paying on or before the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  @Override
  public AnnuityPaymentFixedDefinition trimBefore(ZonedDateTime trimDate) {
    List<PaymentFixedDefinition> list = new ArrayList<PaymentFixedDefinition>();
    for (PaymentFixedDefinition payment : getPayments()) {
      if (payment.getPaymentDate().isAfter(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityPaymentFixedDefinition(list.toArray(new PaymentFixedDefinition[0]));
  }

  @Override
  public AnnuityPaymentFixed toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final List<PaymentFixed> resultList = new ArrayList<PaymentFixed>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getNthPayment(loopcoupon).getPaymentDate())) {
        resultList.add(getNthPayment(loopcoupon).toDerivative(date, yieldCurveNames));
      }
    }
    return new AnnuityPaymentFixed(resultList.toArray(new PaymentFixed[0]));
  }

}
