/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import javax.time.calendar.MonthOfYear;

import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author Andrew
 */
public abstract class FutureSecurity extends FinancialSecurity {
  
  public static final String FUTURE_TYPE = "FUTURE";
  
  private final Expiry _expiry;
  private final String _tradingExchange;
  private final String _settlementExchange;
  private final Currency _currency;
  
  public FutureSecurity (final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency) {
    _expiry = expiry;
    _tradingExchange = tradingExchange;
    _settlementExchange = settlementExchange;
    _currency = currency;
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
  public MonthOfYear getMonth() {
    return getExpiry ().getExpiry ().getMonthOfYear ();
  }

  /**
   * @return the year
   */
  public int getYear() {
    return getExpiry ().getExpiry ().getYear ();
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
  
  public Currency getCurrency () {
    return _currency;
  }
  
  public abstract <T> T accept (FutureSecurityVisitor<T> visitor);
  
  public final <T> T accept (FinancialSecurityVisitor<T> visitor) {
    return accept ((FutureSecurityVisitor<T>)visitor);
  }
  
}