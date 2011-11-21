/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class FixedFloatSwap extends FixedCouponSwap<CouponIbor> {

  /**
   * This sets up a payer swap (i.e. pay the fixed leg and receive the floating leg)
   * @param fixedLeg a fixed annuity for the receive leg
   * @param floatingLeg a variable (floating) annuity for the pay leg
   */
  public FixedFloatSwap(final AnnuityCouponFixed fixedLeg, final GenericAnnuity<CouponIbor> floatingLeg) {
    super(fixedLeg, floatingLeg);
  }

  /**
   * Sets up a basic fixed float swap for testing purposes. For a real world swap, set up the fixed and floating leg separately and pass them to other constructor
   * @param currency The payment currency.
   * @param fixedPaymentTimes Time in years of fixed payments 
   * @param floatingPaymentTimes  Time in Years of floating payments
   * @param index TODO
   * @param couponRate fixed rate paid on the notional amount on fixed payment dates (amount paid is notional*rate*yearFraction)
   * @param fundingCurveName  Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   * @param isPayer whether the swap is a payer (i.e. pay the fixed leg)
   */
  public FixedFloatSwap(Currency currency, final double[] fixedPaymentTimes, final double[] floatingPaymentTimes, IborIndex index, final double couponRate, final String fundingCurveName,
      final String liborCurveName, boolean isPayer) {
    this(new AnnuityCouponFixed(currency, fixedPaymentTimes, couponRate, fundingCurveName, isPayer), new AnnuityCouponIbor(currency, floatingPaymentTimes, index, fundingCurveName, liborCurveName,
        !isPayer));
  }

  public AnnuityCouponIbor getFloatingLeg() {
    return (AnnuityCouponIbor) getSecondLeg();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitFixedFloatSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedFloatSwap(this);
  }
}
