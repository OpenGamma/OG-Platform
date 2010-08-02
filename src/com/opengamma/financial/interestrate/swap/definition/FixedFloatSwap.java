/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
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
  public FixedFloatSwap(final FixedAnnuity fixedLeg, final VariableAnnuity floatingLeg) {
    super(fixedLeg, floatingLeg);
  }

  /**
   * This sets up a payer swap (i.e. pay the fixed leg and receive the floating leg) with notional of 1.0
   * @param fixedPaymentTimes Time in years of fixed payments 
   * @param fixedPaymentAmounts The payments on the fixed leg - <b>Note</b> these are the actual cash payments - i.e. a rate times a year fraction
   * @param floatingPaymentTimes Time in Years of floating payments
   * @param fwdStartOffsets offset in years of start of libor accruing period from <b>previous</b> floating payment time (or trade date if spot libor)
   * @param fwdEndOffsets  offset in years of end of libor accruing period from floating payment time
   * @param fundingCurveName Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public FixedFloatSwap(final double[] fixedPaymentTimes, final double[] fixedPaymentAmounts, final double[] floatingPaymentTimes, final double[] fwdStartOffsets, final double[] fwdEndOffsets,
      String fundingCurveName, String liborCurveName) {
    super(new FixedAnnuity(fixedPaymentTimes, fixedPaymentAmounts, fundingCurveName), new VariableAnnuity(floatingPaymentTimes, 1.0, fwdStartOffsets, fwdEndOffsets, fundingCurveName, liborCurveName));
  }

  public FixedAnnuity getFixedLeg() {
    return (FixedAnnuity) getPayLeg();
  }

  public VariableAnnuity getFloatingLeg() {
    return (VariableAnnuity) getReceiveLeg();
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<T> visitor, YieldCurveBundle curves) {
    return visitor.visitFixedFloatSwap(this, curves);
  }

}
