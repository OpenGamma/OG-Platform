/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

//TODO: rewrite the FRN from scratch.
/**
 * 
 */
public class FloatingRateNote extends Swap<PaymentFixed, CouponIbor> {

  public FloatingRateNote(final GenericAnnuity<CouponIbor> forwardLiborAnnuity) {
    super(setUpFixedLeg(forwardLiborAnnuity), forwardLiborAnnuity);
  }

  private static GenericAnnuity<PaymentFixed> setUpFixedLeg(final GenericAnnuity<CouponIbor> annuity) {
    final String curveName = annuity.getNthPayment(0).getFundingCurveName();
    final double notional = annuity.getNthPayment(0).getNotional();
    final PaymentFixed[] fixedPayments = new PaymentFixed[2];
    fixedPayments[0] = new PaymentFixed(0, notional, curveName);
    fixedPayments[1] = new PaymentFixed(annuity.getNthPayment(annuity.getNumberOfPayments() - 1).getPaymentTime(), -notional, curveName);

    return new GenericAnnuity<PaymentFixed>(fixedPayments);
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return null; //visitor.visitFloatingRateNote(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return null; //visitor.visitFloatingRateNote(this);
  }

}
