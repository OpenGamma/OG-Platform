/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.derivative;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a generic swap with two legs. One should be payer and the other receiver. The two legs currencies can be different.
 * @param <P1> The type of the payments in the payLeg
 * @param <P2> The type of the payments in the receiveLeg
 */
public class Swap<P1 extends Payment, P2 extends Payment> implements InstrumentDerivative, Serializable {

  /**
   * The swap first leg.
   */
  private final Annuity<P1> _firstLeg;
  /**
   * The swap second leg. The two leg should have opposite payer flags.
   */
  private final Annuity<P2> _secondLeg;

  /**
   * Constructor from two legs.
   * @param firstLeg The swap first leg.
   * @param secondLeg The swap second leg. The two leg should have opposite payer flags.
   */
  public Swap(final Annuity<P1> firstLeg, final Annuity<P2> secondLeg) {
    ArgumentChecker.notNull(firstLeg, "first leg");
    ArgumentChecker.notNull(secondLeg, "second leg");
    _firstLeg = firstLeg;
    _secondLeg = secondLeg;
  }

  /**
   * Gets the swap first leg.
   * @return The leg.
   */
  public Annuity<P1> getFirstLeg() {
    return _firstLeg;
  }

  /**
   * Gets the swap second leg.
   * @return The leg.
   */
  public Annuity<P2> getSecondLeg() {
    return _secondLeg;
  }

  /**
   * Create a new swap with the payments of both legs of the original one paying before or on the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  public Swap<P1, P2> trimAfter(final double trimTime) {
    return new Swap<>(_firstLeg.trimAfter(trimTime), _secondLeg.trimAfter(trimTime));
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwap(this);
  }

  @Override
  public String toString() {
    String result = "Swap : \n";
    result += "First leg: \n" + _firstLeg.toString() + "\n";
    result += "Second leg: \n" + _secondLeg.toString();
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
