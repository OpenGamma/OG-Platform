/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataSubscriptionResponse;
import com.opengamma.transport.ByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * A base class that handles all the in-memory requirements
 * for a {@link LiveDataClient} implementation.
 *
 * @author kirk
 */
public abstract class AbstractLiveDataClient implements LiveDataClient {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractLiveDataClient.class);
  // Injected Inputs:
  private long _heartbeatPeriod = HeartbeatSender.DEFAULT_PERIOD;
  private FudgeContext _fudgeContext = new FudgeContext();
  // Running State:
  private final ValueDistributor _valueDistributor = new ValueDistributor();
  private final Timer _timer = new Timer("LiveDataClient Timer");
  private HeartbeatSender _heartbeatSender = null;
  private final Lock _subscriptionLock = new ReentrantLock();
  private final Set<SubscriptionHandle> _pendingSubscriptions =
    new HashSet<SubscriptionHandle>();
  private final Set<LiveDataSpecification> _activeSubscriptionSpecifications =
    new HashSet<LiveDataSpecification>();
  
  public void setHeartbeatMessageSender(ByteArrayMessageSender messageSender) {
    ArgumentChecker.checkNotNull(messageSender, "Message Sender");
    _heartbeatSender = new HeartbeatSender(messageSender, _valueDistributor, getFudgeContext(), getTimer(), getHeartbeatPeriod());
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
  public void subscribe(String userName,
      Collection<LiveDataSpecification> requestedSpecifications,
      LiveDataListener listener) {
    
    ArrayList<SubscriptionHandle> subscriptionHandles = new ArrayList<SubscriptionHandle>();   
    for (LiveDataSpecification requestedSpecification : requestedSpecifications) {
      SubscriptionHandle subHandle = new SubscriptionHandle(userName, requestedSpecification, listener);
      if(addPendingSubscription(subHandle)) {
        subscriptionHandles.add(subHandle);                      
      }
    }
    
    handleSubscriptionRequest(subscriptionHandles);
  }
  
  @Override
  public void subscribe(String userName, LiveDataSpecification requestedSpecification,
      LiveDataListener listener) {
    subscribe(userName, Collections.singleton(requestedSpecification), listener);
  }
  
  protected abstract void handleSubscriptionRequest(Collection<SubscriptionHandle> subHandle);
  
  protected void subscriptionRequestSatisfied(SubscriptionHandle subHandle, LiveDataSubscriptionResponse response) {
    _subscriptionLock.lock();
    try {
      removePendingSubscription(subHandle);
      _activeSubscriptionSpecifications.add(response.getFullyQualifiedSpecification());
      getValueDistributor().addListener(response.getFullyQualifiedSpecification(), subHandle.getListener());
    } finally {
      _subscriptionLock.unlock();
    }
    subHandle.getListener().subscriptionResultReceived(response);
  }
  
  protected void subscriptionRequestFailed(SubscriptionHandle subHandle, LiveDataSubscriptionResponse response) {
    _subscriptionLock.lock();
    try {
      removePendingSubscription(subHandle);
    } finally {
      _subscriptionLock.unlock();
    }
    subHandle.getListener().subscriptionResultReceived(response);
  }
  
  protected boolean addPendingSubscription(SubscriptionHandle subHandle) {
    _subscriptionLock.lock();
    try {
      if(_pendingSubscriptions.contains(subHandle)) {
        return false;
      }
      _pendingSubscriptions.add(subHandle);
      return true;
    } finally {
      _subscriptionLock.unlock();
    }
  }
  
  protected void removePendingSubscription(SubscriptionHandle subHandle) {
    _subscriptionLock.lock();
    try {
      _pendingSubscriptions.remove(subHandle);
    } finally {
      _subscriptionLock.unlock();
    }
  }
  
  @Override
  public void unsubscribe(String userName,
      Collection<LiveDataSpecification> fullyQualifiedSpecifications,
      LiveDataListener listener) {
    for (LiveDataSpecification fullyQualifiedSpecification : fullyQualifiedSpecifications) {
      s_logger.info("Unsubscribing by {} to {} delivered to {}",
          new Object[] {userName, fullyQualifiedSpecification, listener} );
      boolean unsubscribeToSpec = false;
      _subscriptionLock.lock();
      try {
        boolean stillActiveSubs = getValueDistributor().removeListener(fullyQualifiedSpecification, listener);
        if(!stillActiveSubs) {
          unsubscribeToSpec = true;
          _activeSubscriptionSpecifications.remove(fullyQualifiedSpecification);
        }
      } finally {
        _subscriptionLock.unlock();
      }
      
      // REVIEW kirk 2009-09-29 -- Potential race condition with multiple
      // subscribers and unsubscribers here.... do something about it?
      if(unsubscribeToSpec) {
        cancelPublication(fullyQualifiedSpecification);
      }
      listener.subscriptionStopped(fullyQualifiedSpecification);
    }
  }

  @Override
  public void unsubscribe(String userName,
      LiveDataSpecification fullyQualifiedSpecification,
      LiveDataListener listener) {
    unsubscribe(userName, Collections.singleton(fullyQualifiedSpecification), listener);
  }

  protected abstract void cancelPublication(LiveDataSpecification fullyQualifiedSpecification);
  
}
