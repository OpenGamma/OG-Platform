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
public class EnergyFutureSecurity extends CommodityFutureSecurity {
  
  public EnergyFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency, final String type, final Double unitNumber, final String unitName) {
    super (expiry, tradingExchange, settlementExchange, currency, type, unitNumber, unitName);
  }
  
  public EnergyFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency, final String type) {
    super (expiry, tradingExchange, settlementExchange, currency, type);
  }

  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitEnergyFutureSecurity (this);
  }
  
}