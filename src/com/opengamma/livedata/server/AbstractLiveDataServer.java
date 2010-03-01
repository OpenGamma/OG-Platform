/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataSpecificationImpl;
import com.opengamma.livedata.LiveDataSubscriptionRequest;
import com.opengamma.livedata.LiveDataSubscriptionResponse;
import com.opengamma.livedata.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.LiveDataSubscriptionResult;
import com.opengamma.livedata.client.IdentitySpecificationResolver;
import com.opengamma.livedata.client.LiveDataEntitlementChecker;
import com.opengamma.livedata.client.LiveDataSpecificationResolver;
import com.opengamma.livedata.client.PermissiveLiveDataEntitlementChecker;
import com.opengamma.util.ArgumentChecker;

/**
 * The base class from which most OpenGamma Live Data feed servers should
 * extend. Handles most common cases for distributed contract management.
 *
 * @author kirk
 */
public abstract class AbstractLiveDataServer {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractLiveDataServer.class);
  
  private final Set<MarketDataFieldReceiver> _fieldReceivers = new CopyOnWriteArraySet<MarketDataFieldReceiver>();
  private final Set<SubscriptionListener> _subscriptionListeners = new CopyOnWriteArraySet<SubscriptionListener>();
  private final Set<LiveDataSpecification> _currentlyActiveSubscriptions = Collections.synchronizedSet(new HashSet<LiveDataSpecification>());
  
  private final Lock _subscriptionLock = new ReentrantLock();
  
  private DistributionSpecificationResolver _distributionSpecificationResolver = new NaiveDistributionSpecificationResolver();
  private LiveDataSpecificationResolver _specificationResolver = new IdentitySpecificationResolver();
  private LiveDataEntitlementChecker _entitlementChecker = new PermissiveLiveDataEntitlementChecker();
  
  /**
   * @return the distributionSpecificationResolver
   */
  public DistributionSpecificationResolver getDistributionSpecificationResolver() {
    return _distributionSpecificationResolver;
  }

  /**
   * @param distributionSpecificationResolver the distributionSpecificationResolver to set
   */
  public void setDistributionSpecificationResolver(
      DistributionSpecificationResolver distributionSpecificationResolver) {
    _distributionSpecificationResolver = distributionSpecificationResolver;
  }

  public void addMarketDataFieldReceiver(MarketDataFieldReceiver fieldReceiver) {
    ArgumentChecker.checkNotNull(fieldReceiver, "Market Data Field Receiver");
    _fieldReceivers.add(fieldReceiver);
  }
  
  public void setMarketDataFieldReceivers(Collection<MarketDataFieldReceiver> fieldReceivers) {
    _fieldReceivers.clear();
    for (MarketDataFieldReceiver receiver : fieldReceivers) {
      addMarketDataFieldReceiver(receiver);      
    }
  }
  
  public void addSubscriptionListener(SubscriptionListener subscriptionListener) {
    ArgumentChecker.checkNotNull(subscriptionListener, "Subscription Listener");
    _subscriptionListeners.add(subscriptionListener);
  }
  
  public void setSubscriptionListeners(Collection<SubscriptionListener> subscriptionListeners) {
    _subscriptionListeners.clear();
    for (SubscriptionListener subscriptionListener : subscriptionListeners) {
      addSubscriptionListener(subscriptionListener);      
    }
  }
  
  public Set<LiveDataSpecification> getCurrentlyActiveSubscriptions() {
    synchronized (_currentlyActiveSubscriptions) {
      return new HashSet<LiveDataSpecification>(_currentlyActiveSubscriptions);
    }
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
   * @param subscriptionRequest
   * @return
   */
  protected abstract void subscribe(LiveDataSpecification fullyQualifiedSpec);
  protected abstract void unsubscribe(LiveDataSpecification fullyQualifiedSpec);
  
  public final LiveDataSubscriptionResponseMsg subscriptionRequestMade(LiveDataSubscriptionRequest subscriptionRequest) {
    
    ArrayList<LiveDataSubscriptionResponse> responses = new ArrayList<LiveDataSubscriptionResponse>();
    for (LiveDataSpecificationImpl requestedSpecification : subscriptionRequest.getSpecifications()) {
      
      try {
    
        LiveDataSpecification qualifiedSpecification = getSpecificationResolver().resolve(requestedSpecification);
        if(qualifiedSpecification == null) {
          s_logger.info("Unable to resolve requested specification {}", requestedSpecification);
          responses.add(new LiveDataSubscriptionResponse(requestedSpecification, 
              null, 
              LiveDataSubscriptionResult.NOT_PRESENT, 
              null, 
              null));
          continue;
        }
        
        if(!getEntitlementChecker().isEntitled(subscriptionRequest.getUserName(), qualifiedSpecification)) {
          s_logger.info("User {} not entitled to specification {}", subscriptionRequest.getUserName(), qualifiedSpecification);
          // TODO kirk 2009-10-28 -- Extend interface on EntitlementChecker to get a user message.
          responses.add(new LiveDataSubscriptionResponse(requestedSpecification, 
              new LiveDataSpecificationImpl(qualifiedSpecification), 
              LiveDataSubscriptionResult.NOT_AUTHORIZED, 
              null, 
              null));
          continue;
        }
      
        _subscriptionLock.lock();
        try {
          if (isSubscribedTo(qualifiedSpecification)) {
            s_logger.info("Already subscribed to {}", qualifiedSpecification);
          } else {
            subscribe(qualifiedSpecification);
            _currentlyActiveSubscriptions.add(new LiveDataSpecificationImpl(qualifiedSpecification));
            for (SubscriptionListener listener : _subscriptionListeners) {
              try {
                listener.subscribed(qualifiedSpecification);
              } catch (RuntimeException e) {
                s_logger.error("Listener subscribe failed", e);
              }
            }
          }
        } finally {
          _subscriptionLock.unlock();          
        }

        String tickDistributionSpec = getDistributionSpecificationResolver().getDistributionSpecification(qualifiedSpecification);
        LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(requestedSpecification, new LiveDataSpecificationImpl(qualifiedSpecification), LiveDataSubscriptionResult.SUCCESS, null, tickDistributionSpec);
        responses.add(response);
      
      } catch (Exception e) {
        s_logger.error("Failed to subscribe to " + requestedSpecification, e);
        responses.add(new LiveDataSubscriptionResponse(requestedSpecification, null, LiveDataSubscriptionResult.INTERNAL_ERROR, e.getMessage(), null));                
      }
      
    }
    
    return new LiveDataSubscriptionResponseMsg(subscriptionRequest.getUserName(), responses);
  }
  
  public final void unsubscriptionRequestMade(LiveDataSpecification fullyQualifiedSpec) {
    _subscriptionLock.lock();
    try {
      if (isSubscribedTo(fullyQualifiedSpec)) {
        s_logger.info("Terminating publication of {}", fullyQualifiedSpec);
        
        unsubscribe(fullyQualifiedSpec);
        
        _currentlyActiveSubscriptions.remove(new LiveDataSpecificationImpl(fullyQualifiedSpec));
        for (SubscriptionListener listener : _subscriptionListeners) {
          try {
            listener.unsubscribed(fullyQualifiedSpec);
          } catch (RuntimeException e) {
            s_logger.error("Listener unsubscribe failed", e);
          }
        }
      } else {
        s_logger.warn("Already unsubscribed from {}", fullyQualifiedSpec);
      }
            
    } finally {
      _subscriptionLock.unlock();          
    }
  }
  
  public boolean isSubscribedTo(LiveDataSpecification fullyQualifiedSpec) {
    return _currentlyActiveSubscriptions.contains(new LiveDataSpecificationImpl(fullyQualifiedSpec));
  }
  
  public void liveDataReceived(LiveDataSpecification resolvedSpecification, FudgeFieldContainer liveDataFields) {
    s_logger.debug("Live data received: {}", liveDataFields);
    // TODO kirk 2009-10-29 -- This needs to be much better.
    for(MarketDataFieldReceiver receiver : _fieldReceivers) {
      receiver.marketDataReceived(resolvedSpecification, liveDataFields);
    }
  }
  
}
