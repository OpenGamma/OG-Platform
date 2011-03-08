/**
 * Copyright (C) 2011 - present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * 
 */
public class PaymentDefinition {

  private final ZonedDateTime _paymentDate;

  // TODO: add currency

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
