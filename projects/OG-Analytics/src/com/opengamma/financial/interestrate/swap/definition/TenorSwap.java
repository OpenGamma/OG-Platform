/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * 
 * @param <R> The type of the (floating) payments
 */
public class TenorSwap<R extends Payment> extends Swap<R, R> {

  public TenorSwap(final GenericAnnuity<R> payLeg, final GenericAnnuity<R> receiveLeg) {
    super(payLeg, receiveLeg);
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitTenorSwap(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitTenorSwap(this);
  }
}
