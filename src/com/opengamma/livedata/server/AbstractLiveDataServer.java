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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdentificationDomain;
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
public abstract class AbstractLiveDataServer implements LiveDataServerMBean {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractLiveDataServer.class);
  
  private final Set<MarketDataFieldReceiver> _fieldReceivers = new CopyOnWriteArraySet<MarketDataFieldReceiver>();
  private final Set<SubscriptionListener> _subscriptionListeners = new CopyOnWriteArraySet<SubscriptionListener>();
  private final Set<Subscription> _currentlyActiveSubscriptions = Collections.synchronizedSet(new HashSet<Subscription>());
  private final Map<String, LiveDataSpecificationImpl> _securityUniqueId2FullyQualifiedSpecification = new ConcurrentHashMap<String, LiveDataSpecificationImpl>();
  private final Map<LiveDataSpecificationImpl, Subscription> _fullyQualifiedSpec2Subscription = new ConcurrentHashMap<LiveDataSpecificationImpl, Subscription>();
  
  private final AtomicLong _numUpdatesSent = new AtomicLong(0);

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
   * @return Subscription handle
   */
  protected abstract Object subscribe(String uniqueId);
  
  /**
   * @param subscriptionHandle The object that was returned by subscribe()
   */
  protected abstract void unsubscribe(Object subscriptionHandle);
  
  /**
   * @return Identification domain that uniquely identifies securities for this type of server.
   */
  protected abstract IdentificationDomain getUniqueIdDomain();
  
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
        
        String tickDistributionSpec = getDistributionSpecificationResolver().getDistributionSpecification(qualifiedSpecification);
      
        _subscriptionLock.lock();
        try {
          if (isSubscribedTo(qualifiedSpecification)) {
            s_logger.info("Already subscribed to {}", qualifiedSpecification);
          } else {
            
            String securityUniqueId = qualifiedSpecification.getIdentifier(getUniqueIdDomain());
            Object subscriptionHandle = subscribe(securityUniqueId);
            
            LiveDataSpecificationImpl localFullyQualifiedSpecification = new LiveDataSpecificationImpl(qualifiedSpecification);
            Subscription subscription = new Subscription(securityUniqueId, 
                subscriptionHandle, 
                tickDistributionSpec);
            
            _currentlyActiveSubscriptions.add(subscription);
            _securityUniqueId2FullyQualifiedSpecification.put(securityUniqueId, localFullyQualifiedSpecification);
            _fullyQualifiedSpec2Subscription.put(localFullyQualifiedSpecification, subscription);

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
        
        LiveDataSpecificationImpl localFullyQualifiedSpecification = new LiveDataSpecificationImpl(fullyQualifiedSpec);
        Subscription subscription = _fullyQualifiedSpec2Subscription.get(localFullyQualifiedSpecification);
        if (subscription == null) {
          throw new OpenGammaRuntimeException("Subscription handle not found for " + fullyQualifiedSpec);
        }

        unsubscribe(subscription.getHandle());
        
        _currentlyActiveSubscriptions.remove(subscription);
        String uniqueId = fullyQualifiedSpec.getIdentifier(getUniqueIdDomain());
        _securityUniqueId2FullyQualifiedSpecification.remove(uniqueId);
        _fullyQualifiedSpec2Subscription.remove(subscription);
        
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
    return _fullyQualifiedSpec2Subscription.containsKey(new LiveDataSpecificationImpl(fullyQualifiedSpec));
  }
  
  public LiveDataSpecification getFullyQualifiedSpec(String securityUniqueId) {
    return _securityUniqueId2FullyQualifiedSpecification.get(securityUniqueId);  
  }
  
  public void liveDataReceived(LiveDataSpecification resolvedSpecification, FudgeFieldContainer liveDataFields) {
    s_logger.debug("Live data received: {}", liveDataFields);
    
    _numUpdatesSent.incrementAndGet();
    
    // TODO kirk 2009-10-29 -- This needs to be much better.
    for(MarketDataFieldReceiver receiver : _fieldReceivers) {
      receiver.marketDataReceived(resolvedSpecification, liveDataFields);
    }
  }
  
  
  @Override
  public Set<String> getActiveDistributionSpecs() {
    Set<String> subscriptions = new HashSet<String>();
    synchronized (_currentlyActiveSubscriptions) {
      for (Subscription subscription : _currentlyActiveSubscriptions) {
        subscriptions.add(subscription.getDistributionSpecification());                
      }
    }
    return subscriptions;
  }

  @Override
  public Set<String> getActiveSubscriptionIds() {
    Set<String> subscriptions = new HashSet<String>();
    synchronized (_currentlyActiveSubscriptions) {
      for (Subscription subscription : _currentlyActiveSubscriptions) {
        subscriptions.add(subscription.getSecurityUniqueId());                
      }
    }
    return subscriptions;
  }

  @Override
  public int getNumActiveSubscriptions() {
    return _currentlyActiveSubscriptions.size();
  }

  @Override
  public long getNumLiveDataUpdatesSent() {
    return _numUpdatesSent.get();
  }
  
}
