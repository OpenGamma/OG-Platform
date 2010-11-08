/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;

/**
 * 
 */
public class FloatingRateNote extends Swap<FixedPayment, ForwardLiborPayment> {

  public FloatingRateNote(final GenericAnnuity<ForwardLiborPayment> forwardLiborAnnuity) {
    super(setUpFixedLeg(forwardLiborAnnuity), forwardLiborAnnuity);
  }

  private static GenericAnnuity<FixedPayment> setUpFixedLeg(final GenericAnnuity<ForwardLiborPayment> annuity) {

    String curveName = annuity.getNthPayment(0).getFundingCurveName();
    double notional = annuity.getNthPayment(0).getNotional();
    FixedPayment[] fixedPayments = new FixedPayment[2];
    fixedPayments[0] = new FixedPayment(0, notional, curveName);
    fixedPayments[1] = new FixedPayment(annuity.getNthPayment(annuity.getNumberOfPayments() - 1).getPaymentTime(), -notional, curveName);

    return new GenericAnnuity<FixedPayment>(fixedPayments);
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitFloatingRateNote(this, data);
  }

}
