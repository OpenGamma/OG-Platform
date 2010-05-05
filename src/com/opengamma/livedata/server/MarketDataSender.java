/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeFieldContainer;

/**
 * A market data sender sends market data messages to external systems.
 *
 * @author kirk
 */
public interface MarketDataSender {

  void sendMarketData(MarketDataDistributor distributor, FudgeFieldContainer normalizedMarketDataMsg);
}
