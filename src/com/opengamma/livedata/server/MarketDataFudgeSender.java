/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Sends Fudge-encoded market data.
 *
 * @author kirk
 */
public class MarketDataFudgeSender implements MarketDataSender {
  private final FudgeMessageSender _fudgeMessageSender;
  
  public MarketDataFudgeSender(FudgeMessageSender fudgeMessageSender) {
    ArgumentChecker.notNull(fudgeMessageSender, "Fudge Message Sender");
    _fudgeMessageSender = fudgeMessageSender;
  }

  /**
   * @return the fudgeMessageSender
   */
  public FudgeMessageSender getFudgeMessageSender() {
    return _fudgeMessageSender;
  }

  @Override
  public void sendMarketData(MarketDataDistributor distributor, FudgeFieldContainer normalizedMsg) {
    DistributionSpecification distributionSpec = distributor.getDistributionSpec();
    
    LiveDataValueUpdateBean liveDataValueUpdateBean = new LiveDataValueUpdateBean(
        distributor.getNumMessagesSent(), // 0-based as it should be 
        distributionSpec.getFullyQualifiedLiveDataSpecification(), 
        normalizedMsg);

    FudgeFieldContainer fudgeMsg = liveDataValueUpdateBean.toFudgeMsg(getFudgeMessageSender().getFudgeContext());
    getFudgeMessageSender().send(fudgeMsg);
  }

}
