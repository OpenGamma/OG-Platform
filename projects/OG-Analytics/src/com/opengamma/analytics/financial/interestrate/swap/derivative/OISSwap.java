/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;

/**
 * 
 */
/**
 * @deprecated When a SwapFixedIborDefinition is converted, the result is not necessarily a FixedFloatSwap as some Ibor coupons may have fixed already. 
 * This instrument is never used in the natural flow "Definition->toDerivative->Derivative".
 */
@Deprecated
public class OISSwap extends SwapFixedCoupon<CouponOIS> {

  /**
   * @param firstLeg The fixed leg 
   * @param secondLeg The Floating leg 
   */
  public OISSwap(AnnuityCouponFixed firstLeg, Annuity<CouponOIS> secondLeg) {
    super(firstLeg, secondLeg);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitOISSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitOISSwap(this);
  }

}
