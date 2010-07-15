/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * An interest rate future.
 */
public class InterestRateFutureSecurity extends FutureSecurity {

  protected static final String CASHRATETYPE_KEY = "cashRateType";

  /** The cash rate type. */
  private final String _cashRateType; // REVIEW: jim 28-May-2010 -- we might want to make this UniqueIdentifier like FloatingInterestRateLeg...

  /**
   * Creates an interest rate future.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param cashRateType  the cash rate type
   */
  public InterestRateFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final String cashRateType) {
    super(expiry, tradingExchange, settlementExchange, currency);
    _cashRateType = cashRateType;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the cash rate type.
   * @return the cash rate type
   */
  public String getCashRateType() {
    return _cashRateType;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitInterestRateFutureSecurity(this);
  }

}
