/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.LiveDataValueUpdateBeanFudgeBuilder;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A client that talks to a remote LiveData server through an unspecified protocol. Possibilities are JMS, Fudge, direct socket connection, and so on.
 */
@PublicAPI
public class DistributedLiveDataClient extends AbstractLiveDataClient implements FudgeMessageReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(DistributedLiveDataClient.class);
  // Injected Inputs:
  private final FudgeContext _fudgeContext;
  private final FudgeRequestSender _subscriptionRequestSender;

  private final DistributedEntitlementChecker _entitlementChecker;

  /**
   * An exception will be thrown when doing a snapshot if no reply is received from the server within this time. Milliseconds.
   */
  private static final long TIMEOUT = 1800000;

  public DistributedLiveDataClient(FudgeRequestSender subscriptionRequestSender, FudgeRequestSender entitlementRequestSender) {
    this(subscriptionRequestSender, entitlementRequestSender, OpenGammaFudgeContext.getInstance());
  }

  public DistributedLiveDataClient(FudgeRequestSender subscriptionRequestSender, FudgeRequestSender entitlementRequestSender, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(subscriptionRequestSender, "Subscription request sender");
    ArgumentChecker.notNull(entitlementRequestSender, "Entitlement request sender");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");

    _subscriptionRequestSender = subscriptionRequestSender;
    _fudgeContext = fudgeContext;

    _entitlementChecker = new DistributedEntitlementChecker(entitlementRequestSender, fudgeContext);
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
  @Override
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

    // Determine common user and subscription type
    UserPrincipal user = null;
    SubscriptionType type = null;

    List<LiveDataSpecification> specs = Lists.newArrayListWithCapacity(subHandles.size());
    for (SubscriptionHandle subHandle : subHandles) {

      specs.add(subHandle.getRequestedSpecification());

      if (user == null) {
        user = subHandle.getUser();
      } else if (!user.equals(subHandle.getUser())) {
        throw new OpenGammaRuntimeException("Not all usernames are equal");
      }

      if (type == null) {
        type = subHandle.getSubscriptionType();
      } else if (!type.equals(subHandle.getSubscriptionType())) {
        throw new OpenGammaRuntimeException("Not all subscription types are equal");
      }
    }

    // Build request message
    LiveDataSubscriptionRequest subReqMessage = new LiveDataSubscriptionRequest(user, type, specs);
    FudgeMsg requestMessage = subReqMessage.toFudgeMsg(new FudgeSerializer(getFudgeContext()));

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

    private final Map<LiveDataSpecification, SubscriptionHandle> _spec2SubHandle;

    private final Map<SubscriptionHandle, LiveDataSubscriptionResponse> _successResponses = new HashMap<>();
    private final Map<SubscriptionHandle, LiveDataSubscriptionResponse> _failedResponses = new HashMap<>();

    private UserPrincipal _user;

    public AbstractSubscriptionResponseReceiver(Collection<SubscriptionHandle> subHandles) {
      _spec2SubHandle = new HashMap<>();
      for (SubscriptionHandle subHandle : subHandles) {
        _spec2SubHandle.put(subHandle.getRequestedSpecification(), subHandle);
      }
    }

    public UserPrincipal getUser() {
      return _user;
    }

    public void setUser(UserPrincipal user) {
      _user = user;
    }

    public Map<LiveDataSpecification, SubscriptionHandle> getSpec2SubHandle() {
      return _spec2SubHandle;
    }

    public Map<SubscriptionHandle, LiveDataSubscriptionResponse> getSuccessResponses() {
      return _successResponses;
    }

    public Map<SubscriptionHandle, LiveDataSubscriptionResponse> getFailedResponses() {
      return _failedResponses;
    }

    @Override
    public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope envelope) {
      final PoolExecutor.CompletionListener<Void> callback = new PoolExecutor.CompletionListener<Void>() {

        @Override
        public void success(final Void result) {
          // No-op
        }

        @Override
        public void failure(final Throwable error) {
          s_logger.error("Failed to process response message", error);
          for (SubscriptionHandle handle : getSpec2SubHandle().values()) {
            if (handle.getSubscriptionType() != SubscriptionType.SNAPSHOT) {
              subscriptionRequestFailed(handle, new LiveDataSubscriptionResponse(handle.getRequestedSpecification(), LiveDataSubscriptionResult.INTERNAL_ERROR, error.toString(), null, null,
                  null));
            }
          }
        }

      };
      try {
        if ((envelope == null) || (envelope.getMessage() == null)) {
          throw new OpenGammaRuntimeException("Got a message that can't be deserialized from a Fudge message.");
        }
        FudgeMsg msg = envelope.getMessage();
        LiveDataSubscriptionResponseMsg responseMessage = LiveDataSubscriptionResponseMsg.fromFudgeMsg(new FudgeDeserializer(getFudgeContext()), msg);
        if (responseMessage.getResponses().isEmpty()) {
          throw new OpenGammaRuntimeException("Got empty subscription response " + responseMessage);
        }
        messageReceived(responseMessage, callback);
      } catch (Exception e) {
        callback.failure(e);
      }
    }

    private void messageReceived(LiveDataSubscriptionResponseMsg responseMessage, final PoolExecutor.CompletionListener<Void> callback) {
      parseResponse(responseMessage);
      processResponse(new PoolExecutor.CompletionListener<Void>() {

        @Override
        public void success(final Void result) {
          try {
            sendResponse();
          } catch (Throwable t) {
            callback.failure(t);
            return;
          }
          callback.success(null);
        }

        @Override
        public void failure(final Throwable error) {
          callback.failure(error);
        }

      });
    }

    private void parseResponse(LiveDataSubscriptionResponseMsg responseMessage) {
      for (LiveDataSubscriptionResponse response : responseMessage.getResponses()) {

        SubscriptionHandle handle = getSpec2SubHandle().get(response.getRequestedSpecification());
        if (handle == null) {
          throw new OpenGammaRuntimeException("Could not find handle corresponding to request " + response.getRequestedSpecification());
        }

        if (getUser() != null && !getUser().equals(handle.getUser())) {
          throw new OpenGammaRuntimeException("Not all usernames are equal");
        }
        setUser(handle.getUser());

        if (response.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
          getSuccessResponses().put(handle, response);
        } else {
          getFailedResponses().put(handle, response);
        }
      }
    }

    protected void processResponse(final PoolExecutor.CompletionListener<Void> callback) {
      callback.success(null);
    }

    protected void sendResponse() {

      Map<SubscriptionHandle, LiveDataSubscriptionResponse> responses = new HashMap<>();
      responses.putAll(getSuccessResponses());
      responses.putAll(getFailedResponses());

      int total = responses.size();
      s_logger.info("{} subscription responses received", total);
      Map<LiveDataListener, Collection<LiveDataSubscriptionResponse>> batch = new HashMap<>();
      for (Map.Entry<SubscriptionHandle, LiveDataSubscriptionResponse> successEntry : responses.entrySet()) {
        SubscriptionHandle handle = successEntry.getKey();
        LiveDataSubscriptionResponse response = successEntry.getValue();
        Collection<LiveDataSubscriptionResponse> responseBatch = batch.get(handle.getListener());
        if (responseBatch == null) {
          responseBatch = new LinkedList<>();
          batch.put(handle.getListener(), responseBatch);
        }
        responseBatch.add(response);
      }
      for (Map.Entry<LiveDataListener, Collection<LiveDataSubscriptionResponse>> batchEntry : batch.entrySet()) {
        batchEntry.getKey().subscriptionResultsReceived(batchEntry.getValue());
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
   * Some market data requests are non-snapshot requests where market data is continuously read from a JMS topic; this means they require a JMS subscription.
   * <p>
   * As per LIV-19, after we've subscribed to the market data (and started getting deltas), we do a snapshot to make sure we get a full initial image of the data. Things are done in this order (first
   * subscribe, then snapshot) so we don't lose any ticks. See LIV-19.
   */
  private class TopicBasedSubscriptionResponseReceiver extends AbstractSubscriptionResponseReceiver {

    public TopicBasedSubscriptionResponseReceiver(Collection<SubscriptionHandle> subHandles) {
      super(subHandles);
    }

    @Override
    protected void processResponse(final PoolExecutor.CompletionListener<Void> result) {
      try {
        final PoolExecutor.CompletionListener<Void> callback = new PoolExecutor.CompletionListener<Void>() {

          @Override
          public void success(final Void reserved) {
            result.success(null);
          }

          @Override
          public void failure(final Throwable error) {
            try {
              s_logger.error("Failed to process subscription response", error);
              // This is unexpected. Fail everything.
              for (LiveDataSubscriptionResponse response : getSuccessResponses().values()) {
                response.setSubscriptionResult(LiveDataSubscriptionResult.INTERNAL_ERROR);
                response.setUserMessage(error.toString());
              }
              getFailedResponses().putAll(getSuccessResponses());
              getSuccessResponses().clear();
            } catch (Throwable e) {
              result.failure(e);
              return;
            }
            result.success(null);
          }

        };
        try {
          // Phase 1. Create a subscription to market data topic
          startReceivingTicks();
          // Phase 2. After we've subscribed to the market data (and started getting deltas), snapshot it
          snapshot(callback);
        } catch (Throwable e) {
          callback.failure(e);
        }
      } catch (Throwable e) {
        result.failure(e);
      }
    }

    private void startReceivingTicks() {
      Map<SubscriptionHandle, LiveDataSubscriptionResponse> resps = getSuccessResponses();
      // tick distribution specifications can be duplicated, only pass each down once to startReceivingTicks()
      Collection<String> distributionSpecs = new HashSet<>(resps.size());
      for (Map.Entry<SubscriptionHandle, LiveDataSubscriptionResponse> entry : resps.entrySet()) {
        DistributedLiveDataClient.this.subscriptionStartingToReceiveTicks(entry.getKey(), entry.getValue());
        distributionSpecs.add(entry.getValue().getTickDistributionSpecification());
      }
      DistributedLiveDataClient.this.startReceivingTicks(distributionSpecs);
    }

    private void snapshot(final PoolExecutor.CompletionListener<Void> callback) {
      ArrayList<LiveDataSpecification> successLiveDataSpecs = new ArrayList<>();
      for (LiveDataSubscriptionResponse response : getSuccessResponses().values()) {
        successLiveDataSpecs.add(response.getRequestedSpecification());
      }
      DistributedLiveDataClient.this.snapshot(getUser(), successLiveDataSpecs, TIMEOUT, new PoolExecutor.CompletionListener<Collection<LiveDataSubscriptionResponse>>() {

        @Override
        public void success(final Collection<LiveDataSubscriptionResponse> snapshots) {
          try {
            for (LiveDataSubscriptionResponse response : snapshots) {
              final SubscriptionHandle handle = getSpec2SubHandle().get(response.getRequestedSpecification());
              if (handle == null) {
                throw new OpenGammaRuntimeException("Could not find handle corresponding to request " + response.getRequestedSpecification());
              }

              // could be that even though subscription to the JMS topic (phase 1) succeeded, snapshot (phase 2) for some reason failed.
              // since phase 1 already validated everything, this should mainly happen when user permissions are modified 
              // in the sub-second interval between phases 1 and 2!

              // Not so. In fact for a system like Bloomberg, because of the lag in subscription, the LiveDataServer
              // may in fact think that you can successfully subscribe, but then when the snapshot is requested we detect
              // that it's not a valid code. So this is the time that we've actually poked the underlying data provider
              // to check.

              // In addition, it may be that for a FireHose server we didn't have the full SoW on the initial request
              // but now we do.
              if (response.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
                handle.addSnapshotOnHold(response.getSnapshot());
              } else {
                getSuccessResponses().remove(handle);
                getFailedResponses().put(handle, response);
              }
            }
          } catch (Throwable t) {
            callback.failure(t);
            return;
          }
          callback.success(null);
        }

        @Override
        public void failure(final Throwable error) {
          callback.failure(error);
        }

      });
    }

    @Override
    protected void sendResponse() {
      super.sendResponse();

      for (Map.Entry<SubscriptionHandle, LiveDataSubscriptionResponse> successEntry : getSuccessResponses().entrySet()) {
        SubscriptionHandle handle = successEntry.getKey();
        LiveDataSubscriptionResponse response = successEntry.getValue();
        subscriptionRequestSatisfied(handle, response);
      }

      for (Map.Entry<SubscriptionHandle, LiveDataSubscriptionResponse> failedEntry : getFailedResponses().entrySet()) {
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
   * @param tickDistributionSpecification JMS topic name
   */
  public void startReceivingTicks(Collection<String> tickDistributionSpecification) {
    // Default no-op.
  }

  public void stopReceivingTicks(String tickDistributionSpecification) {
    // Default no-op.
  }

  // REVIEW kirk 2009-10-28 -- This is just a braindead way of getting ticks to come in
  // until we can get a handle on the construction of receivers based on responses.
  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    FudgeMsg fudgeMsg = msgEnvelope.getMessage();
    LiveDataValueUpdateBean update = LiveDataValueUpdateBeanFudgeBuilder.fromFudgeMsg(new FudgeDeserializer(fudgeContext), fudgeMsg);
    valueUpdate(update);
  }

  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications) {
    return _entitlementChecker.isEntitled(user, requestedSpecifications);
  }

  @Override
  public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
    return _entitlementChecker.isEntitled(user, requestedSpecification);
  }

}
