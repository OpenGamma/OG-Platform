/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.model.SubscriptionRequestMessage;
import com.opengamma.livedata.model.SubscriptionResponseMessage;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class SubscriptionRequestReceiver implements FudgeRequestReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(SubscriptionRequestReceiver.class);
  private final AbstractLiveDataServer _liveDataServer;
  
  public SubscriptionRequestReceiver(AbstractLiveDataServer liveDataServer) {
    ArgumentChecker.checkNotNull(liveDataServer, "Live Data Server");
    _liveDataServer = liveDataServer;
  }

  /**
   * @return the liveDataServer
   */
  public AbstractLiveDataServer getLiveDataServer() {
    return _liveDataServer;
  }

  @Override
  public FudgeFieldContainer requestReceived(
      FudgeContext fudgeContext,
      FudgeMsgEnvelope requestEnvelope) {
    FudgeFieldContainer requestFudgeMsg = requestEnvelope.getMessage();
    SubscriptionRequestMessage subscriptionRequest = SubscriptionRequestMessage.fromFudgeMsg(requestFudgeMsg);
    s_logger.debug("Received subscription request for {} on behalf of {}", subscriptionRequest.getSpecification(), subscriptionRequest.getUserName());
    SubscriptionResponseMessage subscriptionResponse = getLiveDataServer().subscriptionRequestMade(subscriptionRequest);
    FudgeFieldContainer responseFudgeMsg = subscriptionResponse.toFudgeMsg(fudgeContext);
    return responseFudgeMsg;
  }

}
