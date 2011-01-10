/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * 
 * @param <P> The type of the payments in the payLeg
 * @param <R> The type of the payments in the receiveLeg
 */
public class Swap<P extends Payment, R extends Payment> implements InterestRateDerivative {
  private final GenericAnnuity<P> _payLeg;
  private final GenericAnnuity<R> _receiveLeg;

  public Swap(final GenericAnnuity<P> payLeg, final GenericAnnuity<R> receiveLeg) {
    Validate.notNull(payLeg);
    Validate.notNull(receiveLeg);
    _payLeg = payLeg;
    _receiveLeg = receiveLeg;
  }

  public GenericAnnuity<P> getPayLeg() {
    return _payLeg;
  }

  public GenericAnnuity<R> getReceiveLeg() {
    return _receiveLeg;
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitSwap(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitSwap(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _payLeg.hashCode();
    result = prime * result + _receiveLeg.hashCode();
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
    return ObjectUtils.equals(this._payLeg, other._payLeg) && ObjectUtils.equals(this._receiveLeg, other._receiveLeg);

  }
}
