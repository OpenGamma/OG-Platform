/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.derivative;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a generic swap with multiple legs.
 */
public class SwapMultileg implements InstrumentDerivative {

  /**
   * The swap first leg.
   */
  private final Annuity<? extends Payment>[] _legs;

  public SwapMultileg(final Annuity<? extends Payment>[] legs) {
    ArgumentChecker.noNulls(legs, "legs");
    ArgumentChecker.isTrue(legs.length > 0, "SwapMultileg should have at least one leg");
    _legs = legs;
  }

  /**
   * Returns the legs.
   * @return The legs.
   */
  public Annuity<? extends Payment>[] getLegs() {
    return _legs;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapMultileg(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapMultileg(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_legs);
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
    SwapMultileg other = (SwapMultileg) obj;
    if (!Arrays.equals(_legs, other._legs)) {
      return false;
    }
    return true;
  }

}
