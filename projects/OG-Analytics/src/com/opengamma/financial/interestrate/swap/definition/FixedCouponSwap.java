/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.Payment;

/**
* A generalisation of a vanilla fixed for floating interest rate swap - here you must have a leg of FixedCouponPayment, but the other leg can be any payment 
* @param <R> The type of the payments on the receive leg 
*/
public class FixedCouponSwap<R extends Payment> extends Swap<FixedCouponPayment, R> {

  /**
   * This sets up a generalised payer swap (i.e. pay the fixed leg and receive the other leg)
   * @param fixedLeg a fixed annuity for the receive leg
   * @param receiveLeg a variable (floating) annuity for the pay leg
   */
  public FixedCouponSwap(final GenericAnnuity<FixedCouponPayment> fixedLeg, final GenericAnnuity<R> receiveLeg) {
    super(fixedLeg, receiveLeg);
  }

  public FixedCouponAnnuity getFixedLeg() {
    return (FixedCouponAnnuity) getPayLeg();
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitFixedCouponSwap(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedCouponSwap(this);
  }

}
