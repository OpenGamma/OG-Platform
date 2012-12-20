/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import com.opengamma.livedata.LiveDataValueUpdateBean;

/**
 * Sends market data messages to external systems.
 */
public interface MarketDataSender {

  /**
   * Sends the specified live data.
   * 
   * @param data  the live data to send, not null
   */
  void sendMarketData(LiveDataValueUpdateBean data);

  /**
   * Gets the distributor of the data.
   * 
   * @return the distributor, not null
   */
  MarketDataDistributor getDistributor();

}
