/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;

/**
 * Class describing a foreign exchange transaction (spot or forward).
 */
public class ForexDefinition implements InstrumentDefinition<InstrumentDerivative> {

  /**
   * The payment in the first currency.
   */
  private final PaymentFixedDefinition _paymentCurrency1;
  /**
   * The payment in the second currency.
   */
  private final PaymentFixedDefinition _paymentCurrency2;

  /**
   * Constructor from the financial details.
   * @param currency1 The first currency.
   * @param currency2 The second currency.
   * @param exchangeDate The date of the exchange.
   * @param amountCurrency1 The amount in the first currency.
   * @param fxRate The forex rate, understood as 1.0 Currency1 is exchanged for fxRate Currency2. The amount in Currency2 will be -amountCurrency1*fxRate.
   */
  public ForexDefinition(final Currency currency1, final Currency currency2, final ZonedDateTime exchangeDate, final double amountCurrency1, final double fxRate) {
    Validate.notNull(currency1, "Currency 1");
    Validate.notNull(currency2, "Currency 2");
    Validate.notNull(exchangeDate, "Exchange date");
    Validate.isTrue(fxRate > 0, "FX rate must be positive");
    _paymentCurrency1 = new PaymentFixedDefinition(currency1, exchangeDate, amountCurrency1);
    _paymentCurrency2 = new PaymentFixedDefinition(currency2, exchangeDate, -amountCurrency1 * fxRate);
  }

  /**
   * Constructor from two fixed payments. The payments should take place on the same date. The signs of the amounts should be opposite.
   * @param paymentCurrency1 The first currency payment.
   * @param paymentCurrency2 The second currency payment.
   */
  public ForexDefinition(final PaymentFixedDefinition paymentCurrency1, final PaymentFixedDefinition paymentCurrency2) {
    Validate.notNull(paymentCurrency1, "Payment 1");
    Validate.notNull(paymentCurrency2, "Payment 2");
    Validate.isTrue(paymentCurrency1.getPaymentDate().equals(paymentCurrency2.getPaymentDate()), "Payments on different date");
    Validate.isTrue((paymentCurrency1.getAmount() * paymentCurrency2.getAmount()) <= 0, "Payments with same sign");
    this._paymentCurrency1 = paymentCurrency1;
    this._paymentCurrency2 = paymentCurrency2;
  }

  /**
   * Gets the payment in the first currency.
   * @return The payment in the first currency.
   */
  public PaymentFixedDefinition getPaymentCurrency1() {
    return _paymentCurrency1;
  }

  /**
   * Gets the payment in the second currency.
   * @return The payment in the second currency.
   */
  public PaymentFixedDefinition getPaymentCurrency2() {
    return _paymentCurrency2;
  }

  /**
   * Gets the first currency.
   * @return The currency.
   */
  public Currency getCurrency1() {
    return _paymentCurrency1.getCurrency();
  }

  /**
   * Gets the second currency.
   * @return The currency.
   */
  public Currency getCurrency2() {
    return _paymentCurrency2.getCurrency();
  }

  /**
   * Gets the exchange date.
   * @return The exchange date.
   */
  public ZonedDateTime getExchangeDate() {
    return _paymentCurrency2.getPaymentDate();
  }

  @Override
  /**
   * The first curve is the discounting curve for the first currency and the second curve is the discounting curve for the second currency.
   */
  public Forex toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "Curves");
    final PaymentFixed payment1 = _paymentCurrency1.toDerivative(date, yieldCurveNames[0]);
    final PaymentFixed payment2 = _paymentCurrency2.toDerivative(date, yieldCurveNames[1]);
    return new Forex(payment1, payment2);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitForexDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitForexDefinition(this);
  }

  @Override
  public String toString() {
    String result = "Forex transaction:";
    result += "\nCurrency 1 payment: " + _paymentCurrency1.toString();
    result += "\nCurrency 2 payment: " + _paymentCurrency2.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _paymentCurrency1.hashCode();
    result = prime * result + _paymentCurrency2.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ForexDefinition other = (ForexDefinition) obj;
    if (!ObjectUtils.equals(_paymentCurrency1, other._paymentCurrency1)) {
      return false;
    }
    if (!ObjectUtils.equals(_paymentCurrency2, other._paymentCurrency2)) {
      return false;
    }
    return true;
  }

}
