/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;


/**
 * Producers of Trades to process
 */
public interface TradeProducer {

  /**
   * Add a trade listener
   * 
   * @param tradeListener the listener, not null
   */
  void addTradeListener(TradeListener tradeListener);
  
  /**
   * Remove a trade listener
   * 
   * @param tradeListener the listener, not null
   */
  void removeTradeListener(TradeListener tradeListener);
}
