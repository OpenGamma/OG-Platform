/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Collection;
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
  private final Set<Subscription> _currentlyActiveSubscriptions = new CopyOnWriteArraySet<Subscription>();
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
  protected abstract Object doSubscribe(String uniqueId);
  
  /**
   * @param subscriptionHandle The object that was returned by subscribe()
   */
  protected abstract void doUnsubscribe(Object subscriptionHandle);
  
  /**
   * @return Identification domain that uniquely identifies securities for this type of server.
   */
  protected abstract IdentificationDomain getUniqueIdDomain();
  
  /**
   * Subscribes to market data.
   * If the server already subscribes to the given market data, this method is a no-op.
   *  
   * @param qualifiedSpecification This spec needs to contain an ID with the domain returned by {@link #getUniqueIdDomain()} 
   * @param persistent <code>true</code> if the subscription should be persistent. A persistent subscription never automatically expires.
   * Instead it lives on until explicitly cancelled by the user via JMX.
   * @return Tick distribution specification 
   */
  String subscribe(LiveDataSpecificationImpl qualifiedSpecification, boolean persistent) {
    String tickDistributionSpec = getDistributionSpecificationResolver().getDistributionSpecification(qualifiedSpecification);
    
    _subscriptionLock.lock();
    try {
      if (isSubscribedTo(qualifiedSpecification)) {
        s_logger.info("Already subscribed to {}", qualifiedSpecification);
        
        // Might be necessary to turn the subscription into a persistent one. We
        // never turn it back from persistent to non-persistent, however.
        Subscription subscription = getSubscription(qualifiedSpecification);
        if (!subscription.isPersistent() && persistent) {
          subscription.setPersistent(true);
          
          for (SubscriptionListener listener : _subscriptionListeners) {
            try {
              listener.madePersistent(subscription);
            } catch (RuntimeException e) {
              s_logger.error("Listener subscriptionChanged failed", e);
            }
          }
        }
        
      } else {
        
        String securityUniqueId = qualifiedSpecification.getIdentifier(getUniqueIdDomain());
        if (securityUniqueId == null) {
          throw new IllegalArgumentException("Qualified spec " + qualifiedSpecification + " does not contain ID of domain " + getUniqueIdDomain());          
        }
        Object subscriptionHandle = doSubscribe(securityUniqueId);
        
        Subscription subscription = new Subscription(securityUniqueId, 
              subscriptionHandle, 
              tickDistributionSpec,
              persistent);
                    
        _currentlyActiveSubscriptions.add(subscription);
        _securityUniqueId2FullyQualifiedSpecification.put(securityUniqueId, qualifiedSpecification);
        _fullyQualifiedSpec2Subscription.put(qualifiedSpecification, subscription);

        for (SubscriptionListener listener : _subscriptionListeners) {
          try {
            listener.subscribed(subscription);
          } catch (RuntimeException e) {
            s_logger.error("Listener subscribe failed", e);
          }
        }
      }
    } finally {
      _subscriptionLock.unlock();          
    }
    
    return tickDistributionSpec;
  }
  
  /**
   * Processes a market data subscription request by going through the steps of resolution,
   * entitlement check, and subscription.
   */
  public LiveDataSubscriptionResponseMsg subscriptionRequestMade(LiveDataSubscriptionRequest subscriptionRequest) {
    
    ArrayList<LiveDataSubscriptionResponse> responses = new ArrayList<LiveDataSubscriptionResponse>();
    for (LiveDataSpecificationImpl requestedSpecification : subscriptionRequest.getSpecifications()) {
      
      try {
    
        // Resolution
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
        LiveDataSpecificationImpl localFullyQualifiedSpecification = new LiveDataSpecificationImpl(qualifiedSpecification);
        
        // Entitlement check
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
        
        // Subscribe
        String tickDistributionSpec = subscribe(localFullyQualifiedSpecification, subscriptionRequest.getPersistent());

        LiveDataSubscriptionResponse response = new LiveDataSubscriptionResponse(requestedSpecification, new LiveDataSpecificationImpl(qualifiedSpecification), LiveDataSubscriptionResult.SUCCESS, null, tickDistributionSpec);
        responses.add(response);
        
      } catch (Exception e) {
        s_logger.error("Failed to subscribe to " + requestedSpecification, e);
        responses.add(new LiveDataSubscriptionResponse(requestedSpecification, null, LiveDataSubscriptionResult.INTERNAL_ERROR, e.getMessage(), null));                
      }
      
    }
    
    return new LiveDataSubscriptionResponseMsg(subscriptionRequest.getUserName(), responses);
  }
  
  /**
   * Unsubscribes from market data.
   * Works even if the subscription is persistent.
   * 
   * @return true if a market data subscription was actually removed. false otherwise.
   */
  boolean unsubscribe(String securityUniqueId) {
    Subscription sub = getSubscription(securityUniqueId);
    if (sub == null) {
      return false;
    }
    sub.setPersistent(false); // make sure it will actually be deleted
    return unsubscribe(sub);
  }
  
  /**
   * Unsubscribes from market data.
   * If the subscription is persistent, this method is a no-op.
   * 
   * @return true if a market data subscription was actually removed. false otherwise.  
   */
  boolean unsubscribe(Subscription subscription) {
    ArgumentChecker.checkNotNull(subscription, "Subscription");
    
    boolean actuallyUnsubscribed = false;
    
    _subscriptionLock.lock();
    try {
      if (isSubscribedTo(subscription) && !subscription.isPersistent()) {
        
        s_logger.info("Unsubscribing from {}", subscription);
        
        doUnsubscribe(subscription.getHandle());
        actuallyUnsubscribed = true;
        
        _currentlyActiveSubscriptions.remove(subscription);
        _securityUniqueId2FullyQualifiedSpecification.remove(subscription.getSecurityUniqueId());
        _fullyQualifiedSpec2Subscription.remove(subscription);
        
        for (SubscriptionListener listener : _subscriptionListeners) {
          try {
            listener.unsubscribed(subscription);
          } catch (RuntimeException e) {
            s_logger.error("Listener unsubscribe failed", e);
          }
        }
        
        s_logger.info("Unsubscribed from {}", subscription);
        
      } else {
        s_logger.warn("Received unsubscription request for non-active/persistent subscription: {}", subscription);
      }
            
    } finally {
      _subscriptionLock.unlock();          
    }
    
    return actuallyUnsubscribed;
  }
  
  public boolean isSubscribedTo(LiveDataSpecification fullyQualifiedSpec) {
    return _fullyQualifiedSpec2Subscription.containsKey(new LiveDataSpecificationImpl(fullyQualifiedSpec));
  }
  
  boolean isSubscribedTo(Subscription subscription) {
    return _currentlyActiveSubscriptions.contains(subscription);
  }
  
  public LiveDataSpecificationImpl getFullyQualifiedSpec(String securityUniqueId) {
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
    for (Subscription subscription : _currentlyActiveSubscriptions) {
      subscriptions.add(subscription.getDistributionSpecification());                
    }
    return subscriptions;
  }

  @Override
  public Set<String> getActiveSubscriptionIds() {
    Set<String> subscriptions = new HashSet<String>();
    for (Subscription subscription : _currentlyActiveSubscriptions) {
      subscriptions.add(subscription.getSecurityUniqueId());                
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
  
  Set<Subscription> getSubscriptions() {
    return _currentlyActiveSubscriptions;   
  }
  
  Subscription getSubscription(LiveDataSpecificationImpl spec) {
    return _fullyQualifiedSpec2Subscription.get(spec);  
  }
  
  Subscription getSubscription(String securityUniqueId) {
    LiveDataSpecificationImpl spec = getFullyQualifiedSpec(securityUniqueId);
    if (spec == null) {
      return null;
    }
    return getSubscription(spec);     
  }
  
}
