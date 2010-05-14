/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.datasender;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.livedata.server.MarketDataDistributor;

/**
 * A market data sender sends market data messages to external systems.
 *
 * @author kirk
 */
public interface MarketDataSender {

  void sendMarketData(MarketDataDistributor distributor, FudgeFieldContainer normalizedMarketDataMsg);
}
