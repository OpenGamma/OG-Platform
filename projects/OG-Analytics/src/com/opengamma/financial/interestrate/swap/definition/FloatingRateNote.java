/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;

/**
 * 
 */
public class FloatingRateNote extends Swap {

  public FloatingRateNote(final VariableAnnuity annuity) {
    super(setUpFixedLeg(annuity), annuity);
  }

  private static FixedAnnuity setUpFixedLeg(final VariableAnnuity annuity) {
    String curveName = annuity.getFundingCurveName();
    double notional = annuity.getNotional();
    double[] paymentTimes = new double[2];
    paymentTimes[0] = 0.0;
    paymentTimes[1] = annuity.getPaymentTimes()[annuity.getNumberOfPayments() - 1];

    return new FixedAnnuity(paymentTimes, notional, new double[] {1, -1}, new double[] {1, 1}, curveName);
  }

  @Override
  public FixedAnnuity getPayLeg() {
    return (FixedAnnuity) super.getPayLeg();
  }

  @Override
  public VariableAnnuity getReceiveLeg() {
    return (VariableAnnuity) super.getReceiveLeg();
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitFloatingRateNote(this, data);
  }

}
