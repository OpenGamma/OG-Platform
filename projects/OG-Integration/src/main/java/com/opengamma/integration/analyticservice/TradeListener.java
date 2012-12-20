/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;

import com.opengamma.core.position.Trade;

/**
 * Listener for trades 
 */
public interface TradeListener {
  
  /**
   * Call back to process received trades
   * 
   * @param trade the trade, not null
   */
  void tradeReceived(Trade trade);

}
