/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author Andrew
 */
public class InterestRateFutureSecurity extends FutureSecurity {
  
  private final String _cashRateType; // REVIEW: jim 28-May-2010 -- we might want to make this UniqueIdentifier like FloatingInterestRateLeg...
  
  /**
   * @param expiry
   * @param tradingExchange
   * @param settlementExchange
   */
  public InterestRateFutureSecurity(Expiry expiry, String tradingExchange, String settlementExchange, Currency currency, String cashRateType) {
    super(expiry, tradingExchange, settlementExchange, currency);
    _cashRateType = cashRateType;
  }
  
  public String getCashRateType () {
    return _cashRateType;
  }

  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitInterestRateFutureSecurity (this);
  }

}