/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
    final String curveName = annuity.getNthPayment(0).getFundingCurveName();
    final double notional = annuity.getNthPayment(0).getNotional();
    final FixedPayment[] fixedPayments = new FixedPayment[2];
    fixedPayments[0] = new FixedPayment(0, notional, curveName);
    fixedPayments[1] = new FixedPayment(annuity.getNthPayment(annuity.getNumberOfPayments() - 1).getPaymentTime(), -notional, curveName);

    return new GenericAnnuity<FixedPayment>(fixedPayments);
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitFloatingRateNote(this, data);
  }

}
