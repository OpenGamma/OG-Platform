/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.model.SubscriptionRequestMessage;
import com.opengamma.livedata.model.SubscriptionResponseMessage;
import com.opengamma.transport.ByteArrayRequestReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class SubscriptionRequestReceiver implements ByteArrayRequestReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(SubscriptionRequestReceiver.class);
  private final AbstractLiveDataServer _liveDataServer;
  private final FudgeContext _fudgeContext;
  
  public SubscriptionRequestReceiver(AbstractLiveDataServer liveDataServer) {
    this(liveDataServer, new FudgeContext());
  }

  public SubscriptionRequestReceiver(AbstractLiveDataServer liveDataServer, FudgeContext fudgeContext) {
    ArgumentChecker.checkNotNull(liveDataServer, "Live Data Server");
    ArgumentChecker.checkNotNull(fudgeContext, "Fudge Context");
    _liveDataServer = liveDataServer;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the liveDataServer
   */
  public AbstractLiveDataServer getLiveDataServer() {
    return _liveDataServer;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public byte[] requestReceived(byte[] message) {
    FudgeMsgEnvelope msgEnvelope = getFudgeContext().deserialize(message);
    FudgeMsg requestFudgeMsg = msgEnvelope.getMessage();
    SubscriptionRequestMessage subscriptionRequest = SubscriptionRequestMessage.fromFudgeMsg(requestFudgeMsg);
    s_logger.debug("Received subscription request for {} on behalf of {}", subscriptionRequest.getSpecification(), subscriptionRequest.getUserName());
    SubscriptionResponseMessage subscriptionResponse = getLiveDataServer().subscriptionRequestMade(subscriptionRequest);
    FudgeMsg responseFudgeMsg = subscriptionResponse.toFudgeMsg(getFudgeContext());
    byte[] responseBytes = getFudgeContext().toByteArray(responseFudgeMsg);
    return responseBytes;
  }

}
