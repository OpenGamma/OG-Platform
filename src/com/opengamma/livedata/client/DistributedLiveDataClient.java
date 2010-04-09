/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
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
  private final FudgeRequestSender _subscriptionRequestSender;
  
  public DistributedLiveDataClient(FudgeRequestSender subscriptionRequestSender) {
    this(subscriptionRequestSender, new FudgeContext());
  }

  public DistributedLiveDataClient(FudgeRequestSender subscriptionRequestSender, FudgeContext fudgeContext) {
    ArgumentChecker.checkNotNull(subscriptionRequestSender, "Subscription request sender");
    ArgumentChecker.checkNotNull(fudgeContext, "Fudge Context");
    _subscriptionRequestSender = subscriptionRequestSender;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the subscriptionRequestSender
   */
  public FudgeRequestSender getSubscriptionRequestSender() {
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
  protected void handleSubscriptionRequest(Collection<SubscriptionHandle> subHandles) {
    ArgumentChecker.checkNotNull(subHandles, "Subscription Handles");
    FudgeFieldContainer requestMessage = composeRequestMessage(subHandles);
    getSubscriptionRequestSender().sendRequest(requestMessage, new SubscriptionResponseReceiver(subHandles));
  }
  
  private class SubscriptionResponseReceiver implements FudgeMessageReceiver {
    private final Map<LiveDataSpecification, SubscriptionHandle> _spec2SubHandle;
    
    public SubscriptionResponseReceiver(Collection<SubscriptionHandle> subHandles) {
      _spec2SubHandle = new HashMap<LiveDataSpecification, SubscriptionHandle>();
      for (SubscriptionHandle subHandle : subHandles) {
        _spec2SubHandle.put(subHandle.getRequestedSpecification(), subHandle);        
      }
    }

    @Override
    public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope envelope) {
      if((envelope == null) || (envelope.getMessage() == null)) {
        s_logger.warn("Got a message that can't be deserialized from a Fudge message.");
      }
      FudgeFieldContainer msg = envelope.getMessage();
      LiveDataSubscriptionResponseMsg responseMessage = LiveDataSubscriptionResponseMsg.fromFudgeMsg(new FudgeDeserializationContext(getFudgeContext()), msg);
      
      for (LiveDataSubscriptionResponse response : responseMessage.getResponses()) {
        
        SubscriptionHandle handle = _spec2SubHandle.get(response.getRequestedSpecification());
        if (handle == null) {
          s_logger.error("Could not find request corresponding to response {}", response.getRequestedSpecification());
          continue;
        }
        
        switch(response.getSubscriptionResult()) {
        case NOT_AUTHORIZED:
        case NOT_PRESENT:
          s_logger.info("Failed to establish subscription to {}. Result was {}", handle.getRequestedSpecification(), response.getSubscriptionResult());
          subscriptionRequestFailed(handle, response);
          break;
        case SUCCESS:
          s_logger.info("Established subscription to {}", handle.getRequestedSpecification());
          subscriptionRequestSatisfied(handle, response);
          if (handle.getSubscriptionType() != SubscriptionType.SNAPSHOT) {
            startReceivingTicks(response.getTickDistributionSpecification());
          }
          break;
        }
      }
    }
    
  }

  /**
   * @param subHandle
   * @return
   */
  protected FudgeFieldContainer composeRequestMessage(Collection<SubscriptionHandle> subHandles) {
    LiveDataSubscriptionRequest subReqMessage = composeSubscriptionRequestMessage(subHandles);
    FudgeFieldContainer requestMessage = subReqMessage.toFudgeMsg(new FudgeSerializationContext(getFudgeContext()));
    return requestMessage;
  }
  
  /**
   * @param tickDistributionSpecification
   */
  public void startReceivingTicks(String tickDistributionSpecification) {
    // Default no-op.
  }

  protected LiveDataSubscriptionRequest composeSubscriptionRequestMessage(Collection<SubscriptionHandle> subHandles) {
    String username = null;
    SubscriptionType type = null;
    
    ArrayList<LiveDataSpecification> specs = new ArrayList<LiveDataSpecification>();
    for (SubscriptionHandle subHandle : subHandles) {
      specs.add(new LiveDataSpecification(subHandle.getRequestedSpecification()));
      
      if (username == null) {
        username = subHandle.getUserName();
      } else if (!username.equals(subHandle.getUserName())) {
        throw new OpenGammaRuntimeException("Not all usernames are equal");        
      }
      
      if (type == null) {
        type = subHandle.getSubscriptionType();
      } else if (!type.equals(subHandle.getSubscriptionType())) {
        throw new OpenGammaRuntimeException("Not all subscription types are equal");
      }
    }
    
    LiveDataSubscriptionRequest request = new LiveDataSubscriptionRequest(username, type, specs);
    return request;
  }

  // REVIEW kirk 2009-10-28 -- This is just a braindead way of getting ticks to come in
  // until we can get a handle on the construction of receivers based on responses.
  @Override
  public void messageReceived(FudgeContext fudgeContext,
      FudgeMsgEnvelope msgEnvelope) {
    FudgeFieldContainer fudgeMsg = msgEnvelope.getMessage();
    LiveDataValueUpdateBean update = LiveDataValueUpdateBean.fromFudgeMsg(fudgeMsg);
    getValueDistributor().notifyListeners(update.getRelevantTimestamp(), update.getSpecification(), update.getFields());
  }

}
