/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.derivative;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Bermuda swaption on vanilla swaps with physical delivery.
 */
public class SwaptionBermudaFixedIbor implements InstrumentDerivative {

  /**
   * The swaps underlying the swaption. There is one swap for each expiration date. All swaps shoud have the same currency.
   * The swap do not need to be identical; this allow to incorporate fees or changing margins in the description.
   */
  private final SwapFixedCoupon<? extends Coupon>[] _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;
  /**
   * The swaption expiration dates.
   */
  private final double[] _expiryTime;
  /**
   * The time (in years) to swaps settlement.
   */
  private final double[] _settlementTime;

  /**
   * Constructor for the Bermuda swaption.
   * @param underlyingSwap The swaps underlying the swaption. There is one swap for each expiration date.
   * @param isLong Flag indicating if the option is long (true) or short (false).
   * @param expiryTime The swaption expiration times.
   * @param settlementTime The times (in year) to the swaps settlement.
   */
  public SwaptionBermudaFixedIbor(final SwapFixedCoupon<? extends Coupon>[] underlyingSwap, final boolean isLong, final double[] expiryTime, final double[] settlementTime) {
    ArgumentChecker.notNull(expiryTime, "Expiry time");
    ArgumentChecker.notNull(underlyingSwap, "Underlying swap");
    ArgumentChecker.notNull(settlementTime, "Settlement time");
    ArgumentChecker.isTrue(underlyingSwap.length == expiryTime.length, "Number of swaps not in line with number of expiry times");
    ArgumentChecker.isTrue(underlyingSwap.length == settlementTime.length, "Number of swaps not in line with number of settlement times");
    _underlyingSwap = underlyingSwap;
    _isLong = isLong;
    _expiryTime = expiryTime;
    _settlementTime = settlementTime;
  }

  /**
   * Gets the swaps underlying the swaption. There is one swap for each expiration date.
   * @return The underlying swaps.
   */
  public SwapFixedCoupon<? extends Coupon>[] getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the flag indicating if the option is long (true) or short (false).
   * @return The flag.
   */
  public boolean isLong() {
    return _isLong;
  }

  /**
   * Gets the swaption expiration dates.
   * @return The swaption expiration dates.
   */
  public double[] getExpiryTime() {
    return _expiryTime;
  }

  /**
   * Gets the times (in year) to the swaps settlement.
   * @return The times to the swaps settlement.
   */
  public double[] getSettlementTime() {
    return _settlementTime;
  }

  /**
   * Gets the swaption currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlyingSwap[0].getFirstLeg().getCurrency();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionBermudaFixedIbor(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionBermudaFixedIbor(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_expiryTime);
    result = prime * result + (_isLong ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_settlementTime);
    result = prime * result + Arrays.hashCode(_underlyingSwap);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SwaptionBermudaFixedIbor other = (SwaptionBermudaFixedIbor) obj;
    if (!Arrays.equals(_expiryTime, other._expiryTime)) {
      return false;
    }
    if (_isLong != other._isLong) {
      return false;
    }
    if (!Arrays.equals(_settlementTime, other._settlementTime)) {
      return false;
    }
    if (!Arrays.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

}
