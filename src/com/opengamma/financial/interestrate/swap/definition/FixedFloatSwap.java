/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;

/**
 * 
 */
public class FixedFloatSwap extends Swap {

  /**
   *  This sets up a payer swap (i.e. pay the fixed leg and receive the floating leg)
   * @param fixedLeg a fixed annuity for the receive leg
   * @param floatingLeg a variable (floating) annuity for the pay leg
   */
  public FixedFloatSwap(final ConstantCouponAnnuity fixedLeg, final VariableAnnuity floatingLeg) {
    super(fixedLeg, floatingLeg);
  }

  /**
   * This sets up a payer swap (i.e. pay the fixed leg and receive the floating leg) with notional of 1.0
   * @param fixedPaymentTimes Time in years of fixed payments 
   * @param floatingPaymentTimes Time in Years of floating payments
   * @param couponRate fixed rate paid on the notional amount on fixed payment dates (amount paid is notional*rate*yearFraction)
   * @param fwdStartOffsets offset in years of start of libor accruing period from <b>previous</b> floating payment time (or trade date if spot libor)
   * @param fwdEndOffsets  offset in years of end of libor accruing period from floating payment time
   * @param fundingCurveName Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public FixedFloatSwap(final double[] fixedPaymentTimes, final double[] floatingPaymentTimes, double couponRate, final double[] fwdStartOffsets, final double[] fwdEndOffsets,
      String fundingCurveName, String liborCurveName) {
    this(fixedPaymentTimes, floatingPaymentTimes, 1.0, couponRate, fwdStartOffsets, fwdEndOffsets, fundingCurveName, liborCurveName);
  }

  /**
   * This sets up a payer swap (i.e. pay the fixed leg and receive the floating leg)
   * @param fixedPaymentTimes Time in years of fixed payments 
   * @param floatingPaymentTimes Time in Years of floating payments
   * @param notional the notional amount of the swap (payments are calculated as interest payments on this amount) 
   * @param couponRate fixed rate paid on the notional amount on fixed payment dates (amount paid is notional*rate*yearFraction)
   * @param fwdStartOffsets offset in years of start of libor accruing period from <b>previous</b> floating payment time (or trade date if spot libor)
   * @param fwdEndOffsets  offset in years of end of libor accruing period from floating payment time
   * @param fundingCurveName Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public FixedFloatSwap(final double[] fixedPaymentTimes, final double[] floatingPaymentTimes, double notional, double couponRate, final double[] fwdStartOffsets, final double[] fwdEndOffsets,
      String fundingCurveName, String liborCurveName) {
    super(new ConstantCouponAnnuity(fixedPaymentTimes, notional, couponRate, fundingCurveName), new VariableAnnuity(floatingPaymentTimes, notional, fwdStartOffsets, fwdEndOffsets, fundingCurveName,
        liborCurveName));
  }

  public ConstantCouponAnnuity getFixedLeg() {
    return (ConstantCouponAnnuity) getPayLeg();
  }

  public VariableAnnuity getFloatingLeg() {
    return (VariableAnnuity) getReceiveLeg();
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<T> visitor, YieldCurveBundle curves) {
    return visitor.visitFixedFloatSwap(this, curves);
  }

}
