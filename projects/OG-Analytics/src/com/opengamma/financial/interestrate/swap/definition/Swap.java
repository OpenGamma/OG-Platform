/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.Annuity;

/**
 * 
 */
public class Swap implements InterestRateDerivative {

  private final Annuity _payLeg;
  private final Annuity _receiveLeg;

  public Swap(final Annuity payLeg, final Annuity receiveLeg) {
    Validate.notNull(payLeg);
    Validate.notNull(receiveLeg);
    _payLeg = payLeg;
    _receiveLeg = receiveLeg;
  }

  public Annuity getPayLeg() {
    return _payLeg;
  }

  public Annuity getReceiveLeg() {
    return _receiveLeg;
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitSwap(this, data);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_payLeg == null) ? 0 : _payLeg.hashCode());
    result = prime * result + ((_receiveLeg == null) ? 0 : _receiveLeg.hashCode());
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
    final Swap other = (Swap) obj;
    return ObjectUtils.equals(this._payLeg, other._payLeg) && ObjectUtils.equals(this._receiveLeg, other._receiveLeg);

  }

  @Override
  public InterestRateDerivative withRate(final double rate) {
    throw new NotImplementedException();
  }

}
