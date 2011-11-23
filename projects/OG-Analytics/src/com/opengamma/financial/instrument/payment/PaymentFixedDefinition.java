/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;

/**
 * Class describing a simple payment of a given amount on a given date.
 */
public class PaymentFixedDefinition extends PaymentDefinition {

  /**
   * The amount of the simple payment.
   */
  private final double _amount;

  /**
   * Constructor from payment details.
   * @param currency The payment currency.
   * @param paymentDate The payment date.
   * @param amount The payment amount.
   */
  public PaymentFixedDefinition(final Currency currency, final ZonedDateTime paymentDate, final double amount) {
    super(currency, paymentDate);
    this._amount = amount;
  }

  /**
   * Gets the amount field.
   * @return the amount
   */
  public double getAmount() {
    return _amount;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PaymentFixedDefinition other = (PaymentFixedDefinition) obj;
    if (Double.doubleToLongBits(_amount) != Double.doubleToLongBits(other._amount)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return super.toString() + "Amount = " + _amount;
  }

  @Override
  public PaymentFixed toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final String fundingCurveName = yieldCurveNames[0];
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    return new PaymentFixed(getCurrency(), paymentTime, _amount, fundingCurveName);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitPaymentFixed(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitPaymentFixed(this);
  }

  @Override
  public double getReferenceAmount() {
    return _amount;
  }

}
