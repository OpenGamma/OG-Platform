/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author Andrew
 */
public class BondFutureSecurity extends FutureSecurity {
  
  // Additional fields ...
  
  /**
   * @param expiry
   * @param month
   * @param year
   * @param tradingExchange
   * @param settlementExchange
   */
  public BondFutureSecurity(Expiry expiry, int month, int year,
      String tradingExchange, String settlementExchange) {
    super(expiry, month, year, tradingExchange, settlementExchange);
  }

  @Override
  public <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitBondFutureSecurity (this);
  }

}