/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;

/**
 * Class describing a European swaption on a vanilla swap.
 */
public class SwaptionFixedIbor implements InterestRateDerivative {
  /**
   * Time to the swaption expiry.
   */
  private final double _expiryTime;
  /**
   * Swap underlying the swaption. The swap should be of vanilla type.
   */
  private final FixedCouponSwap<? extends Payment> _underlyingSwap;
  //TODO: Should the cash/physical feature be in a flag or two instruments should be created?
  /**
   * A flag indicating if the swaption is cash-settled (true) or physical delivery (false).
   */
  private final boolean _isCash;

  /**
   * Swaption construction from the underlying and the exercise details.
   * @param expiryTime The time (in year) to expiry.
   * @param underlyingSwap The swap underlying the swaption. The swap should be of vanilla type.
   * @param isCash A flag indicating if the swaption is cash-settled (true) or physical delivery (false).
   */
  public SwaptionFixedIbor(double expiryTime, FixedCouponSwap<? extends Payment> underlyingSwap, boolean isCash) {
    Validate.notNull(underlyingSwap, "underlying swap");
    _underlyingSwap = underlyingSwap;
    Validate.isTrue(expiryTime >= 0, "expiry time < 0");
    _expiryTime = expiryTime;
    _isCash = isCash;
  }

  /**
   * Swaption construction from the underlying and the expiry time. The delivery type is physical delivery.
   * @param expiryTime The time (in year) to expiry.
   * @param underlyingSwap The swap underlying the swaption. The swap should be of vanilla type.
   */
  public SwaptionFixedIbor(double expiryTime, FixedCouponSwap<? extends Payment> underlyingSwap) {
    this(expiryTime, underlyingSwap, false);
  }

  /**
   * Gets the _underlyingSwap field.
   * @return The underlying swap.
   */
  public FixedCouponSwap<? extends Payment> getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the _expiryTime field.
   * @return The time to expiry.
   */
  public double getExpiryTime() {
    return _expiryTime;
  }

  /**
   * Gets the _isCash field.
   * @return The cash(true)/physical(false) flag.
   */
  public boolean isCash() {
    return _isCash;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return null;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return null;
  }

}
