/**
 * Copyright (C) 2011 - present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * Class describing a generic payment. 
 */
public abstract class PaymentDefinition implements FixedIncomeInstrumentDefinition<Payment> {

  // TODO: add currency
  private final ZonedDateTime _paymentDate;

  /**
   * Constructor from payment date.
   * @param paymentDate The payment date.
   */
  public PaymentDefinition(ZonedDateTime paymentDate) {
    Validate.notNull(paymentDate, "payment date");
    this._paymentDate = paymentDate;
  }

  /**
   * Gets the paymentDate field.
   * @return the paymentDate
   */
  public ZonedDateTime getPaymentDate() {
    return _paymentDate;
  }

  @Override
  public String toString() {
    return "\nPayment Date = " + _paymentDate.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _paymentDate.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PaymentDefinition other = (PaymentDefinition) obj;
    if (!ObjectUtils.equals(_paymentDate, other._paymentDate)) {
      return false;
    }
    return true;
  }

}
