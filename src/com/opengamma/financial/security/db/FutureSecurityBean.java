/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

/**
 * 
 * @author Andrew
 */
public class FutureSecurityBean extends SecurityBean {
  
  private FutureType _futureType;
  private Date _expiry;
  private int _month;
  private int _year;
  private ExchangeBean _tradingExchange;
  private ExchangeBean _settlementExchange;
  
  public FutureSecurityBean () {
    super ();
  }
  
  public FutureSecurityBean (final FutureType futureType, final Date expiry, final int month, final int year, final ExchangeBean tradingExchange, final ExchangeBean settlementExchange) {
    this ();
    _futureType = futureType;
    _expiry = expiry;
    _month = month;
    _year = year;
    _tradingExchange = tradingExchange;
    _settlementExchange = settlementExchange;
  }
  
  /**
   * @return the future type
   */
  public FutureType getFutureType () {
    return _futureType;
  }

  /**
   * @return the expiry
   */
  public Date getExpiry() {
    return _expiry;
  }

  /**
   * @param expiry the expiry to set
   */
  public void setExpiry(Date expiry) {
    _expiry = expiry;
  }

  /**
   * @return the month
   */
  public int getMonth() {
    return _month;
  }

  /**
   * @param month the month to set
   */
  public void setMonth(int month) {
    _month = month;
  }

  /**
   * @return the year
   */
  public int getYear() {
    return _year;
  }

  /**
   * @param year the year to set
   */
  public void setYear(int year) {
    _year = year;
  }

  /**
   * @return the tradingExchange
   */
  public ExchangeBean getTradingExchange() {
    return _tradingExchange;
  }

  /**
   * @param tradingExchange the tradingExchange to set
   */
  public void setTradingExchange(ExchangeBean tradingExchange) {
    _tradingExchange = tradingExchange;
  }

  /**
   * @return the settlementExchange
   */
  public ExchangeBean getSettlementExchange() {
    return _settlementExchange;
  }

  /**
   * @param settlementExchange the settlementExchange to set
   */
  public void setSettlementExchange(ExchangeBean settlementExchange) {
    _settlementExchange = settlementExchange;
  }

  @Override
  public <T> T accept(SecurityBeanVisitor<T> visitor) {
    return visitor.visitFutureSecurityBean (this);
  }

}