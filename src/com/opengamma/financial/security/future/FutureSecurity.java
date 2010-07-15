/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import javax.time.calendar.MonthOfYear;

import com.opengamma.financial.Currency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.util.time.Expiry;

/**
 * A {@code Security} used to model futures.
 */
public abstract class FutureSecurity extends FinancialSecurity {

  /**
   * 
   */
  protected static final String EXPIRY_KEY = "expiry";
  /**
   * 
   */
  protected static final String TRADINGEXCHANGE_KEY = "tradingExchange";
  /**
   * 
   */
  protected static final String SETTLEMENTEXCHANGE_KEY = "settlementExchange";
  /**
   * 
   */
  protected static final String CURRENCY_KEY = "currency";

  /**
   * The security type of equity.
   */
  public static final String FUTURE_TYPE = "FUTURE";

  /**
   * The expiry of the security.
   */
  private final Expiry _expiry;
  /**
   * The trading exchange.
   */
  private final String _tradingExchange;
  /**
   * The settlement exchange.
   */
  private final String _settlementExchange;
  /**
   * The currency.
   */
  private final Currency _currency;

  /**
   * Creates a future security.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the trading exchange
   * @param settlementExchange  the settlement exchange
   * @param currency  the currency
   */
  public FutureSecurity(final Expiry expiry, final String tradingExchange, final String settlementExchange, final Currency currency) {
    super(FUTURE_TYPE);
    _expiry = expiry;
    _tradingExchange = tradingExchange;
    _settlementExchange = settlementExchange;
    _currency = currency;
  }

  //-------------------------------------------------------------------------
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
    return getExpiry().getExpiry().getMonthOfYear();
  }

  /**
   * @return the year
   */
  public int getYear() {
    return getExpiry().getExpiry().getYear();
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

  /**
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  //-------------------------------------------------------------------------
  public abstract <T> T accept(FutureSecurityVisitor<T> visitor);

  public final <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitFutureSecurity(this);
  }

}
