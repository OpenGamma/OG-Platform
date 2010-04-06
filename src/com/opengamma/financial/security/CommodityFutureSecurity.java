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
public abstract class CommodityFutureSecurity extends FutureSecurity {
  
  private final String _commodityType;
  private final Double _unitNumber;
  private final String _unitName;
  
  public CommodityFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency, final String type, final Double unitNumber, final String unitName) {
    super (expiry, tradingExchange, settlementExchange, currency);
    _commodityType = type;
    _unitNumber = unitNumber;
    _unitName = unitName;
  }
  
  public CommodityFutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency, final String type) {
    this (expiry, tradingExchange, settlementExchange, currency, type, null, null);
  }
  
  public String getCommodityType () {
    return _commodityType;
  }
  
  public Double getUnitNumber () {
    return _unitNumber;
  }
  
  public String getUnitName () {
    return _unitName;
  }
  
}