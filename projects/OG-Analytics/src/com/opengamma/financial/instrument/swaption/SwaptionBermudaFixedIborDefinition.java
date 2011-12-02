/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swaption;

import java.util.Arrays;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.util.time.TimeCalculator;

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
    Validate.notNull(expiryDate, "expiry date");
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.isTrue(underlyingSwap.length == expiryDate.length, "Number of swaps not in line with number of expiry dates");
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
    Validate.notNull(expiryDate, "expiry date");
    Validate.notNull(underlyingTotalSwap, "underlying swap");
    final int nbExpiry = underlyingTotalSwap.getFixedLeg().getNumberOfPayments();
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
  public SwaptionBermudaFixedIbor toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    final int nbExpiry = _expiryDate.length;
    final double[] expiryTime = new double[nbExpiry];
    final double[] settleTime = new double[nbExpiry];
    @SuppressWarnings("unchecked")
    final FixedCouponSwap<Coupon>[] expirySwap = new FixedCouponSwap[nbExpiry];
    for (int loopexp = 0; loopexp < nbExpiry; loopexp++) {
      expiryTime[loopexp] = TimeCalculator.getTimeBetween(date, _expiryDate[loopexp]);
      expirySwap[loopexp] = _underlyingSwap[loopexp].toDerivative(date, yieldCurveNames);
      settleTime[loopexp] = TimeCalculator.getTimeBetween(date, _underlyingSwap[loopexp].getFixedLeg().getNthPayment(0).getAccrualStartDate());
    }
    return new SwaptionBermudaFixedIbor(expirySwap, _isLong, expiryTime, settleTime);
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitSwaptionBermudaFixedIborDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SwaptionBermudaFixedIborDefinition other = (SwaptionBermudaFixedIborDefinition) obj;
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
