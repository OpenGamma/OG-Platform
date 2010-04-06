/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.Currency;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author Andrew
 */
public class MetalFutureSecurity extends CommodityFutureSecurity {
  private DomainSpecificIdentifier _underlyingIdentifier;
  
  public MetalFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency, final String type, final Double unitNumber, final String unitName, DomainSpecificIdentifier underlyingIdentifier) {
    super (expiry, tradingExchange, settlementExchange, currency, type, unitNumber, unitName);
    _underlyingIdentifier = underlyingIdentifier;
  }
  
  public MetalFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency, final String type, DomainSpecificIdentifier underlyingIdentifier) {
    super (expiry, tradingExchange, settlementExchange, currency, type);
    _underlyingIdentifier = underlyingIdentifier;
  }

  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitMetalFutureSecurity (this);
  }

  public DomainSpecificIdentifier getUnderlyingIdentityKey() {
    return _underlyingIdentifier;
  }
}