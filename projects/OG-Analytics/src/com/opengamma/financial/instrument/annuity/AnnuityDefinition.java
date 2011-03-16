/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * Class describing a generic annuity (or leg).
 * @param <P> The payment type 
 *
 */
public class AnnuityDefinition<P extends PaymentDefinition> implements FixedIncomeInstrumentDefinition<GenericAnnuity<? extends Payment>> {

  //TODO: all the payments should have the same currency: implements the currency and validate the currency?
  /** 
   * The list of payments or coupons.
   */
  private final P[] _payments;
  /**
   * Flag indicating if the annuity is payer (true) or receiver (false).
   */
  private final boolean _isPayer;

  /**
   * Constructor from an array of payments.
   * @param payments The payments
   * @param isPayer The payer/receiver flag.
   */
  public AnnuityDefinition(final P[] payments, boolean isPayer) {
    Validate.noNullElements(payments);
    Validate.isTrue(payments.length > 0);
    _payments = payments;
    _isPayer = isPayer;
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
   * Gets the isPayer field.
   * @return isPayer flag.
   */
  public boolean isPayer() {
    return _isPayer;
  }

  @Override
  public String toString() {
    String result = "Annuity:";
    for (int looppayment = 0; looppayment < _payments.length; looppayment++) {
      result += _payments[looppayment].toString();
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
    AnnuityDefinition<?> other = (AnnuityDefinition<?>) obj;
    if (_isPayer != other._isPayer) {
      return false;
    }
    if (!Arrays.equals(_payments, other._payments)) {
      return false;
    }
    return true;
  }

  @Override
  public GenericAnnuity<? extends Payment> toDerivative(LocalDate date, String... yieldCurveNames) {
    List<Payment> resultList = new ArrayList<Payment>();
    for (int loopcoupon = 0; loopcoupon < _payments.length; loopcoupon++) {
      if (!date.isAfter(_payments[loopcoupon].getPaymentDate().toLocalDate())) {
        resultList.add(_payments[loopcoupon].toDerivative(date, yieldCurveNames));
      }
    }
    return new GenericAnnuity<Payment>(resultList.toArray(new Payment[0]));
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return null;
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return null;
  }

}
