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
public abstract class FutureSecurity extends FinancialSecurity {
  
  public static final String FUTURE_TYPE = "FUTURE";
  
  private final Expiry _expiry;
  private final int _month;
  private final int _year;
  private final String _tradingExchange;
  private final String _settlementExchange;
  
  public FutureSecurity (final Expiry expiry, final int month, final int year, final String tradingExchange, final String settlementExchange) {
    _expiry = expiry;
    _month = month;
    _year = year;
    _tradingExchange = tradingExchange;
    _settlementExchange = settlementExchange;
    setSecurityType (FUTURE_TYPE);
  }

  /**
   * @return the expiry
   */
  public Expiry getExpiry() {
    return _expiry;
  }

  /**
   * @return the month
   */
  public int getMonth() {
    return _month;
  }

  /**
   * @return the year
   */
  public int getYear() {
    return _year;
  }

  /**
   * @return the tradingExchange
   */
  public String getTradingExchange() {
    return _tradingExchange;
  }

  /**
   * @return the settlementExchange
   */
  public String getSettlementExchange() {
    return _settlementExchange;
  }
  
}