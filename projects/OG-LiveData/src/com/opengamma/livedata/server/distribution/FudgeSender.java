/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple implementation of {@link MarketDataSender} that sends
 * market data to a {@link FudgeMessageSender}. 
 * Useful in tests. 
 */
public class FudgeSender implements MarketDataSender {
  private final FudgeMessageSender _fudgeMessageSender;
  private final MarketDataDistributor _distributor;
  
  public FudgeSender(FudgeMessageSender fudgeMessageSender, MarketDataDistributor distributor) {
    ArgumentChecker.notNull(fudgeMessageSender, "Fudge Message Sender");
    ArgumentChecker.notNull(distributor, "Market Data Distributor");
    _fudgeMessageSender = fudgeMessageSender;
    _distributor = distributor;
  }

  public FudgeMessageSender getFudgeMessageSender() {
    return _fudgeMessageSender;
  }
  
  @Override
  public MarketDataDistributor getDistributor() {
    return _distributor;
  }

  @Override
  public void sendMarketData(LiveDataValueUpdateBean data) {
    FudgeFieldContainer fudgeMsg = data.toFudgeMsg(getFudgeMessageSender().getFudgeContext());
    getFudgeMessageSender().send(fudgeMsg);
  }

}
