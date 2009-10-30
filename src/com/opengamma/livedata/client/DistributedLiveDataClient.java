/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.model.SubscriptionRequestMessage;
import com.opengamma.livedata.model.SubscriptionResponseMessage;
import com.opengamma.transport.ByteArrayMessageReceiver;
import com.opengamma.transport.ByteArrayRequestSender;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class DistributedLiveDataClient extends AbstractLiveDataClient implements FudgeMessageReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(DistributedLiveDataClient.class);
  // Injected Inputs:
  private final FudgeContext _fudgeContext;
  private final ByteArrayRequestSender _subscriptionRequestSender;
  
  public DistributedLiveDataClient(ByteArrayRequestSender subscriptionRequestSender) {
    this(subscriptionRequestSender, new FudgeContext());
  }

  public DistributedLiveDataClient(ByteArrayRequestSender subscriptionRequestSender, FudgeContext fudgeContext) {
    ArgumentChecker.checkNotNull(subscriptionRequestSender, "Subscription request sender");
    ArgumentChecker.checkNotNull(fudgeContext, "Fudge Context");
    _subscriptionRequestSender = subscriptionRequestSender;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the subscriptionRequestSender
   */
  public ByteArrayRequestSender getSubscriptionRequestSender() {
    return _subscriptionRequestSender;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  protected void cancelPublication(LiveDataSpecification fullyQualifiedSpecification) {
    s_logger.info("Request made to cancel publication of {}", fullyQualifiedSpecification);
    // TODO kirk 2009-10-28 -- This should handle an unsubscription request. For now,
    // however, we can just make do with allowing the heartbeat to time out.
  }

  @Override
  protected void handleSubscriptionRequest(SubscriptionHandle subHandle) {
    ArgumentChecker.checkNotNull(subHandle, "Subscription Handle");
    byte[] requestMessage = composeRequestMessage(subHandle);
    s_logger.info("Sending request to {} on behalf of {}", subHandle.getFullyQualifiedSpecification(), subHandle.getUserName());
    getSubscriptionRequestSender().sendRequest(requestMessage, new SubscriptionResponseReceiver(subHandle));
  }
  
  private class SubscriptionResponseReceiver implements ByteArrayMessageReceiver {
    private final SubscriptionHandle _subHandle;
    
    public SubscriptionResponseReceiver(SubscriptionHandle subHandle) {
      _subHandle = subHandle;
    }

    @Override
    public void messageReceived(byte[] message) {
      FudgeMsgEnvelope envelope = getFudgeContext().deserialize(message);
      if((envelope == null) || (envelope.getMessage() == null)) {
        s_logger.warn("Got a message that can't be deserialized from a Fudge message.");
      }
      FudgeMsg msg = envelope.getMessage();
      SubscriptionResponseMessage responseMessage = SubscriptionResponseMessage.fromFudgeMsg(msg);
      switch(responseMessage.getSubscriptionResult()) {
      case NOT_AUTHORIZED:
      case NOT_PRESENT:
        s_logger.info("Failed to establish subscription to {}. Result was {}", _subHandle.getFullyQualifiedSpecification(), responseMessage.getSubscriptionResult());
        subscriptionRequestFailed(_subHandle, responseMessage.getSubscriptionResult(), responseMessage.getUserMessage());
        break;
      case SUCCESS:
        s_logger.info("Established subscription to {}", _subHandle.getFullyQualifiedSpecification());
        subscriptionRequestSatisfied(_subHandle);
        break;
      }
    }
    
  }

  /**
   * @param subHandle
   * @return
   */
  protected byte[] composeRequestMessage(SubscriptionHandle subHandle) {
    SubscriptionRequestMessage subReqMessage = composeSubscriptionRequestMessage(subHandle);
    FudgeMsg requestMessage = subReqMessage.toFudgeMsg(getFudgeContext());
    byte[] bytes = getFudgeContext().toByteArray(requestMessage);
    return bytes;
  }
  
  protected SubscriptionRequestMessage composeSubscriptionRequestMessage(SubscriptionHandle subHandle) {
    SubscriptionRequestMessage request = new SubscriptionRequestMessage();
    request.setUserName(subHandle.getUserName());
    request.setSpecification(subHandle.getFullyQualifiedSpecification());
    return request;
  }

  // REVIEW kirk 2009-10-28 -- This is just a braindead way of getting ticks to come in
  // until we can get a handle on the construction of receivers based on responses.
  @Override
  public void messageReceived(FudgeContext fudgeContext,
      FudgeMsgEnvelope msgEnvelope) {
    FudgeMsg fudgeMsg = msgEnvelope.getMessage();
    LiveDataValueUpdateBean update = LiveDataValueUpdateBean.fromFudgeMsg(fudgeMsg);
    getValueDistributor().notifyListeners(update.getRelevantTimestamp(), update.getSpecification(), update.getFields());
  }

}
