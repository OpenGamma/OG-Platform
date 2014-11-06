/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swaption;

import java.util.Arrays;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a Bermuda swaption on vanilla swaps with physical delivery.
 */
public class SwaptionBermudaFixedIborDefinition implements InstrumentDefinition<SwaptionBermudaFixedIbor> {

  /**
   * The swaps underlying the swaption. There is one swap for each expiration date.
   * The swap do not need to be identical; this allow to incorporate fees or changing margins in the description.
   */
  private final SwapFixedIborDefinition[] _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;
  /**
   * The swaption expiration dates.
   */
  private final ZonedDateTime[] _expiryDate;

  /**
   * Constructor for the Bermuda swaption.
   * @param underlyingSwap The swaps underlying the swaption. There is one swap for each expiration date.
   * @param isLong Flag indicating if the option is long (true) or short (false).
   * @param expiryDate The swaption expiration dates.
   */
  public SwaptionBermudaFixedIborDefinition(final SwapFixedIborDefinition[] underlyingSwap, final boolean isLong, final ZonedDateTime[] expiryDate) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlyingSwap, "underlying swap");
    ArgumentChecker.isTrue(underlyingSwap.length == expiryDate.length, "Number of swaps not in line with number of expiry dates");
    this._underlyingSwap = underlyingSwap;
    this._isLong = isLong;
    this._expiryDate = expiryDate;
  }

  /**
   * Creates a Bermudan swaption from a unique swap and the expiry dates. For each expiry dates, a exercise swap with the coupon that start on or
   * after the exercise date is created.
   * @param underlyingTotalSwap The underlying swap.
   * @param isLong Flag indicating if the option is long (true) or short (false).
   * @param expiryDate The swaption expiration dates.
   * @return The Bermuda swaption.
   */
  public static SwaptionBermudaFixedIborDefinition from(final SwapFixedIborDefinition underlyingTotalSwap, final boolean isLong, final ZonedDateTime[] expiryDate) {
    ArgumentChecker.notNull(expiryDate, "expiry date");
    ArgumentChecker.notNull(underlyingTotalSwap, "underlying swap");
    final int nbExpiry = underlyingTotalSwap.getFixedLeg().getNumberOfPayments();
    ArgumentChecker.isTrue(expiryDate.length == nbExpiry, "Number of expiries provided {} did not match the number of fixed payments of underlying swap {}", expiryDate.length, nbExpiry);
    final SwapFixedIborDefinition[] underlyingSwaps = new SwapFixedIborDefinition[nbExpiry];
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      underlyingSwaps[loopexp] = underlyingTotalSwap.trimStart(expiryDate[loopexp]);
    }
    return new SwaptionBermudaFixedIborDefinition(underlyingSwaps, isLong, expiryDate);

  }

  /**
   * Gets the swaps underlying the swaption. There is one swap for each expiration date.
   * @return The underlying swaps.
   */
  public SwapFixedIborDefinition[] getUnderlyingSwap() {
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
  public ZonedDateTime[] getExpiryDate() {
    return _expiryDate;
  }

  @Override
  public SwaptionBermudaFixedIbor toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final int nbExpiry = _expiryDate.length;
    final double[] expiryTime = new double[nbExpiry];
    final double[] settleTime = new double[nbExpiry];
    @SuppressWarnings("unchecked")
    final SwapFixedCoupon<Coupon>[] expirySwap = new SwapFixedCoupon[nbExpiry];
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      expiryTime[loopexp] = TimeCalculator.getTimeBetween(date, _expiryDate[loopexp]);
      expirySwap[loopexp] = _underlyingSwap[loopexp].toDerivative(date);
      settleTime[loopexp] = TimeCalculator.getTimeBetween(date, _underlyingSwap[loopexp].getFixedLeg().getNthPayment(0).getAccrualStartDate());
    }
    return new SwaptionBermudaFixedIbor(expirySwap, _isLong, expiryTime, settleTime);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionBermudaFixedIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwaptionBermudaFixedIborDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_expiryDate);
    result = prime * result + (_isLong ? 1231 : 1237);
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
    final SwaptionBermudaFixedIborDefinition other = (SwaptionBermudaFixedIborDefinition) obj;
    if (!Arrays.equals(_expiryDate, other._expiryDate)) {
      return false;
    }
    if (_isLong != other._isLong) {
      return false;
    }
    if (!Arrays.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

}
