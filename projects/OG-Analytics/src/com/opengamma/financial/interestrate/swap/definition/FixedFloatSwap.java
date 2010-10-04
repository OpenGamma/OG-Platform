/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;

/**
 * 
 */
public class FixedFloatSwap extends Swap<FixedCouponPayment, ForwardLiborPayment> {

  /**
   * This sets up a payer swap (i.e. pay the fixed leg and receive the floating leg)
   * @param fixedLeg a fixed annuity for the receive leg
   * @param floatingLeg a variable (floating) annuity for the pay leg
   */
  public FixedFloatSwap(final GenericAnnuity<FixedCouponPayment> fixedLeg, final GenericAnnuity<ForwardLiborPayment> floatingLeg) {
    super(fixedLeg, floatingLeg);
  }

  /**
   * Sets up a basic fixed float swap for testing purposes. For a real world swap, set up the fixed and floating leg separately and pass them to other constructor
   * @param fixedPaymentTimes Time in years of fixed payments 
   * @param floatingPaymentTimes  Time in Years of floating payments
   * @param couponRate fixed rate paid on the notional amount on fixed payment dates (amount paid is notional*rate*yearFraction)
   * @param fundingCurveName  Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   * @see #FixedFloatSwap(FixedCouponAnnuity,ForwardLiborAnnuity)
   */
  public FixedFloatSwap(final double[] fixedPaymentTimes, final double[] floatingPaymentTimes, double couponRate, String fundingCurveName, String liborCurveName) {
    this(new FixedCouponAnnuity(fixedPaymentTimes, couponRate, fundingCurveName), new ForwardLiborAnnuity(floatingPaymentTimes, fundingCurveName, liborCurveName));
  }

  public GenericAnnuity<FixedCouponPayment> getFixedLeg() {
    return getPayLeg();
  }

  public GenericAnnuity<ForwardLiborPayment> getFloatingLeg() {
    return getReceiveLeg();
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitFixedFloatSwap(this, data);
  }

  @Override
  public FixedFloatSwap withRate(double rate) {
    FixedCouponPayment[] payments = getPayLeg().getPayments();
    int n = payments.length;
    FixedCouponPayment[] temp = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = payments[i].withRate(rate);
    }
    return new FixedFloatSwap(new GenericAnnuity<FixedCouponPayment>(temp), getReceiveLeg());
  }

}
