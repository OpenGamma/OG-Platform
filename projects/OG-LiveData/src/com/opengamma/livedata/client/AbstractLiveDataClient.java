/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.transport.ByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A base class that handles all the in-memory requirements
 * for a {@link LiveDataClient} implementation.
 */
@PublicAPI
public abstract class AbstractLiveDataClient implements LiveDataClient {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractLiveDataClient.class);
  // Injected Inputs:
  private long _heartbeatPeriod = HeartbeatSender.DEFAULT_PERIOD;
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  // Running State:
  private final ValueDistributor _valueDistributor = new ValueDistributor();
  private final Timer _timer = new Timer("LiveDataClient Timer");
  private HeartbeatSender _heartbeatSender;
  private final Lock _subscriptionLock = new ReentrantLock();
  private final Map<LiveDataSpecification, Set<SubscriptionHandle>> _fullyQualifiedSpec2PendingSubscriptions =
    new HashMap<LiveDataSpecification, Set<SubscriptionHandle>>();
  private final Set<LiveDataSpecification> _activeSubscriptionSpecifications =
    new HashSet<LiveDataSpecification>();
  
  public void setHeartbeatMessageSender(ByteArrayMessageSender messageSender) {
    ArgumentChecker.notNull(messageSender, "Message Sender");
    _heartbeatSender = new HeartbeatSender(messageSender, _valueDistributor, getFudgeContext(), getTimer(), getHeartbeatPeriod());
  }

  @Override
  public void close() {
    _timer.cancel();
  }

  /**
   * @return the heartbeatSender
   */
  public HeartbeatSender getHeartbeatSender() {
    return _heartbeatSender;
  }

  /**
   * @return the timer
   */
  public Timer getTimer() {
    return _timer;
  }

  /**
   * @return the heartbeatPeriod
   */
  public long getHeartbeatPeriod() {
    return _heartbeatPeriod;
  }

  /**
   * @param heartbeatPeriod the heartbeatPeriod to set
   */
  public void setHeartbeatPeriod(long heartbeatPeriod) {
    _heartbeatPeriod = heartbeatPeriod;
  }

  /**
   * @return the valueDistributor
   */
  public ValueDistributor getValueDistributor() {
    return _valueDistributor;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * @param fudgeContext the fudgeContext to set
   */
  public void setFudgeContext(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  /**
   * Obtain <em>a copy of</em> the active subscription specifications.
   * For concurrency reason this will return a new copy on each call.
   * 
   * @return a copy of the Active Fully-Qualified Subscription Specifications
   */
  public Set<LiveDataSpecification> getActiveSubscriptionSpecifications() {
    _subscriptionLock.lock();
    try {
      return new HashSet<LiveDataSpecification>(_activeSubscriptionSpecifications);
    } finally {
      _subscriptionLock.unlock();
    }
  }
  
  
  
  @Override
  public void subscribe(UserPrincipal user,
      Collection<LiveDataSpecification> requestedSpecifications,
      LiveDataListener listener) {
    
    ArrayList<SubscriptionHandle> subscriptionHandles = new ArrayList<SubscriptionHandle>();   
    for (LiveDataSpecification requestedSpecification : requestedSpecifications) {
      SubscriptionHandle subHandle = new SubscriptionHandle(user, SubscriptionType.NON_PERSISTENT, requestedSpecification, listener);
      subscriptionHandles.add(subHandle);
    }
    
    if (!subscriptionHandles.isEmpty()) {
      handleSubscriptionRequest(subscriptionHandles);
    }
  }
  
  @Override
  public void subscribe(UserPrincipal user, LiveDataSpecification requestedSpecification,
      LiveDataListener listener) {
    subscribe(user, Collections.singleton(requestedSpecification), listener);
  }
  

  
  private class SnapshotListener implements LiveDataListener {
    
    private final Collection<LiveDataSubscriptionResponse> _responses = new ArrayList<LiveDataSubscriptionResponse>();
    private final CountDownLatch _responsesReceived;
    
    public SnapshotListener(int expectedNumberOfResponses) {
      _responsesReceived = new CountDownLatch(expectedNumberOfResponses);
    }

    @Override
    public void subscriptionResultReceived(
        LiveDataSubscriptionResponse subscriptionResult) {
      _responses.add(subscriptionResult);
      _responsesReceived.countDown();
    }

    @Override
    public void subscriptionStopped(
        LiveDataSpecification fullyQualifiedSpecification) {
      // should never go here      
      throw new UnsupportedOperationException();
    }

    @Override
    public void valueUpdate(LiveDataValueUpdate valueUpdate) {
      // should never go here
      throw new UnsupportedOperationException();
    }
  }
  
  @Override
  public Collection<LiveDataSubscriptionResponse> snapshot(UserPrincipal user,
      Collection<LiveDataSpecification> requestedSpecifications,
      long timeout) {
    
    ArgumentChecker.notNull(user, "User");
    ArgumentChecker.notNull(requestedSpecifications, "Live Data specifications");
    
    if (requestedSpecifications.isEmpty()) {
      return Collections.emptyList();
    }
    
    SnapshotListener listener = new SnapshotListener(requestedSpecifications.size());
    
    ArrayList<SubscriptionHandle> subscriptionHandles = new ArrayList<SubscriptionHandle>();   
    for (LiveDataSpecification requestedSpecification : requestedSpecifications) {
      SubscriptionHandle subHandle = new SubscriptionHandle(user, SubscriptionType.SNAPSHOT, requestedSpecification, listener);
      subscriptionHandles.add(subHandle);                      
    }
    
    handleSubscriptionRequest(subscriptionHandles);
    
    boolean success;
    try {
      success = listener._responsesReceived.await(timeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Thread interrupted when obtaining snapshot");
    }
    
    if (success) {
      return listener._responses;
    } else {
      throw new OpenGammaRuntimeException("Timeout " + timeout + " ms reached when obtaining snapshot " + subscriptionHandles);
    }
  }

  @Override
  public LiveDataSubscriptionResponse snapshot(UserPrincipal user,
      LiveDataSpecification requestedSpecification,
      long timeout) {
    
    Collection<LiveDataSubscriptionResponse> snapshots = snapshot(user, 
        Collections.singleton(requestedSpecification), 
        timeout);
    
    if (snapshots.size() != 1) {
      throw new OpenGammaRuntimeException("One snapshot request should return 1 snapshot, was " + snapshots.size());
    }
    
    return snapshots.iterator().next();
  }

  /**
   * @param subHandle Not null, not empty
   */
  protected abstract void handleSubscriptionRequest(Collection<SubscriptionHandle> subHandle);
  
  
  protected void subscriptionStartingToReceiveTicks(SubscriptionHandle subHandle, LiveDataSubscriptionResponse response) {
    synchronized (_fullyQualifiedSpec2PendingSubscriptions) {
      
      Set<SubscriptionHandle> subscriptionHandleSet = _fullyQualifiedSpec2PendingSubscriptions.get(response.getFullyQualifiedSpecification());
      if (subscriptionHandleSet == null) {
        subscriptionHandleSet = new HashSet<SubscriptionHandle>();
        _fullyQualifiedSpec2PendingSubscriptions.put(response.getFullyQualifiedSpecification(), subscriptionHandleSet);
      }
      
      subscriptionHandleSet.add(subHandle);
    } 
  }
  
  protected void subscriptionRequestSatisfied(SubscriptionHandle subHandle, LiveDataSubscriptionResponse response) {
    synchronized (_fullyQualifiedSpec2PendingSubscriptions) {
      // Atomically (to valueUpdate callers) turn the pending subscription into a full subscription.
      // REVIEW jonathan 2010-12-01 -- rearranged this so that the internal _subscriptionLock is not being held while
      // releasing ticks to listeners, which is a recipe for deadlock.
      removePendingSubscription(subHandle);
      subHandle.releaseTicksOnHold();
      _subscriptionLock.lock();
      try {
        _activeSubscriptionSpecifications.add(response.getFullyQualifiedSpecification());
        getValueDistributor().addListener(response.getFullyQualifiedSpecification(), subHandle.getListener());
      } finally {
        _subscriptionLock.unlock();
      }
    }
  }
  
  protected void subscriptionRequestFailed(SubscriptionHandle subHandle, LiveDataSubscriptionResponse response) {
    removePendingSubscription(subHandle);
  }
  
  protected void removePendingSubscription(SubscriptionHandle subHandle) {
    synchronized (_fullyQualifiedSpec2PendingSubscriptions) {
      for (Iterator<Set<SubscriptionHandle>> iterator = _fullyQualifiedSpec2PendingSubscriptions.values().iterator(); iterator.hasNext(); ) {
        Set<SubscriptionHandle> handleSet = iterator.next();
        boolean removed = handleSet.remove(subHandle);
        if (removed && handleSet.isEmpty()) {
          iterator.remove();
        }
      }
      

    }
  }
  
  
  
  @Override
  public void unsubscribe(UserPrincipal user,
      Collection<LiveDataSpecification> fullyQualifiedSpecifications,
      LiveDataListener listener) {
    for (LiveDataSpecification fullyQualifiedSpecification : fullyQualifiedSpecifications) {
      s_logger.info("Unsubscribing by {} to {} delivered to {}",
          new Object[] {user, fullyQualifiedSpecification, listener});
      boolean unsubscribeToSpec = false;
      _subscriptionLock.lock();
      try {
        boolean stillActiveSubs = getValueDistributor().removeListener(fullyQualifiedSpecification, listener);
        if (!stillActiveSubs) {
          unsubscribeToSpec = true;
          _activeSubscriptionSpecifications.remove(fullyQualifiedSpecification);
        }
      } finally {
        _subscriptionLock.unlock();
      }
      
      // REVIEW kirk 2009-09-29 -- Potential race condition with multiple
      // subscribers and unsubscribers here.... do something about it?
      if (unsubscribeToSpec) {
        cancelPublication(fullyQualifiedSpecification);
      }
      listener.subscriptionStopped(fullyQualifiedSpecification);
    }
  }

  @Override
  public void unsubscribe(UserPrincipal user,
      LiveDataSpecification fullyQualifiedSpecification,
      LiveDataListener listener) {
    unsubscribe(user, Collections.singleton(fullyQualifiedSpecification), listener);
  }

  protected abstract void cancelPublication(LiveDataSpecification fullyQualifiedSpecification);
  
  @Override
  public String getDefaultNormalizationRuleSetId() {
    return StandardRules.getOpenGammaRuleSetId();
  }
  
  protected void valueUpdate(LiveDataValueUpdateBean update) {
    
    s_logger.debug("{}", update);
    
    synchronized (_fullyQualifiedSpec2PendingSubscriptions) { 
      Set<SubscriptionHandle> pendingSubscriptions = _fullyQualifiedSpec2PendingSubscriptions.get(update.getSpecification());
      if (pendingSubscriptions != null) {
        for (SubscriptionHandle pendingSubscription : pendingSubscriptions) {
          pendingSubscription.addTickOnHold(update);      
        }
      }
    }
    
    getValueDistributor().notifyListeners(update);
  }

}
