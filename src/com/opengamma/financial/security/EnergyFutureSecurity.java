/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author Andrew
 */
public class EnergyFutureSecurity extends CommodityFutureSecurity {
  private final Identifier _underlyingIdentifier;
  
  public EnergyFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency, final String type, final Double unitNumber, final String unitName, Identifier underlyingIdentifier) {
    super (expiry, tradingExchange, settlementExchange, currency, type, unitNumber, unitName);
    _underlyingIdentifier = underlyingIdentifier;
  }
  
  public EnergyFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency, final String type, Identifier underlyingIdentifier) {
    super (expiry, tradingExchange, settlementExchange, currency, type);
    _underlyingIdentifier = underlyingIdentifier;
  }

  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitEnergyFutureSecurity (this);
  }
  
  public Identifier getUnderlyingIdentityKey() {
    return _underlyingIdentifier;
  }
  
}