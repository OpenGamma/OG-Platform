/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives market data subscription requests
 * from clients. 
 */
public class SubscriptionRequestReceiver implements FudgeRequestReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(SubscriptionRequestReceiver.class);
  private final AbstractLiveDataServer _liveDataServer;
  
  public SubscriptionRequestReceiver(AbstractLiveDataServer liveDataServer) {
    ArgumentChecker.notNull(liveDataServer, "Live Data Server");
    _liveDataServer = liveDataServer;
  }

  /**
   * @return the liveDataServer
   */
  public AbstractLiveDataServer getLiveDataServer() {
    return _liveDataServer;
  }

  @Override
  @Transactional
  public FudgeFieldContainer requestReceived(
      FudgeDeserializationContext context,
      FudgeMsgEnvelope requestEnvelope) {
    try {
      FudgeFieldContainer requestFudgeMsg = requestEnvelope.getMessage();
      LiveDataSubscriptionRequest subscriptionRequest = LiveDataSubscriptionRequest.fromFudgeMsg(context, requestFudgeMsg);
      s_logger.info("Received subscription request {}", subscriptionRequest);
      LiveDataSubscriptionResponseMsg subscriptionResponse = getLiveDataServer().subscriptionRequestMade(subscriptionRequest);
      s_logger.info("Sending subscription response {}", subscriptionResponse);
      FudgeFieldContainer responseFudgeMsg = subscriptionResponse.toFudgeMsg(new FudgeSerializationContext(context.getFudgeContext()));
      return responseFudgeMsg;
    } catch (RuntimeException e) {
      s_logger.error("Unexpected exception when processing subscription request", e);
      throw e;      
    }
  }

}
