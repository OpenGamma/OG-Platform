/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a generic annuity (or leg) with at least one payment. All the annuity payments are in the same currency. 
 * @param <P> The payment type 
 *
 */
public class AnnuityDefinition<P extends PaymentDefinition> implements InstrumentDefinitionWithData<GenericAnnuity<? extends Payment>, DoubleTimeSeries<ZonedDateTime>> {
  /** Empty array for array conversion of list */
  protected static final Payment[] EMPTY_ARRAY = new Payment[0];
  /** 
   * The list of payments or coupons. All payments have the same currency. All payments have the same sign or are 0.
   */
  private final P[] _payments;
  /**
   * Flag indicating if the annuity is payer (true) or receiver (false). Deduced from the first non-zero amount; 
   * if all amounts don't have the same sign, the flag can be incorrect.
   */
  private final boolean _isPayer;

  /**
   * Constructor from an array of payments.
   * @param payments The payments. All of them should have the same currency.
   */
  public AnnuityDefinition(final P[] payments) {
    Validate.noNullElements(payments);
    Validate.isTrue(payments.length > 0, "Have no payments in annuity");
    double amount = payments[0].getReferenceAmount();
    final Currency currency0 = payments[0].getCurrency();
    for (int loopcpn = 1; loopcpn < payments.length; loopcpn++) {
      Validate.isTrue(currency0.equals(payments[loopcpn].getCurrency()), "currency not the same for all payments");
      amount = (amount == 0) ? payments[loopcpn].getReferenceAmount() : amount; // amount contains the first non-zero element if any and 0 if not.
    }
    _payments = payments;
    _isPayer = (amount < 0);
  }

  /**
   * Gets the _payments field.
   * @return the payments
   */
  public P[] getPayments() {
    return _payments;
  }

  /**
   * Return one of the payments.
   * @param n The payment index.
   * @return The payment.
   */
  public P getNthPayment(final int n) {
    return _payments[n];
  }

  /**
   * Return the currency of the annuity. 
   * @return The currency.
   */
  public Currency getCurrency() {
    return _payments[0].getCurrency();
  }

  /**
   * Gets the isPayer field.
   * @return isPayer flag.
   */
  public boolean isPayer() {
    return _isPayer;
  }

  /**
   * The number of payments of the annuity.
   * @return The number of payments.
   */
  public int getNumberOfPayments() {
    return _payments.length;
  }

  /**
   * Remove the payments paying on or before the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  public AnnuityDefinition<?> trimBefore(ZonedDateTime trimDate) {
    List<PaymentDefinition> list = new ArrayList<PaymentDefinition>();
    for (PaymentDefinition payment : getPayments()) {
      if (payment.getPaymentDate().isAfter(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityDefinition<PaymentDefinition>(list.toArray(new PaymentDefinition[0]));
  }

  @Override
  public String toString() {
    String result = "Annuity:";
    for (final P payment : _payments) {
      result += payment.toString();
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_isPayer ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_payments);
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
    final AnnuityDefinition<?> other = (AnnuityDefinition<?>) obj;
    if (_isPayer != other._isPayer) {
      return false;
    }
    if (!Arrays.equals(_payments, other._payments)) {
      return false;
    }
    return true;
  }

  @Override
  public GenericAnnuity<? extends Payment> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final List<Payment> resultList = new ArrayList<Payment>();
    for (int loopcoupon = 0; loopcoupon < _payments.length; loopcoupon++) {
      if (!date.isAfter(_payments[loopcoupon].getPaymentDate())) {
        resultList.add(_payments[loopcoupon].toDerivative(date, yieldCurveNames));
      }
    }
    return new GenericAnnuity<Payment>(resultList.toArray(EMPTY_ARRAY));
  }

  @SuppressWarnings("unchecked")
  @Override
  public GenericAnnuity<? extends Payment> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final List<Payment> resultList = new ArrayList<Payment>();
    for (final P payment : _payments) {
      //TODO check this 
      if (!date.isAfter(payment.getPaymentDate())) {
        if (payment instanceof InstrumentDefinitionWithData) {
          resultList.add(((InstrumentDefinitionWithData<? extends Payment, DoubleTimeSeries<ZonedDateTime>>) payment).toDerivative(date, indexFixingTS, yieldCurveNames));
        } else {
          resultList.add(payment.toDerivative(date, yieldCurveNames));
        }
      }
    }
    return new GenericAnnuity<Payment>(resultList.toArray(EMPTY_ARRAY));
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitAnnuityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitAnnuityDefinition(this);
  }
}
