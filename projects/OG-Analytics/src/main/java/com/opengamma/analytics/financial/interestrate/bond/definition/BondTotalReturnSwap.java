/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.TotalReturnSwap;

/**
 *
 */
public class BondTotalReturnSwap extends TotalReturnSwap {

  /**
   * @param fundingLeg The funding leg
   * @param asset The bond
   */
  public BondTotalReturnSwap(final Annuity<? extends Payment> fundingLeg, final BondSecurity<? extends Payment, ? extends Coupon> asset) {
    super(fundingLeg, asset);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBondTotalReturnSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondTotalReturnSwap(this);
  }

}
