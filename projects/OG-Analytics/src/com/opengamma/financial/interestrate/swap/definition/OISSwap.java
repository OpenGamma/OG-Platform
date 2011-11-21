/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;

/**
 * 
 */
public class OISSwap extends FixedCouponSwap<CouponOIS> {

  /**
   * @param firstLeg The fixed leg 
   * @param secondLeg The Floating leg 
   */
  public OISSwap(AnnuityCouponFixed firstLeg, GenericAnnuity<CouponOIS> secondLeg) {
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
