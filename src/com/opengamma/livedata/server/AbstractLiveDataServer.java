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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.livedata.entitlement.PermissiveLiveDataEntitlementChecker;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.msg.SubscriptionType;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.resolver.NaiveDistributionSpecificationResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PerformanceCounter;

/**
 * The base class from which most OpenGamma Live Data feed servers should
 * extend. Handles most common cases for distributed contract management.
 * 
 * @author kirk
 */
public abstract class AbstractLiveDataServer implements Lifecycle {
  private static final Logger s_logger = LoggerFactory
      .getLogger(AbstractLiveDataServer.class);
  
  private final Collection<MarketDataSender> _marketDataSenders = new CopyOnWriteArrayList<MarketDataSender>();
  private final Collection<SubscriptionListener> _subscriptionListeners = new CopyOnWriteArrayList<SubscriptionListener>();
  private final Set<Subscription> _currentlyActiveSubscriptions = new CopyOnWriteArraySet<Subscription>();
  private final Map<String, Subscription> _securityUniqueId2Subscription = new ConcurrentHashMap<String, Subscription>();
  private final Map<LiveDataSpecification, Subscription> _fullyQualifiedSpec2Subscription = new ConcurrentHashMap<LiveDataSpecification, Subscription>();

  private final AtomicLong _numMarketDataUpdatesReceived = new AtomicLong(0);
  private final PerformanceCounter _performanceCounter = new PerformanceCounter(60);

  private final Lock _subscriptionLock = new ReentrantLock();

  private DistributionSpecificationResolver _distributionSpecificationResolver = new NaiveDistributionSpecificationResolver();
  private LiveDataEntitlementChecker _entitlementChecker = new PermissiveLiveDataEntitlementChecker();
  
  private volatile ConnectionStatus _connectionStatus = ConnectionStatus.NOT_CONNECTED;

  /**
   * @return the distributionSpecificationResolver
   */
  public DistributionSpecificationResolver getDistributionSpecificationResolver() {
    return _distributionSpecificationResolver;
  }

  /**
   * @param distributionSpecificationResolver
   *          the distributionSpecificationResolver to set
   */
  public void setDistributionSpecificationResolver(
      DistributionSpecificationResolver distributionSpecificationResolver) {
    _distributionSpecificationResolver = distributionSpecificationResolver;
  }
  
  public void addMarketDataSender(MarketDataSender fieldReceiver) {
    ArgumentChecker.notNull(fieldReceiver, "Market Data Sender");
    _marketDataSenders.add(fieldReceiver);
  }

  public void setMarketDataSenders(
      Collection<MarketDataSender> marketDataSenders) {
    _marketDataSenders.clear();
    for (MarketDataSender sender : marketDataSenders) {
      addMarketDataSender(sender);
    }
  }
  
  public Collection<MarketDataSender> getMarketDataSenders() {
    return Collections.unmodifiableCollection(_marketDataSenders);
  }

  public void addSubscriptionListener(SubscriptionListener subscriptionListener) {
    ArgumentChecker.notNull(subscriptionListener, "Subscription Listener");
    _subscriptionListeners.add(subscriptionListener);
  }

  public void setSubscriptionListeners(
      Collection<SubscriptionListener> subscriptionListeners) {
    _subscriptionListeners.clear();
    for (SubscriptionListener subscriptionListener : subscriptionListeners) {
      addSubscriptionListener(subscriptionListener);
    }
  }

  /**
   * @return the entitlementChecker
   */
  public LiveDataEntitlementChecker getEntitlementChecker() {
    return _entitlementChecker;
  }

  /**
   * @param entitlementChecker
   *          the entitlementChecker to set
   */
  public void setEntitlementChecker(
      LiveDataEntitlementChecker entitlementChecker) {
    _entitlementChecker = entitlementChecker;
  }
  
  public String getDefaultNormalizationRuleSetId() {
    return StandardRules.getOpenGammaRuleSetId();
  }

  /**
   * Subscribes to the given ticker using the underlying market
   * data provider.
   * 
   * @return Subscription handle
   */
  protected abstract Object doSubscribe(String uniqueId);

  /**
   * @param subscriptionHandle
   *          The object that was returned by subscribe()
   */
  protected abstract void doUnsubscribe(Object subscriptionHandle);
  
  /**
   * Returns an image (i.e., all fields) from the underlying market data provider.
   * 
   * @throws RuntimeException If the snapshot could not be obtained.
   */
  protected abstract FudgeFieldContainer doSnapshot(String uniqueId);

  /**
   * @return Identification domain that uniquely identifies securities for this
   *         type of server.
   */
  protected abstract IdentificationScheme getUniqueIdDomain();
  
  /**
   * Connects to the underlying market data provider.
   * You can rely on the fact that this method is only
   * called when getConnectionStatus() == ConnectionStatus.NOT_CONNECTED.
   */
  protected abstract void doConnect();
  
  /**
   * Connects to the underlying market data provider.
   * You can rely on the fact that this method is only
   * called when getConnectionStatus() == ConnectionStatus.CONNECTED.
   */
  protected abstract void doDisconnect();
  
  
  
  
  
  public enum ConnectionStatus {
    CONNECTED, NOT_CONNECTED
  }
  
  public ConnectionStatus getConnectionStatus() {
    return _connectionStatus;
  }
  
  private void setConnectionStatus(ConnectionStatus connectionStatus) {
    _connectionStatus = connectionStatus;
    s_logger.info("Connection status changed to " + connectionStatus);
  }
  
  void reestablishSubscriptions() {
    for (Subscription subscription : getSubscriptions()) {
      try {
        Object handle = doSubscribe(subscription.getSecurityUniqueId());
        subscription.setHandle(handle);
      } catch (RuntimeException e) {
        s_logger.error("Could not reestablish subscription to " + subscription, e);        
      }
    }
  }
  
  private void verifyConnectionOk() {
    if (getConnectionStatus() == ConnectionStatus.NOT_CONNECTED) {
      throw new IllegalStateException("Connection to market data API down");
    }
  }
  
  @Override
  public synchronized boolean isRunning() {
    return getConnectionStatus() == ConnectionStatus.CONNECTED; 
  }

  @Override
  public synchronized void start() {
    if (getConnectionStatus() == ConnectionStatus.NOT_CONNECTED) {
      connect();
    }
  }
  
  @Override
  public synchronized void stop() {
    if (getConnectionStatus() == ConnectionStatus.CONNECTED) {
      disconnect();
    }
  }
  
  public void connect() {
    if (getConnectionStatus() != ConnectionStatus.NOT_CONNECTED) {
      throw new IllegalStateException("Can only connect if not connected");      
    }
    doConnect();
    setConnectionStatus(ConnectionStatus.CONNECTED);
  }
  
  public void disconnect() {
    if (getConnectionStatus() != ConnectionStatus.CONNECTED) {
      throw new IllegalStateException("Can only disconnect if connected");      
    }
    doDisconnect();
    setConnectionStatus(ConnectionStatus.NOT_CONNECTED);
  }

  

  
  public DistributionSpecification subscribe(String securityUniqueId) {
    return subscribe(securityUniqueId, false);
  }
  
  public DistributionSpecification subscribe(String securityUniqueId, boolean persistent) {
    return subscribe(
        new LiveDataSpecification(
            getDefaultNormalizationRuleSetId(),
            new Identifier(getUniqueIdDomain(), securityUniqueId)), 
        persistent);    
  }
  
  public DistributionSpecification subscribe(LiveDataSpecification liveDataSpecificationFromClient, 
      boolean persistent) {
    
    verifyConnectionOk();
    
    DistributionSpecification distributionSpec = getDistributionSpecificationResolver()
        .getDistributionSpecification(liveDataSpecificationFromClient);
    LiveDataSpecification fullyQualifiedSpec = distributionSpec.getFullyQualifiedLiveDataSpecification();

    _subscriptionLock.lock();
    try {
      Subscription subscription;
      
      if (isSubscribedTo(fullyQualifiedSpec)) {
        s_logger.info("Already subscribed to {}", fullyQualifiedSpec);

        // Might be necessary to turn the subscription into a persistent one. We
        // never turn it back from persistent to non-persistent, however.
        subscription = getSubscription(fullyQualifiedSpec);
        if (!subscription.isPersistent() && persistent) {
          changePersistent(subscription, true);
        }

      } else {

        String securityUniqueId = fullyQualifiedSpec.getIdentifier(getUniqueIdDomain());
        if (securityUniqueId == null) {
          throw new IllegalArgumentException("Qualified spec "
              + fullyQualifiedSpec + " does not contain ID of domain "
              + getUniqueIdDomain());
        }
        
        // REVIEW kirk 2010-04-16 -- There's a potential race condition here:
        // - Get snapshot time t1
        // - Start subscription
        // - First tick comes in at t2
        // - Pump the snapshot through the system
        // In this case, we have the tick at t2 hitting the chain before the snapshot.
        // However, while this is a theoretical race condition, I'm not sure whether in practice
        // it justifies extended fixes at this time.
        
        // First, grab a snapshot of the data, BEFORE the background subscription is started.
        FudgeFieldContainer snapshot = doSnapshot(securityUniqueId);

        // Setup the subscription in the underlying data provider.
        Object subscriptionHandle = doSubscribe(securityUniqueId);

        // Setup the subscription.
        subscription = new Subscription(securityUniqueId, subscriptionHandle, 
            persistent);

        _currentlyActiveSubscriptions.add(subscription);
        _securityUniqueId2Subscription.put(securityUniqueId,
            subscription);
        
        // Pump the snapshot before telling listeners.
        liveDataReceived(securityUniqueId, snapshot);

        for (SubscriptionListener listener : _subscriptionListeners) {
          try {
            listener.subscribed(subscription);
          } catch (RuntimeException e) {
            s_logger.error("Listener subscribe failed", e);
          }
        }
        
        s_logger.info("Created subscription to {}", fullyQualifiedSpec);
      }
      
      _fullyQualifiedSpec2Subscription.put(fullyQualifiedSpec,
          subscription);
      
      subscription.createDistribution(distributionSpec, getMarketDataSenders());
    
    } finally {
      _subscriptionLock.unlock();
    }

    return distributionSpec;
  }
  
  /**
   * Returns a snapshot of the requested market data.
   * If the server already subscribes to the market data,
   * the last known value from that subscription is used.
   * Otherwise a snapshot is requested from the underlying market data API.
   * 
   * TODO. This needs to be optimized for batch snapshot.
   * 
   * @return Never null.
   * @throws RuntimeException If no snapshot could be obtained
   */
  public FudgeFieldContainer snapshot(LiveDataSpecification liveDataSpecificationFromClient) {
    verifyConnectionOk();
    
    DistributionSpecification distributionSpec = getDistributionSpecificationResolver()
      .getDistributionSpecification(liveDataSpecificationFromClient);
    LiveDataSpecification fullyQualifiedSpec = distributionSpec.getFullyQualifiedLiveDataSpecification();
    
    MarketDataDistributor currentlyActiveDistributor = getMarketDataDistributor(distributionSpec);
    if (currentlyActiveDistributor != null 
        && currentlyActiveDistributor.getLastKnownValue() != null) {
      return currentlyActiveDistributor.getLastKnownValue();
    }
    
    String securityUniqueId = fullyQualifiedSpec.getIdentifier(getUniqueIdDomain());
    if (securityUniqueId == null) {
      throw new IllegalArgumentException("Qualified spec "
          + fullyQualifiedSpec + " does not contain ID of domain "
          + getUniqueIdDomain());
    }

    FudgeFieldContainer msg = doSnapshot(securityUniqueId);
    
    FudgeFieldContainer normalizedMsg = distributionSpec.getNormalizedMessage(msg);
    return normalizedMsg;
  }
  
  /**
   * Processes a market data subscription request by going through the steps of
   * resolution, entitlement check, and subscription.
   */
  public LiveDataSubscriptionResponseMsg subscriptionRequestMade(
      LiveDataSubscriptionRequest subscriptionRequest) {

    ArrayList<LiveDataSubscriptionResponse> responses = new ArrayList<LiveDataSubscriptionResponse>();
    for (LiveDataSpecification requestedSpecification : subscriptionRequest
        .getSpecifications()) {

      try {

        // Check that this spec can be found
        DistributionSpecification distributionSpec;
        try {
          distributionSpec = getDistributionSpecificationResolver()
            .getDistributionSpecification(requestedSpecification);
        } catch (IllegalArgumentException e) {
          s_logger.info("Unable to work out distribution spec for specification {}",
              requestedSpecification);
          responses.add(new LiveDataSubscriptionResponse(
              requestedSpecification,
              LiveDataSubscriptionResult.NOT_PRESENT,
              e.getMessage(),
              null,
              null, 
              null));
          continue;
        }

        // Entitlement check
        if (!getEntitlementChecker().isEntitled(
            subscriptionRequest.getUserName(), distributionSpec)) {
          s_logger.info("User {} not entitled to specification {}",
              subscriptionRequest.getUserName(), requestedSpecification);
          // TODO kirk 2009-10-28 -- Extend interface on EntitlementChecker to
          // get a user message.
          responses.add(new LiveDataSubscriptionResponse(
              requestedSpecification,
              LiveDataSubscriptionResult.NOT_AUTHORIZED,
              null,
              distributionSpec.getFullyQualifiedLiveDataSpecification(),
              null,
              null));
          continue;
        }

        // Subscribe
        LiveDataSubscriptionResponse response;
        if (subscriptionRequest.getType() == SubscriptionType.SNAPSHOT) {
          
          FudgeFieldContainer snapshot = snapshot(requestedSpecification);
          response = new LiveDataSubscriptionResponse(
              requestedSpecification,
              LiveDataSubscriptionResult.SUCCESS,
              null,
              distributionSpec.getFullyQualifiedLiveDataSpecification(), 
              null,
              snapshot);
          
        } else {
          
          boolean persistent = subscriptionRequest.getType().equals(SubscriptionType.PERSISTENT);
          distributionSpec = subscribe(requestedSpecification, persistent);
          response = new LiveDataSubscriptionResponse(
              requestedSpecification,
              LiveDataSubscriptionResult.SUCCESS,
              null,
              distributionSpec.getFullyQualifiedLiveDataSpecification(), 
              distributionSpec.getJmsTopic(),
              null);
           
        }
        responses.add(response);

      } catch (Exception e) {
        s_logger.error("Failed to subscribe to " + requestedSpecification, e);
        responses.add(new LiveDataSubscriptionResponse(requestedSpecification,
            LiveDataSubscriptionResult.INTERNAL_ERROR,
            e.getMessage(),
            null,
            null,
            null));
      }

    }

    return new LiveDataSubscriptionResponseMsg(subscriptionRequest
        .getUserName(), responses);
  }

  /**
   * Unsubscribes from market data.
   * Works even if the subscription is persistent.
   * 
   * @return true if a market data subscription was actually removed. false
   *         otherwise.
   */
  boolean unsubscribe(String securityUniqueId) {
    Subscription sub = getSubscription(securityUniqueId);
    if (sub == null) {
      return false;
    }
    changePersistent(sub, false); // make sure it will actually be deleted
    return unsubscribe(sub);
  }

  /**
   * Unsubscribes from market data.
   * If the subscription is persistent, this method is a no-op.
   * 
   * @return true if a market data subscription was actually removed. false
   *         otherwise.
   */
  boolean unsubscribe(Subscription subscription) {
    ArgumentChecker.notNull(subscription, "Subscription");
    verifyConnectionOk();

    boolean actuallyUnsubscribed = false;

    _subscriptionLock.lock();
    try {
      if (isSubscribedTo(subscription) && !subscription.isPersistent()) {

        s_logger.info("Unsubscribing from {}", subscription);

        doUnsubscribe(subscription.getHandle());
        actuallyUnsubscribed = true;

        _currentlyActiveSubscriptions.remove(subscription);
        _securityUniqueId2Subscription.remove(subscription
            .getSecurityUniqueId());
        
        for (DistributionSpecification distributionSpec : subscription.getDistributionSpecifications()) {
          _fullyQualifiedSpec2Subscription.remove(distributionSpec.getFullyQualifiedLiveDataSpecification());
        }

        for (SubscriptionListener listener : _subscriptionListeners) {
          try {
            listener.unsubscribed(subscription);
          } catch (RuntimeException e) {
            s_logger.error("Listener unsubscribe failed", e);
          }
        }

        s_logger.info("Unsubscribed from {}", subscription);

      } else {
        s_logger
            .warn(
                "Received unsubscription request for non-active/persistent subscription: {}",
                subscription);
      }

    } finally {
      _subscriptionLock.unlock();
    }

    return actuallyUnsubscribed;
  }

  boolean changePersistent(Subscription subscription, boolean persistent) {

    boolean actuallyChanged = false;

    _subscriptionLock.lock();
    try {
      if (isSubscribedTo(subscription)
          && persistent != subscription.isPersistent()) {

        s_logger.info("Changing subscription {} persistence status to {}",
            subscription, persistent);

        subscription.setPersistent(persistent);
        actuallyChanged = true;

        for (SubscriptionListener listener : _subscriptionListeners) {
          try {
            listener.persistentChanged(subscription);
          } catch (RuntimeException e) {
            s_logger.error("Listener persistentChanged failed", e);
          }
        }

      } else {
        s_logger.warn("No-op changePersistent() received: {} {}", subscription,
            persistent);
      }

    } finally {
      _subscriptionLock.unlock();
    }

    return actuallyChanged;
  }
  
  public boolean isSubscribedTo(String securityUniqueId) {
    return _securityUniqueId2Subscription.containsKey(securityUniqueId);    
  }
  
  public boolean isSubscribedTo(LiveDataSpecification fullyQualifiedSpec) {
    return _fullyQualifiedSpec2Subscription.containsKey(fullyQualifiedSpec);
  }

  public boolean isSubscribedTo(Subscription subscription) {
    return _currentlyActiveSubscriptions.contains(subscription);
  }

  public void liveDataReceived(String securityUniqueId,
      FudgeFieldContainer liveDataFields) {
    s_logger.debug("Live data received: {}", liveDataFields);

    _numMarketDataUpdatesReceived.incrementAndGet();
    _performanceCounter.hit();
    
    Subscription subscription = getSubscription(securityUniqueId);
    if (subscription == null) {
      s_logger.warn("Got data for invalid security unique ID {}", securityUniqueId);
      return;
    }
    
    subscription.liveDataReceived(liveDataFields);
  }

  public Set<String> getActiveDistributionSpecs() {
    Set<String> subscriptions = new HashSet<String>();
    for (Subscription subscription : _currentlyActiveSubscriptions) {
      for (DistributionSpecification distributionSpec : subscription.getDistributionSpecifications()) {
        subscriptions.add(distributionSpec.toString());
      }
    }
    return subscriptions;
  }

  public Set<String> getActiveSubscriptionIds() {
    Set<String> subscriptions = new HashSet<String>();
    for (Subscription subscription : _currentlyActiveSubscriptions) {
      subscriptions.add(subscription.getSecurityUniqueId());
    }
    return subscriptions;
  }

  public int getNumActiveSubscriptions() {
    return _currentlyActiveSubscriptions.size();
  }

  public long getNumMarketDataUpdatesReceived() {
    return _numMarketDataUpdatesReceived.get();
  }
  
  public double getNumLiveDataUpdatesSentPerSecondOverLastMinute() {
    return _performanceCounter.getHitsPerSecond();
  }

  public Set<Subscription> getSubscriptions() {
    return Collections.unmodifiableSet(_currentlyActiveSubscriptions);
  }

  public Subscription getSubscription(LiveDataSpecification fullyQualifiedSpec) {
    return _fullyQualifiedSpec2Subscription.get(fullyQualifiedSpec);
  }

  public Subscription getSubscription(String securityUniqueId) {
    return _securityUniqueId2Subscription.get(securityUniqueId);
  }
  
  public MarketDataDistributor getMarketDataDistributor(DistributionSpecification distributionSpec) {
    Subscription subscription = getSubscription(distributionSpec.getFullyQualifiedLiveDataSpecification());
    if (subscription == null) {
      return null;
    }
    return subscription.getMarketDataDistributor(distributionSpec);
  }
  
}
