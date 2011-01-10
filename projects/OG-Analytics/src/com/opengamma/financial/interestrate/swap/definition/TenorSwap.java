/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.InterestRateDerivativeWithRate;
import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;

/**
 * 
 */
public class TenorSwap extends Swap<ForwardLiborPayment, ForwardLiborPayment> implements InterestRateDerivativeWithRate {

  public TenorSwap(final GenericAnnuity<ForwardLiborPayment> payLeg, final GenericAnnuity<ForwardLiborPayment> receiveLeg) {
    super(payLeg, receiveLeg);
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitTenorSwap(this, data);
  }

  @Override
  public InterestRateDerivativeWithRate withRate(double rate) {
    return new TenorSwap(getPayLeg(), ((ForwardLiborAnnuity) getReceiveLeg()).withRate(rate));
  }

}
