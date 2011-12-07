/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives market data subscription requests from clients. 
 */
public class SubscriptionRequestReceiver implements FudgeRequestReceiver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SubscriptionRequestReceiver.class);

  /**
   * The underlying server.
   */
  private final AbstractLiveDataServer _liveDataServer;

  /**
   * Creates an instance wrapping an underlying server.
   * 
   * @param liveDataServer  the server, not null
   */
  public SubscriptionRequestReceiver(AbstractLiveDataServer liveDataServer) {
    ArgumentChecker.notNull(liveDataServer, "liveDataServer");
    _liveDataServer = liveDataServer;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying server.
   * 
   * @return the server, not null
   */
  public AbstractLiveDataServer getLiveDataServer() {
    return _liveDataServer;
  }

  //-------------------------------------------------------------------------
  @Override
  @Transactional
  public FudgeMsg requestReceived(FudgeDeserializer deserializer, FudgeMsgEnvelope requestEnvelope) {
    try {
      FudgeMsg requestFudgeMsg = requestEnvelope.getMessage();
      LiveDataSubscriptionRequest subscriptionRequest = LiveDataSubscriptionRequest.fromFudgeMsg(deserializer, requestFudgeMsg);
      s_logger.debug("Received subscription request {}", subscriptionRequest);
      LiveDataSubscriptionResponseMsg subscriptionResponse = getLiveDataServer().subscriptionRequestMade(subscriptionRequest);
      s_logger.debug("Sending subscription response {}", subscriptionResponse);
      FudgeMsg responseFudgeMsg = subscriptionResponse.toFudgeMsg(new FudgeSerializer(deserializer.getFudgeContext()));
      return responseFudgeMsg;
    } catch (RuntimeException e) {
      s_logger.error("Unexpected exception when processing subscription request", e);
      throw e;      
    }
  }

}
