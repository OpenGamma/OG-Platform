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
  
  private final Currency _currency;
  private final String _cashRateType;
  
  /**
   * @param expiry
   * @param tradingExchange
   * @param settlementExchange
   */
  public InterestRateFutureSecurity(Expiry expiry, String tradingExchange, String settlementExchange, Currency currency, String cashRateType) {
    super(expiry, tradingExchange, settlementExchange);
    _currency = currency;
    _cashRateType = cashRateType;
  }
  
  public Currency getCurrency () {
    return _currency;
  }
  
  public String getCashRateType () {
    return _cashRateType;
  }

  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitInterestRateFutureSecurity (this);
  }

}