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
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
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
  
  private final long TIMEOUT = 30000;
  
  public DistributedLiveDataClient(FudgeRequestSender subscriptionRequestSender) {
    this(subscriptionRequestSender, new FudgeContext());
  }

  public DistributedLiveDataClient(FudgeRequestSender subscriptionRequestSender, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(subscriptionRequestSender, "Subscription request sender");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
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
    ArgumentChecker.notEmpty(subHandles, "Subscription handle collection");
    
    // Determine common username and subscription type
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
    
    // Build request message
    LiveDataSubscriptionRequest subReqMessage = new LiveDataSubscriptionRequest(username, type, specs);
    FudgeFieldContainer requestMessage = subReqMessage.toFudgeMsg(new FudgeSerializationContext(getFudgeContext()));
    
    // Build response receiver
    FudgeMessageReceiver responseReceiver;
    if (type == SubscriptionType.SNAPSHOT) {
      responseReceiver = new SnapshotResponseReceiver(subHandles);
    } else {
      responseReceiver = new TopicBasedSubscriptionResponseReceiver(subHandles);
    }
    
    getSubscriptionRequestSender().sendRequest(requestMessage, responseReceiver);
  }
  
  /**
   * Common functionality for receiving subscription responses from the server. 
   */
  private abstract class AbstractSubscriptionResponseReceiver implements FudgeMessageReceiver {
    
    protected final Map<LiveDataSpecification, SubscriptionHandle> _spec2SubHandle;
    
    protected final Map<SubscriptionHandle, LiveDataSubscriptionResponse> _successResponses = new HashMap<SubscriptionHandle, LiveDataSubscriptionResponse>();
    protected final Map<SubscriptionHandle, LiveDataSubscriptionResponse> _failedResponses = new HashMap<SubscriptionHandle, LiveDataSubscriptionResponse>();
    
    protected String _userName = null;
    
    public AbstractSubscriptionResponseReceiver(Collection<SubscriptionHandle> subHandles) {
      _spec2SubHandle = new HashMap<LiveDataSpecification, SubscriptionHandle>();
      for (SubscriptionHandle subHandle : subHandles) {
        _spec2SubHandle.put(subHandle.getRequestedSpecification(), subHandle);        
      }
    }

    
    @Override
    public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope envelope) {
      try {
        
        if((envelope == null) || (envelope.getMessage() == null)) {
          throw new OpenGammaRuntimeException("Got a message that can't be deserialized from a Fudge message.");
        }
        FudgeFieldContainer msg = envelope.getMessage();
        
        LiveDataSubscriptionResponseMsg responseMessage = LiveDataSubscriptionResponseMsg.fromFudgeMsg(new FudgeDeserializationContext(getFudgeContext()), msg);
        if (responseMessage.getResponses().isEmpty()) {
          throw new OpenGammaRuntimeException("Got empty subscription response " + responseMessage);
        }
        
        messageReceived(responseMessage);
      
      } catch (Exception e) {
        s_logger.error("Failed to process response message", e);
        
        for (SubscriptionHandle handle : _spec2SubHandle.values()) {
          if (handle.getSubscriptionType() != SubscriptionType.SNAPSHOT) {
            subscriptionRequestFailed(handle, new LiveDataSubscriptionResponse(
                handle.getRequestedSpecification(), 
                LiveDataSubscriptionResult.INTERNAL_ERROR, 
                e.getMessage(),
                null,
                null,
                null));          
          }
        }
      }
    }
    
    
    private void messageReceived(LiveDataSubscriptionResponseMsg responseMessage) {
      parseResponse(responseMessage);
      processResponse();
      sendResponse();
    }

    
    private void parseResponse(LiveDataSubscriptionResponseMsg responseMessage) {
      for (LiveDataSubscriptionResponse response : responseMessage.getResponses()) {
        
        SubscriptionHandle handle = _spec2SubHandle.get(response.getRequestedSpecification());
        if (handle == null) {
          throw new OpenGammaRuntimeException("Could not find handle corresponding to request " + response.getRequestedSpecification());
        }
        
        if (_userName != null && !_userName.equals(handle.getUserName())) {
          throw new OpenGammaRuntimeException("Not all usernames are equal");
        }
        _userName = handle.getUserName();
        
        if (response.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
          _successResponses.put(handle, response);
        } else {
          _failedResponses.put(handle, response);
        }
      }
    }

    
    protected void processResponse() {
    }

    
    protected void sendResponse() {
      
      Map<SubscriptionHandle, LiveDataSubscriptionResponse> responses = new HashMap<SubscriptionHandle, LiveDataSubscriptionResponse>(); 
      responses.putAll(_successResponses);
      responses.putAll(_failedResponses);
      
      for (Map.Entry<SubscriptionHandle, LiveDataSubscriptionResponse> successEntry : responses.entrySet()) {
        SubscriptionHandle handle = successEntry.getKey();
        LiveDataSubscriptionResponse response = successEntry.getValue();
        handle.subscriptionResultReceived(response);
      }

    }
  }
  
  /**
   * Some market data requests are snapshot requests; this means that they do not require a JMS subscription. 
   */
  private class SnapshotResponseReceiver extends AbstractSubscriptionResponseReceiver {
    
    public SnapshotResponseReceiver(Collection<SubscriptionHandle> subHandles) {
      super(subHandles);
    }
    
  }
  
  /**
   * Some market data requests are non-snapshot requests where market data is continuously read from a JMS topic;
   * this means they require a JMS subscription.
   * <p>
   * As per LIV-19, after we've subscribed to the market data (and started getting deltas), we do a snapshot
   * to make sure we get a full initial image of the data. Things are done in this order (first subscribe, then snapshot)
   * so we don't lose any ticks. See LIV-19.
   */
  private class TopicBasedSubscriptionResponseReceiver extends AbstractSubscriptionResponseReceiver {
    
    public TopicBasedSubscriptionResponseReceiver(Collection<SubscriptionHandle> subHandles) {
      super(subHandles);
    }
    
    @Override
    protected void processResponse() {
      try {
        // Phase 1. Create a subscription to market data topic
        startReceivingTicks();
      
        // Phase 2. After we've subscribed to the market data (and started getting deltas), snapshot it
        snapshot();

      } catch (RuntimeException e) {
        s_logger.error("Failed to process subscription response", e);
        
        // This is unexpected. Fail everything.
        for (LiveDataSubscriptionResponse response : _successResponses.values()) {
          response.setSubscriptionResult(LiveDataSubscriptionResult.INTERNAL_ERROR);          
          response.setUserMessage(e.getMessage());
        }
        
        _failedResponses.putAll(_successResponses);
        _successResponses.clear();
      }
    }
  
    private void startReceivingTicks() {
      for (Map.Entry<SubscriptionHandle, LiveDataSubscriptionResponse> entry : _successResponses.entrySet()) {
        DistributedLiveDataClient.this.subscriptionStartingToReceiveTicks(entry.getKey(), entry.getValue());
        DistributedLiveDataClient.this.startReceivingTicks(entry.getValue().getTickDistributionSpecification());
      }
    }
    
    private void snapshot() {
      
      ArrayList<LiveDataSpecification> successLiveDataSpecs = new ArrayList<LiveDataSpecification>();
      for (LiveDataSubscriptionResponse response : _successResponses.values()) {
        successLiveDataSpecs.add(response.getRequestedSpecification());                
      }
      
      Collection<LiveDataSubscriptionResponse> snapshots = DistributedLiveDataClient.this.snapshot(_userName, successLiveDataSpecs, TIMEOUT);
        
      for (LiveDataSubscriptionResponse response : snapshots) {
        
        SubscriptionHandle handle = _spec2SubHandle.get(response.getRequestedSpecification());
        if (handle == null) {
          throw new OpenGammaRuntimeException("Could not find handle corresponding to request " + response.getRequestedSpecification());
        }
        
        // could be that even though subscription to the JMS topic (phase 1) succeeded, snapshot (phase 2) for some reason failed.
        // since phase 1 already validated everything, this should mainly happen when user permissions are modified 
        // in the sub-second interval between phases 1 and 2!
        if (response.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
          
          handle.addSnapshotOnHold(response.getSnapshot());
          
        } else {
          _successResponses.remove(handle);
          _failedResponses.put(handle, response);
        }
      }
    }
    
    @Override
    protected void sendResponse() {
      super.sendResponse();
      
      for (Map.Entry<SubscriptionHandle, LiveDataSubscriptionResponse> successEntry : _successResponses.entrySet()) {
        SubscriptionHandle handle = successEntry.getKey();
        LiveDataSubscriptionResponse response = successEntry.getValue();
        subscriptionRequestSatisfied(handle, response);
      }
      
      for (Map.Entry<SubscriptionHandle, LiveDataSubscriptionResponse> failedEntry : _failedResponses.entrySet()) {
        SubscriptionHandle handle = failedEntry.getKey();
        LiveDataSubscriptionResponse response = failedEntry.getValue();
        subscriptionRequestFailed(handle, response);
        
        // this is here just to clean up. It's safe to call stopReceivingTicks()
        // even if no JMS subscription actually exists.
        stopReceivingTicks(response.getTickDistributionSpecification());
      }
    }
    
  }
  
  /**
   * @param tickDistributionSpecification
   */
  public void startReceivingTicks(String tickDistributionSpecification) {
    // Default no-op.
  }
  
  public void stopReceivingTicks(String tickDistributionSpecification) {
    // Default no-op.
  }

  // REVIEW kirk 2009-10-28 -- This is just a braindead way of getting ticks to come in
  // until we can get a handle on the construction of receivers based on responses.
  @Override
  public void messageReceived(FudgeContext fudgeContext,
      FudgeMsgEnvelope msgEnvelope) {
    FudgeFieldContainer fudgeMsg = msgEnvelope.getMessage();
    LiveDataValueUpdateBean update = LiveDataValueUpdateBean.fromFudgeMsg(fudgeMsg);
    valueUpdate(update);
  }

}
