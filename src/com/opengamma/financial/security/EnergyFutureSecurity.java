/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author Andrew
 */
public class EnergyFutureSecurity extends CommodityFutureSecurity {
  private final DomainSpecificIdentifier _underlyingIdentifier;
  
  public EnergyFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final String type, final Double unitNumber, final String unitName, DomainSpecificIdentifier underlyingIdentifier) {
    super (expiry, tradingExchange, settlementExchange, type, unitNumber, unitName);
    _underlyingIdentifier = underlyingIdentifier;
  }
  
  public EnergyFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final String type, DomainSpecificIdentifier underlyingIdentifier) {    
    super (expiry, tradingExchange, settlementExchange, type);
    _underlyingIdentifier = underlyingIdentifier;
  }

  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitEnergyFutureSecurity (this);
  }
  
  public DomainSpecificIdentifier getUnderlyingIdentityKey() {
    return _underlyingIdentifier;
  }
  
}