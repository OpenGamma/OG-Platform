/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;

/**
 * @param <R> The type of the (floating) payments
 * @deprecated When a SwapFixedIborDefinition is converted, the result is not necessarily a FixedFloatSwap as some Ibor coupons may have fixed already. 
 * This instrument is never used in the natural flow "Definition->toDerivative->Derivative".
 */
@Deprecated
public class TenorSwap<R extends Payment> extends Swap<R, R> {

  public TenorSwap(final Annuity<R> payLeg, final Annuity<R> receiveLeg) {
    super(payLeg, receiveLeg);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitTenorSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitTenorSwap(this);
  }
}
