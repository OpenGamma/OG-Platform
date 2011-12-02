/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap.definition;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;

/**
* A generalisation of a vanilla fixed for floating interest rate swap - here you must have a leg of FixedCouponPayment, but the other leg can be any payment 
* @param <R> The type of the payments on the receive leg 
*/
public class FixedCouponSwap<R extends Coupon> extends Swap<CouponFixed, R> {

  /**
   * This sets up a generalised payer swap (i.e. pay the fixed leg and receive the other leg)
   * @param fixedLeg a fixed annuity for the receive leg
   * @param receiveLeg a variable (floating) annuity for the pay leg
   */
  public FixedCouponSwap(final GenericAnnuity<CouponFixed> fixedLeg, final GenericAnnuity<R> receiveLeg) {
    super(fixedLeg, receiveLeg);
  }

  public AnnuityCouponFixed getFixedLeg() {
    return (AnnuityCouponFixed) getFirstLeg();
  }

  /**
   * Check if the payments of of the other leg is of the type CouponFixed or CouponIbor. Used to check that payment are of vanilla type.
   * @return True if IborCoupon or FixedCoupon 
   */
  public boolean isIborOrFixed() {
    return getSecondLeg().isIborOrFixed();
  }

  /**
   * Creates a new swap with the same characteristics, except that the absolute value of the notional of all coupons is the one given.
   * @param notional The notional.
   * @return The new swap.
   */
  @SuppressWarnings("unchecked")
  public FixedCouponSwap<R> withNotional(double notional) {
    AnnuityCouponFixed legFixedNotional = getFixedLeg().withNotional(notional * Math.signum(getFixedLeg().getNthPayment(0).getNotional()));
    Coupon[] cpn = new Coupon[getSecondLeg().getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < getSecondLeg().getNumberOfPayments(); loopcpn++) {
      cpn[loopcpn] = getSecondLeg().getNthPayment(loopcpn).withNotional(notional * Math.signum(getSecondLeg().getNthPayment(loopcpn).getNotional()));
    }
    return new FixedCouponSwap<R>(legFixedNotional, new GenericAnnuity<R>((R[]) cpn));
  }

  /**
   * Create a new swap with the payments of both legs of the original one paying before or on the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  @Override
  public FixedCouponSwap<R> trimAfter(double trimTime) {
    return new FixedCouponSwap<R>(getFixedLeg().trimAfter(trimTime), getSecondLeg().trimAfter(trimTime));
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitFixedCouponSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedCouponSwap(this);
  }

}
