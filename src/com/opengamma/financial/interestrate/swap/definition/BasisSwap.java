/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;

/**
 * 
 */
public class BasisSwap extends Swap {

  public BasisSwap(final VariableAnnuity payLeg, final VariableAnnuity receiveLeg) {
    super(payLeg, receiveLeg);
  }

  @Override
  public VariableAnnuity getPayLeg() {
    return (VariableAnnuity) super.getPayLeg();
  }

  @Override
  public VariableAnnuity getReceiveLeg() {
    return (VariableAnnuity) super.getReceiveLeg();
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<T> visitor, YieldCurveBundle curves) {
    return visitor.visitBasisSwap(this, curves);
  }
}
