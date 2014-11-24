/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a generic annuity (or leg) with at least one payment. All the annuity payments are in the same currency.
 * @param <P> The payment type
 *
 */
public class AnnuityDefinition<P extends PaymentDefinition> implements InstrumentDefinitionWithData<Annuity<? extends Payment>, DoubleTimeSeries<ZonedDateTime>> {
  /**
   * The list of payments or coupons. All payments have the same currency. All payments have the same sign or are 0.
   * There should be at least one payment.
   */
  private final P[] _payments;
  /**
   * Flag indicating if the annuity is payer (true) or receiver (false). Deduced from the first non-zero amount;
   * if all amounts don't have the same sign, the flag can be incorrect.
   * @deprecated This flag does not work correctly if the amounts do not have the same sign or if the notionals
   * are zero
   */
  @Deprecated
  private final boolean _isPayer;
  /**
   * The calendar, not null
   */
  private final Calendar _calendar;

  /**
   * Constructor from an array of payments.
   * @param payments The payments, not null. All of them should have the same currency.
   * @param calendar The holiday calendar, not null
   */
  public AnnuityDefinition(final P[] payments, final Calendar calendar) {
    ArgumentChecker.noNulls(payments, "payments");
    ArgumentChecker.isTrue(payments.length > 0, "Have no payments in annuity");
    ArgumentChecker.notNull(calendar, "calendar");
    final double amount = payments[0].getReferenceAmount();
    final Currency currency0 = payments[0].getCurrency();
    for (int loopcpn = 1; loopcpn < payments.length; loopcpn++) {
      ArgumentChecker.isTrue(currency0.equals(payments[loopcpn].getCurrency()), "currency not the same for all payments");
    }
    _payments = payments;
    _isPayer = amount < 0;
    _calendar = calendar;
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
   * @deprecated The payer flag is no longer used; the sign of the notional
   * determines whether a leg is paid or received
   */
  @Deprecated
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
   * Gets the holiday calendar.
   * @return The holiday calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /**
   * Remove the payments paying on or before the given date.
   * @param trimDate The date.
   * @return The trimmed annuity.
   */
  public AnnuityDefinition<?> trimBefore(final ZonedDateTime trimDate) {
    final List<PaymentDefinition> list = new ArrayList<>();
    for (final PaymentDefinition payment : getPayments()) {
      if (payment.getPaymentDate().isAfter(trimDate)) {
        list.add(payment);
      }
    }
    return new AnnuityDefinition<>(list.toArray(new PaymentDefinition[list.size()]), _calendar);
  }

  @Override
  public String toString() {
    final StringBuffer result = new StringBuffer("Annuity:");
    for (final P payment : _payments) {
      result.append(payment.toString());
      result.append(" ");
    }
    return result.toString();
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
  public Annuity<? extends Payment> toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final List<Payment> resultList = new ArrayList<>();
    for (int loopcoupon = 0; loopcoupon < _payments.length; loopcoupon++) {
      if (!date.isAfter(_payments[loopcoupon].getPaymentDate())) {
        resultList.add(_payments[loopcoupon].toDerivative(date));
      }
    }
    return new Annuity<>(resultList.toArray(new Payment[resultList.size()]));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Annuity<? extends Payment> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(indexFixingTS, "index fixing time series");
    final List<Payment> resultList = new ArrayList<>();
    //    ZonedDateTime adjDate = ScheduleCalculator.getAdjustedDate(date, 3, _calendar);
    for (final P payment : _payments) {
      // TODO check this
      if (!date.isAfter(payment.getPaymentDate())) {
        if (payment instanceof InstrumentDefinitionWithData) {
          resultList.add(((InstrumentDefinitionWithData<? extends Payment, DoubleTimeSeries<ZonedDateTime>>) payment).toDerivative(date, indexFixingTS));
        } else {
          resultList.add(payment.toDerivative(date));
        }
      }
    }
    return new Annuity<>(resultList.toArray(new Payment[resultList.size()]));
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitAnnuityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitAnnuityDefinition(this);
  }
}
