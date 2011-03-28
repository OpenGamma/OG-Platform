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
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * Class describing a European swaption on a vanilla swap.
 */
public final class SwaptionPhysicalFixedIbor extends EuropeanVanillaOption implements InterestRateDerivative {

  /**
   * Swap underlying the swaption. The swap should be of vanilla type.
   */
  private final FixedCouponSwap<? extends Payment> _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;

  /**
   * Constructor from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryTime The expiry time.
   * @param strike The strike
   * @param underlyingSwap The underlying swap.
   * @param isCall Call.
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionPhysicalFixedIbor(double expiryTime, double strike, FixedCouponSwap<? extends Payment> underlyingSwap, boolean isCall, boolean isLong) {
    super(strike, expiryTime, isCall);
    // A swaption payer can be consider as a call on the swap rate.
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.isTrue(isCall == underlyingSwap.getFixedLeg().isPayer(), "Call flag not in line with underlying");
    _underlyingSwap = underlyingSwap;
    _isLong = isLong;
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryTime The expiry time.
   * @param underlyingSwap The underlying swap.
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   */
  public static SwaptionPhysicalFixedIbor from(double expiryTime, FixedCouponSwap<? extends Payment> underlyingSwap, boolean isLong) {
    Validate.notNull(underlyingSwap, "underlying swap");
    // A swaption payer can be consider as a call on the swap rate.
    double strike = underlyingSwap.getFixedLeg().getNthPayment(0).getFixedRate();
    // Is working only for swap with same rate on all coupons and standard conventions.
    return new SwaptionPhysicalFixedIbor(expiryTime, strike, underlyingSwap, underlyingSwap.getFixedLeg().isPayer(), isLong);
  }

  /**
   * Gets the _underlyingSwap field.
   * @return The underlying swap.
   */
  public FixedCouponSwap<? extends Payment> getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the _isLong field.
   * @return The Long (true)/Short (false) flag.
   */
  public boolean isLong() {
    return _isLong;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitSwaptionPhysicalFixedIbor(this, data);
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitSwaptionPhysicalFixedIbor(this);
  }

}
