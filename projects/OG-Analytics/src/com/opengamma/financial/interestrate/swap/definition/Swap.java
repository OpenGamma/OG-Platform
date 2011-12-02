/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * Class describing a generic swap with two legs. One should be payer and the other receiver.
 * @param <P1> The type of the payments in the payLeg
 * @param <P2> The type of the payments in the receiveLeg
 */
public class Swap<P1 extends Payment, P2 extends Payment> implements InstrumentDerivative {
  private final GenericAnnuity<P1> _firstLeg;
  private final GenericAnnuity<P2> _secondLeg;

  public Swap(final GenericAnnuity<P1> firstLeg, final GenericAnnuity<P2> secondLeg) {
    Validate.notNull(firstLeg);
    Validate.notNull(secondLeg);
    Validate.isTrue((firstLeg.isPayer() != secondLeg.isPayer()), "both legs have same payer flag");
    _firstLeg = firstLeg;
    _secondLeg = secondLeg;
  }

  public GenericAnnuity<P1> getFirstLeg() {
    return _firstLeg;
  }

  public GenericAnnuity<P2> getSecondLeg() {
    return _secondLeg;
  }

  /**
   * Create a new swap with the payments of both legs of the original one paying before or on the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  public Swap<P1, P2> trimAfter(double trimTime) {
    return new Swap<P1, P2>(_firstLeg.trimAfter(trimTime), _secondLeg.trimAfter(trimTime));
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitSwap(this);
  }

  @Override
  public String toString() {
    String result = "Swap : \n";
    result += "First leg: " + _firstLeg.toString();
    result += "\nSecond leg: " + _secondLeg.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _firstLeg.hashCode();
    result = prime * result + _secondLeg.hashCode();
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
    final Swap<?, ?> other = (Swap<?, ?>) obj;
    return ObjectUtils.equals(this._firstLeg, other._firstLeg) && ObjectUtils.equals(this._secondLeg, other._secondLeg);

  }
}
