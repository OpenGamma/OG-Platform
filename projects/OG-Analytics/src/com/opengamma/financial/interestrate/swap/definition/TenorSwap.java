/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;

/**
 * 
 */
public class TenorSwap extends Swap<ForwardLiborPayment, ForwardLiborPayment> {

  public TenorSwap(final GenericAnnuity<ForwardLiborPayment> payLeg, final GenericAnnuity<ForwardLiborPayment> receiveLeg) {
    super(payLeg, receiveLeg);
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitTenorSwap(this, data);
  }

}
