/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author emcleod
 */
public class StockFutureSecurity extends FutureSecurity {
  private final DomainSpecificIdentifier _underlyingIdentifier;

  public StockFutureSecurity(final Expiry expiry, final String tradingExchange, final String settlementExchange, final DomainSpecificIdentifier underlyingIdentifier) {
    super(expiry, tradingExchange, settlementExchange);
    _underlyingIdentifier = underlyingIdentifier;
  }

  public DomainSpecificIdentifier getUnderlyingIdentityKey() {
    return _underlyingIdentifier;
  }

  @Override
  public <T> T accept(final FutureSecurityVisitor<T> visitor) {
    return visitor.visitStockFutureSecurity(this);
  }
}
