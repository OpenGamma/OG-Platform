/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * 
 */
public class AnnuityPaymentFixedDefinition extends AnnuityDefinition<PaymentFixedDefinition> {

  /**
   * Constructor from a list of fixed payments.
   * @param payments The fixed coupons.
   */
  public AnnuityPaymentFixedDefinition(final PaymentFixedDefinition[] payments) {
    super(payments);
  }

  @Override
  public AnnuityPaymentFixed toDerivative(LocalDate date, String... yieldCurveNames) { // GenericAnnuity<CouponFixed>
    List<PaymentFixed> resultList = new ArrayList<PaymentFixed>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getNthPayment(loopcoupon).getPaymentDate().toLocalDate())) {
        resultList.add(getNthPayment(loopcoupon).toDerivative(date, yieldCurveNames));
      }
    }
    return new AnnuityPaymentFixed(resultList.toArray(new PaymentFixed[0]));
  }

}
