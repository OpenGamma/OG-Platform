/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;

/**
* A swap with the two legs made of coupons.
* @param <C1> The type of the payments on first leg.
* @param <C2> The type of the payments on second leg.
*/
public class SwapCouponCoupon<C1 extends Coupon, C2 extends Coupon> extends Swap<C1, C2> {

  /**
   * Constructor from the two legs.
   * @param firstLeg a fixed annuity for the receive leg
   * @param secondLeg a variable (floating) annuity for the pay leg
   */
  public SwapCouponCoupon(final Annuity<C1> firstLeg, final Annuity<C2> secondLeg) {
    super(firstLeg, secondLeg);
  }

  //  /**
  //   * Gets the swap first leg.
  //   * @return The leg.
  //   */
  //  @Override
  //  public Annuity<C1> getFirstLeg() {
  //    return super.getFirstLeg();
  //  }
  //
  //  /**
  //   * Gets the swap second leg.
  //   * @return The leg.
  //   */
  //  @Override
  //  public Annuity<C2> getSecondLeg() {
  //    return super.getSecondLeg();
  //  }

  /**
   * Create a new swap with the payments of both legs of the original one paying before or on the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  @Override
  public SwapCouponCoupon<C1, C2> trimAfter(double trimTime) {
    return new SwapCouponCoupon<C1, C2>(getFirstLeg().trimAfter(trimTime), getSecondLeg().trimAfter(trimTime));
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitSwapCouponCoupon(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitSwapCouponCoupon(this);
  }

}
