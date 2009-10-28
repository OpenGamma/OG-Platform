/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

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
import com.opengamma.livedata.LiveDataSubscriptionResult;
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
  private LiveDataSpecificationResolver _specificationResolver = new IdentitySpecificationResolver();
  private LiveDataEntitlementChecker _entitlementChecker = new PermissiveLiveDataEntitlementChecker();
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
  // TODO kirk 2009-09-29 -- Figure out a better way to handle active subscriptions.
  // The data structures were getting too unwieldy for this pass.
  private final Set<SubscriptionHandle> _activeSubscriptions =
    new HashSet<SubscriptionHandle>();
  
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
   * @return the specificationResolver
   */
  public LiveDataSpecificationResolver getSpecificationResolver() {
    return _specificationResolver;
  }

  /**
   * @param specificationResolver the specificationResolver to set
   */
  public void setSpecificationResolver(
      LiveDataSpecificationResolver specificationResolver) {
    assert specificationResolver != null;
    _specificationResolver = specificationResolver;
  }

  /**
   * @return the entitlementChecker
   */
  public LiveDataEntitlementChecker getEntitlementChecker() {
    return _entitlementChecker;
  }

  /**
   * @param entitlementChecker the entitlementChecker to set
   */
  public void setEntitlementChecker(LiveDataEntitlementChecker entitlementChecker) {
    _entitlementChecker = entitlementChecker;
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
  public void subscribe(String userName, LiveDataSpecification requestedSpecification,
      LiveDataListener listener) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    
    // TODO kirk 2009-09-29 -- This should all happen in a background thread.
    
    LiveDataSpecification qualifiedSpecification = getSpecificationResolver().resolve(requestedSpecification);
    if(qualifiedSpecification == null) {
      s_logger.info("Unable to resolve requested specification {}", requestedSpecification);
      listener.subscriptionResultReceived(new LiveDataSubscriptionResponse(userName, requestedSpecification, null, LiveDataSubscriptionResult.NOT_PRESENT, null));
      return;
    }
    
    if(!getEntitlementChecker().isEntitled(userName, qualifiedSpecification)) {
      s_logger.info("User {} not entitled to specification {}", userName, qualifiedSpecification);
      // TODO kirk 2009-10-28 -- Extend interface on EntitlementChecker to get a user message.
      listener.subscriptionResultReceived(new LiveDataSubscriptionResponse(userName, requestedSpecification, qualifiedSpecification, LiveDataSubscriptionResult.NOT_AUTHORIZED, null));
      return;
    }
    
    // Now we've got the fully qualified request, we can acquire the subscription
    // lock and enqueue the pending subscription handle.
    SubscriptionHandle subHandle = new SubscriptionHandle(userName, requestedSpecification, qualifiedSpecification, listener);
    if(!addPendingSubscription(subHandle)) {
      // Already enqueued. Do nothing, the original request will take
      // care of it.
      return;
    }
    
    handleSubscriptionRequest(subHandle);
  }
  
  protected abstract void handleSubscriptionRequest(SubscriptionHandle subHandle);
  
  protected void subscriptionRequestSatisfied(SubscriptionHandle subHandle) {
    _subscriptionLock.lock();
    try {
      removePendingSubscription(subHandle);
      _activeSubscriptionSpecifications.add(subHandle.getFullyQualifiedSpecification());
      getValueDistributor().addListener(subHandle.getFullyQualifiedSpecification(), subHandle.getListener());
    } finally {
      _subscriptionLock.unlock();
    }
    subHandle.getListener().subscriptionResultReceived(
        new LiveDataSubscriptionResponse(
            subHandle.getUserName(), subHandle.getRequestedSpecification(), subHandle.getFullyQualifiedSpecification(), LiveDataSubscriptionResult.SUCCESS, null));
  }
  
  protected void subscriptionRequestFailed(SubscriptionHandle subHandle, LiveDataSubscriptionResult result, String userMessage) {
    _subscriptionLock.lock();
    try {
      removePendingSubscription(subHandle);
    } finally {
      _subscriptionLock.unlock();
    }
    subHandle.getListener().subscriptionResultReceived(
        new LiveDataSubscriptionResponse(
            subHandle.getUserName(), subHandle.getRequestedSpecification(), subHandle.getFullyQualifiedSpecification(), result, userMessage));
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
      LiveDataSpecification fullyQualifiedSpecification,
      LiveDataListener listener) {
    s_logger.info("Unsubscribing by {} to {} delivered to {}",
        new Object[] {userName, fullyQualifiedSpecification, listener} );
    SubscriptionHandle subHandle = new SubscriptionHandle(userName, null, fullyQualifiedSpecification, listener);
    boolean unsubscribeToSpec = false;
    _subscriptionLock.lock();
    try {
      _activeSubscriptions.remove(subHandle);
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

  protected abstract void cancelPublication(LiveDataSpecification fullyQualifiedSpecification);
  
}
