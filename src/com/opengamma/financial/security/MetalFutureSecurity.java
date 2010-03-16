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
public class MetalFutureSecurity extends CommodityFutureSecurity {
  
  public MetalFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final String type, final Double unitNumber, final String unitName) {
    super (expiry, tradingExchange, settlementExchange, type, unitNumber, unitName);
  }
  
  public MetalFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final String type) {
    super (expiry, tradingExchange, settlementExchange, type);
  }

  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitMetalFutureSecurity (this);
  }
  
}