/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

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
    _amount = amount;
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

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public PaymentFixed toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException(this.getClass().getCanonicalName());
  }

  @Override
  public PaymentFixed toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date {} is after payment date {}", date, getPaymentDate());
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    return new PaymentFixed(getCurrency(), paymentTime, _amount);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitPaymentFixedDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitPaymentFixedDefinition(this);
  }

  @Override
  public double getReferenceAmount() {
    return _amount;
  }

}
