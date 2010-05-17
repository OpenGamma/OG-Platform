/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import com.opengamma.livedata.LiveDataValueUpdateBean;


/**
 * A market data sender sends market data messages to external systems.
 *
 * @author kirk
 */
public interface MarketDataSender {

  void sendMarketData(LiveDataValueUpdateBean data);
  
  MarketDataDistributor getDistributor();

}
