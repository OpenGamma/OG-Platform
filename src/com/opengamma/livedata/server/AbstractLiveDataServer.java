/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
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
import com.opengamma.livedata.server.distribution.EmptyMarketDataSenderFactory;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.livedata.server.distribution.MarketDataSenderFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PerformanceCounter;

/**
 * The base class from which most OpenGamma Live Data feed servers should
 * extend. Handles most common cases for distributed contract management.
 * 
 */
public abstract class AbstractLiveDataServer implements Lifecycle {
  private static final Logger s_logger = LoggerFactory
      .getLogger(AbstractLiveDataServer.class);
  
  private volatile MarketDataSenderFactory _marketDataSenderFactory = new EmptyMarketDataSenderFactory();
  private final Collection<SubscriptionListener> _subscriptionListeners = new CopyOnWriteArrayList<SubscriptionListener>();
  
  /** Access controlled via _subscriptionLock */
  private final Set<Subscription> _currentlyActiveSubscriptions = new HashSet<Subscription>();
  
  /** Access controlled via _subscriptionLock */
  private final Map<String, Subscription> _securityUniqueId2Subscription = new HashMap<String, Subscription>();
  
  /** Access controlled via _subscriptionLock */
  private final Map<LiveDataSpecification, MarketDataDistributor> _fullyQualifiedSpec2Distributor = new HashMap<LiveDataSpecification, MarketDataDistributor>();

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
  
  public MarketDataSenderFactory getMarketDataSenderFactory() {
    return _marketDataSenderFactory;
  }
  
  public void setMarketDataSenderFactory(MarketDataSenderFactory marketDataSenderFactory) {
    _marketDataSenderFactory = marketDataSenderFactory;
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
   * Subscribes to the given ticker(s) using the underlying market
   * data provider.
   * <p>
   * The return value is a map from unique ID to subscription handle.
   * The map must contain an entry for each <code>uniqueId</code>.
   * Failure to subscribe to any <code>uniqueId</code> should result in an exception being thrown. 
   * 
   * @param uniqueIds A collection of unique IDs. Not null. May be empty.
   * @return Subscription handles corresponding to the unique IDs.
   * @throws RuntimeException If subscribing to any unique IDs failed.
   */
  protected abstract Map<String, Object> doSubscribe(Collection<String> uniqueIds);

  /**
   * Unsubscribes to the given ticker(s) using the underlying market
   * data provider.
   *  
   * @param subscriptionHandles
   *          Subscription handle(s) returned by {@link #doSubscribe(Collection uniqueIds)}.
   *          Not null. May be empty.
   */
  protected abstract void doUnsubscribe(Collection<Object> subscriptionHandles);
  
  /**
   * Returns an image (i.e., all fields) from the underlying market data provider.
   * 
   * @return The return value is a map from unique ID to subscription handle.
   * The map must contain an entry for each <code>uniqueId</code>.
   * Failure to snapshot any <code>uniqueId</code> should result in an exception being thrown. 
   * @param uniqueIds Not null. May be empty.
   * @throws RuntimeException If the snapshot could not be obtained.
   */
  protected abstract Map<String, FudgeFieldContainer> doSnapshot(Collection<String> uniqueIds);

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
  
  /**
   * In some cases, the underlying market data API may not, when a subscription is created,
   * return a full image of all fields. If so, we need to get the full image explicitly.
   * 
   * @param subscription The subscription currently being created 
   * @return true if a snapshot should be made when a new subscription is created, false otherwise. 
   */
  protected abstract boolean snapshotOnSubscriptionStartRequired(Subscription subscription);
  
  /**
   * Whether the server is connected to underlying market data API or not
   */
  public enum ConnectionStatus {
    /** Connection established */
    CONNECTED,
    /** Connection not established */
    NOT_CONNECTED
  }
  
  public ConnectionStatus getConnectionStatus() {
    return _connectionStatus;
  }
  
  void setConnectionStatus(ConnectionStatus connectionStatus) {
    _connectionStatus = connectionStatus;
    s_logger.info("Connection status changed to " + connectionStatus);
    
    if (connectionStatus == ConnectionStatus.NOT_CONNECTED) {
      for (Subscription subscription : getSubscriptions()) {
        subscription.setHandle(null);        
      }
    }
  }
  
  void reestablishSubscriptions() {
    for (Subscription subscription : getSubscriptions()) {
      try {
        Object handle = doSubscribe(Collections.singleton(subscription.getSecurityUniqueId()));
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
  
  public synchronized void connect() {
    if (getConnectionStatus() != ConnectionStatus.NOT_CONNECTED) {
      throw new IllegalStateException("Can only connect if not connected");      
    }
    doConnect();
    setConnectionStatus(ConnectionStatus.CONNECTED);
  }
  
  public synchronized void disconnect() {
    if (getConnectionStatus() != ConnectionStatus.CONNECTED) {
      throw new IllegalStateException("Can only disconnect if connected");      
    }
    doDisconnect();
    setConnectionStatus(ConnectionStatus.NOT_CONNECTED);
  }
  
  /**
   * @param securityUniqueId Security unique ID
   * @return A {@code LiveDataSpecification} with default normalization
   * rule used.
   */
  public LiveDataSpecification getLiveDataSpecification(String securityUniqueId) {
    LiveDataSpecification liveDataSpecification = new LiveDataSpecification(
        getDefaultNormalizationRuleSetId(),
        new Identifier(getUniqueIdDomain(), securityUniqueId));
    return liveDataSpecification;
  }

  /**
   * Subscribes to the market data and creates a default distributor.
   *
   * @param securityUniqueId Security unique ID
   * @return Whether the subscription succeeded or failed
   * @see #getDefaultNormalizationRuleSetId()
   */
  public SubscriptionResult subscribe(String securityUniqueId) {
    return subscribe(securityUniqueId, false);
  }
  
  /**
   * Subscribes to the market data and creates a default distributor.
   *
   * @param securityUniqueId Security unique ID
   * @param persistent See {@link MarketDataDistributor#isPersistent()}
   * @return Whether the subscription succeeded or failed
   * @see #getDefaultNormalizationRuleSetId()
   */
  public SubscriptionResult subscribe(String securityUniqueId, boolean persistent) {
    LiveDataSpecification liveDataSpecification = getLiveDataSpecification(securityUniqueId);
    return subscribe(liveDataSpecification, persistent);
  }
  
  public SubscriptionResult subscribe(LiveDataSpecification liveDataSpecificationFromClient,
      boolean persistent) {
    
    Map<LiveDataSpecification, SubscriptionResult> resultMap = subscribe(
        Collections.singleton(liveDataSpecificationFromClient), 
        persistent);
    
    SubscriptionResult result = resultMap.get(liveDataSpecificationFromClient);
    if (result == null) {
      throw new OpenGammaRuntimeException("subscribe() did not fulfill its contract to populate map for each live data spec");      
    }
    return result;
  }
  
  public Map<LiveDataSpecification, SubscriptionResult> subscribe(Collection<LiveDataSpecification> liveDataSpecificationsFromClient, 
      boolean persistent) {
    ArgumentChecker.notNull(liveDataSpecificationsFromClient, "Subscriptions to be created");
    
    s_logger.info("Subscribe requested for {}, persistent = {}", liveDataSpecificationsFromClient, persistent);
    
    verifyConnectionOk();
    
    Map<LiveDataSpecification, SubscriptionResult> liveDataSpecFromClient2Result = new HashMap<LiveDataSpecification, SubscriptionResult>();
    Map<String, Subscription> securityUniqueId2NewSubscription = new HashMap<String, Subscription>();
    Map<String, LiveDataSpecification> securityUniqueId2SpecFromClient = new HashMap<String, LiveDataSpecification>();
    
    _subscriptionLock.lock();
    try {
    
      for (LiveDataSpecification specFromClient : liveDataSpecificationsFromClient) {
        
        // this is the only place where subscribe() can 'partially' fail
        DistributionSpecification distributionSpec;
        try {
          distributionSpec = getDistributionSpecificationResolver().getDistributionSpecification(specFromClient);
        } catch (RuntimeException e) {
          s_logger.info("Unable to work out distribution spec for specification " + specFromClient, e);
          liveDataSpecFromClient2Result.put(specFromClient, new SubscriptionResult(specFromClient, 
              null, 
              LiveDataSubscriptionResult.NOT_PRESENT, 
              e));                    
          continue;
        }
        
        LiveDataSpecification fullyQualifiedSpec = distributionSpec.getFullyQualifiedLiveDataSpecification();
      
        Subscription subscription = getSubscription(fullyQualifiedSpec);
        if (subscription != null) {
          s_logger.info("Already subscribed to {}", fullyQualifiedSpec);
          
          subscription.createDistributor(distributionSpec, persistent);
    
          liveDataSpecFromClient2Result.put(specFromClient, new SubscriptionResult(specFromClient, 
              distributionSpec, 
              LiveDataSubscriptionResult.SUCCESS, 
              null));                    
    
        } else {
    
          String securityUniqueId = fullyQualifiedSpec.getIdentifier(getUniqueIdDomain());
          if (securityUniqueId == null) {
            throw new IllegalArgumentException("Qualified spec "
                + fullyQualifiedSpec + " does not contain ID of domain "
                + getUniqueIdDomain());
          }
          
          subscription = new Subscription(securityUniqueId, getMarketDataSenderFactory());
          subscription.createDistributor(distributionSpec, persistent);
          securityUniqueId2NewSubscription.put(subscription.getSecurityUniqueId(), subscription);
          securityUniqueId2SpecFromClient.put(subscription.getSecurityUniqueId(), specFromClient);
        }
      }
      
      // In some cases, the underlying market data API may not, when the subscription is started,
      // return a full image of all fields. If so, we need to get the full image explicitly.
      Collection<String> newSubscriptionsForWhichSnapshotIsRequired = new ArrayList<String>();
      for (Subscription subscription : securityUniqueId2NewSubscription.values()) {
        if (snapshotOnSubscriptionStartRequired(subscription)) {
          newSubscriptionsForWhichSnapshotIsRequired.add(subscription.getSecurityUniqueId());
        }
      }
      
      s_logger.info("Subscription snapshot required for {}", newSubscriptionsForWhichSnapshotIsRequired);
      Map<String, FudgeFieldContainer> snapshots = doSnapshot(newSubscriptionsForWhichSnapshotIsRequired);
      for (Map.Entry<String, FudgeFieldContainer> snapshot : snapshots.entrySet()) {
        Subscription subscription = securityUniqueId2NewSubscription.get(snapshot.getKey());
        subscription.initialSnapshotReceived(snapshot.getValue());
      }
    
      // Setup the subscriptions in the underlying data provider.
      for (Subscription subscription : securityUniqueId2NewSubscription.values()) {
        // this is necessary so we don't lose any updates immediately after doSubscribe(). See AbstractLiveDataServer#liveDataReceived()
        // and how it calls AbstractLiveDataServer#getSubscription()
        _securityUniqueId2Subscription.put(subscription.getSecurityUniqueId(), subscription); 
      }

      s_logger.info("Creating underlying market data API subscription to {}", securityUniqueId2NewSubscription.keySet());
      Map<String, Object> subscriptionHandles = doSubscribe(securityUniqueId2NewSubscription.keySet());
    
      // Set up data structures
      for (Map.Entry<String, Object> subscriptionHandle : subscriptionHandles.entrySet()) {
        String securityUniqueId = subscriptionHandle.getKey();
        Object handle = subscriptionHandle.getValue();
        LiveDataSpecification specFromClient = securityUniqueId2SpecFromClient.get(securityUniqueId);
        
        Subscription subscription = securityUniqueId2NewSubscription.get(securityUniqueId); 
        subscription.setHandle(handle);
          
        for (SubscriptionListener listener : _subscriptionListeners) {
          try {
            listener.subscribed(subscription);
          } catch (RuntimeException e) {
            s_logger.error("Listener " + listener + " subscribe failed", e);
          }
        }
          
        _currentlyActiveSubscriptions.add(subscription);

        if (subscription.getDistributionSpecifications().size() != 1) {
          throw new RuntimeException("The subscription should only have 1 distribution specification at the moment: " + subscription);
        }
        
        for (MarketDataDistributor distributor : subscription.getDistributors()) {
          _fullyQualifiedSpec2Distributor.put(distributor.getFullyQualifiedLiveDataSpecification(),
              distributor);
          
          SubscriptionResult result = new SubscriptionResult(specFromClient, 
              distributor.getDistributionSpec(), 
              LiveDataSubscriptionResult.SUCCESS, 
              null);
          liveDataSpecFromClient2Result.put(specFromClient, result);
        }
        
        s_logger.info("Created {}", subscription);
      }

    } catch (RuntimeException e) {
      
      s_logger.info("Unexpected exception thrown when subscribing. Cleaning up.");
      
      for (Subscription subscription : securityUniqueId2NewSubscription.values()) {
        _securityUniqueId2Subscription.remove(subscription.getSecurityUniqueId());
        
        for (MarketDataDistributor distributor : subscription.getDistributors()) {
          _fullyQualifiedSpec2Distributor.remove(distributor.getFullyQualifiedLiveDataSpecification());
        }
      }
      _currentlyActiveSubscriptions.removeAll(securityUniqueId2NewSubscription.values());
      
      throw e;
        
    } finally {
      _subscriptionLock.unlock();
    }

    return liveDataSpecFromClient2Result;
  }
  
  /**
   * Returns a snapshot of the requested market data.
   * If the server already subscribes to the market data,
   * the last known value from that subscription is used.
   * Otherwise a snapshot is requested from the underlying market data API.
   * 
   * @param liveDataSpecificationsFromClient What snapshot(s) are being requested. Not empty
   * @return Responses to snapshot requests. Some, or even all, of them might be failures.
   * @throws RuntimeException If no snapshot could be obtained due to unexpected error.
   */
  public Map<LiveDataSpecification, LiveDataValueUpdateBean> snapshot(Collection<LiveDataSpecification> liveDataSpecificationsFromClient) {
    ArgumentChecker.notNull(liveDataSpecificationsFromClient, "Snapshots to be obtained");
    
    s_logger.info("Snapshot requested for {}", liveDataSpecificationsFromClient);
    
    verifyConnectionOk();
    
    Map<LiveDataSpecification, LiveDataValueUpdateBean> returnValue = new HashMap<LiveDataSpecification, LiveDataValueUpdateBean>();
    
    Collection<String> snapshotsToActuallyDo = new ArrayList<String>();
    Map<String, LiveDataSpecification> securityUniqueId2LiveDataSpecificationFromClient = new HashMap<String, LiveDataSpecification>(); 
    
    for (LiveDataSpecification liveDataSpecificationFromClient : liveDataSpecificationsFromClient) {
      DistributionSpecification distributionSpec = getDistributionSpecificationResolver()
        .getDistributionSpecification(liveDataSpecificationFromClient);
      LiveDataSpecification fullyQualifiedSpec = distributionSpec.getFullyQualifiedLiveDataSpecification();
      
      MarketDataDistributor currentlyActiveDistributor = getMarketDataDistributor(distributionSpec);
      if (currentlyActiveDistributor != null 
          && currentlyActiveDistributor.getSnapshot() != null) {
        s_logger.info("Able to satisfy {} from existing LKV", liveDataSpecificationFromClient);
        returnValue.put(liveDataSpecificationFromClient, currentlyActiveDistributor.getSnapshot());
        continue;
      }
      
      String securityUniqueId = fullyQualifiedSpec.getIdentifier(getUniqueIdDomain());
      if (securityUniqueId == null) {
        throw new IllegalArgumentException("Qualified spec "
            + fullyQualifiedSpec + " does not contain ID of domain "
            + getUniqueIdDomain());
      }
      
      snapshotsToActuallyDo.add(securityUniqueId);
      securityUniqueId2LiveDataSpecificationFromClient.put(securityUniqueId, liveDataSpecificationFromClient);      
    }

    s_logger.info("Need to actually snapshot {}", snapshotsToActuallyDo);
    Map<String, FudgeFieldContainer> snapshots = doSnapshot(snapshotsToActuallyDo);
    for (Map.Entry<String, FudgeFieldContainer> snapshotEntry : snapshots.entrySet()) {
      String securityUniqueId = snapshotEntry.getKey();
      FudgeFieldContainer msg = snapshotEntry.getValue();
      
      LiveDataSpecification liveDataSpecFromClient = securityUniqueId2LiveDataSpecificationFromClient.get(securityUniqueId);
      
      DistributionSpecification distributionSpec = getDistributionSpecificationResolver()
        .getDistributionSpecification(liveDataSpecFromClient);
      FudgeFieldContainer normalizedMsg = distributionSpec.getNormalizedMessage(msg);
      
      LiveDataValueUpdateBean snapshot = new LiveDataValueUpdateBean(0, distributionSpec.getFullyQualifiedLiveDataSpecification(), normalizedMsg);
      returnValue.put(liveDataSpecFromClient, snapshot);
    }
    
    return returnValue; 
  }
  
  /**
   * If you want to force a snapshot - i.e., always a request a snapshot from the underlying API -
   * you can use this method.
   * 
   * @param securityUniqueId Security unique ID
   * @return The snapshot
   */
  public FudgeFieldContainer doSnapshot(String securityUniqueId) {
    Map<String, FudgeFieldContainer> snapshots = doSnapshot(Collections.singleton(securityUniqueId));
    FudgeFieldContainer snapshot = snapshots.get(securityUniqueId);
    if (snapshot == null) {
      throw new OpenGammaRuntimeException("doSnapshot() did not fulfill its contract to populate map for each unique ID");
    }
    return snapshot;
  }
  
  /**
   * Processes a market data subscription request by going through the steps of
   * resolution, entitlement check, and subscription.
   * 
   * @param subscriptionRequest Request from client telling what to subscribe to
   * @return LiveDataSubscriptionResponseMsg Sent back to the client of this server
   */
  public LiveDataSubscriptionResponseMsg subscriptionRequestMade(
      LiveDataSubscriptionRequest subscriptionRequest) {
    
    boolean persistent = subscriptionRequest.getType().equals(SubscriptionType.PERSISTENT);

    ArrayList<LiveDataSubscriptionResponse> responses = new ArrayList<LiveDataSubscriptionResponse>();
    
    ArrayList<LiveDataSpecification> snapshots = new ArrayList<LiveDataSpecification>();
    ArrayList<LiveDataSpecification> subscriptions = new ArrayList<LiveDataSpecification>();
    
    for (LiveDataSpecification requestedSpecification : subscriptionRequest
        .getSpecifications()) {

      try {

        // Check that this spec can be found
        DistributionSpecification distributionSpec;
        try {
          distributionSpec = getDistributionSpecificationResolver()
            .getDistributionSpecification(requestedSpecification);
        } catch (RuntimeException e) {
          s_logger.info("Unable to work out distribution spec for specification " + requestedSpecification, e);
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
            subscriptionRequest.getUser(), distributionSpec)) {
          String msg = subscriptionRequest.getUser() + " is not entitled to " + requestedSpecification;
          s_logger.info(msg);
          responses.add(new LiveDataSubscriptionResponse(
              requestedSpecification,
              LiveDataSubscriptionResult.NOT_AUTHORIZED,
              msg,
              null,
              null,
              null));
          continue;
        }

        // Pass to the right bucket by type
        if (subscriptionRequest.getType() == SubscriptionType.SNAPSHOT) {
          snapshots.add(requestedSpecification);
        } else {
          subscriptions.add(requestedSpecification);
        }

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
    
    if (!snapshots.isEmpty()) {
      try {
        Map<LiveDataSpecification, LiveDataValueUpdateBean> snapshotResponses = snapshot(snapshots);
        for (Map.Entry<LiveDataSpecification, LiveDataValueUpdateBean> snapshotResponse : snapshotResponses.entrySet()) {
          responses.add(new LiveDataSubscriptionResponse(
              snapshotResponse.getKey(),
              LiveDataSubscriptionResult.SUCCESS,
              null,
              snapshotResponse.getValue().getSpecification(),
              null,
              snapshotResponse.getValue()));      
        }
      } catch (Exception e) {
        s_logger.error("Failed to snapshot " + snapshots, e);
        
        for (LiveDataSpecification spec : snapshots) {
          responses.add(new LiveDataSubscriptionResponse(spec,
              LiveDataSubscriptionResult.INTERNAL_ERROR,
              e.getMessage(),
              null,
              null,
              null));
          
        }
      }
    }
    
    if (!subscriptions.isEmpty()) {
      try {
        Map<LiveDataSpecification, SubscriptionResult> subscriptionResults = subscribe(subscriptions, persistent);
        for (SubscriptionResult result : subscriptionResults.values()) {
          responses.add(result.toResponse());      
        }
      } catch (Exception e) {
        s_logger.error("Failed to subscribe to " + subscriptions, e);
        
        for (LiveDataSpecification spec : subscriptions) {
          responses.add(new LiveDataSubscriptionResponse(spec,
              LiveDataSubscriptionResult.INTERNAL_ERROR,
              e.getMessage(),
              null,
              null,
              null));
        }
      }
    }
    
    return new LiveDataSubscriptionResponseMsg(subscriptionRequest
        .getUser(), responses);
  }
  
  /**
   * Unsubscribes from market data. All distributors related to that
   * subscription will be stopped.
   * 
   * @param securityUniqueId Security unique ID
   * @return true if a market data subscription was actually removed. false
   *         otherwise.
   */
  public boolean unsubscribe(String securityUniqueId) {
    Subscription sub = getSubscription(securityUniqueId);
    if (sub == null) {
      return false;
    }
    return unsubscribe(sub);
  }

  /**
   * Unsubscribes from market data. All distributors related to that
   * subscription will be stopped.
   * 
   * @param subscription What to unsubscribe from
   * @return true if a market data subscription was actually removed. false
   *         otherwise.
   */
  public boolean unsubscribe(Subscription subscription) {
    ArgumentChecker.notNull(subscription, "Subscription");
    verifyConnectionOk();

    boolean actuallyUnsubscribed = false;

    _subscriptionLock.lock();
    try {
      if (isSubscribedTo(subscription)) {

        s_logger.info("Unsubscribing from {}", subscription);

        actuallyUnsubscribed = true;
        
        Object subscriptionHandle = subscription.getHandle();
        if (subscriptionHandle != null) {
          doUnsubscribe(Collections.singleton(subscriptionHandle)); // todo, optimize to use batch
        }

        _currentlyActiveSubscriptions.remove(subscription);
        _securityUniqueId2Subscription.remove(subscription
            .getSecurityUniqueId());
        
        for (MarketDataDistributor distributor : subscription.getDistributors()) {
          _fullyQualifiedSpec2Distributor.remove(distributor.getFullyQualifiedLiveDataSpecification());
        }
        subscription.removeAllDistributors();

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
                "Received unsubscription request for non-active subscription: {}",
                subscription);
      }

    } finally {
      _subscriptionLock.unlock();
    }

    return actuallyUnsubscribed;
  }
  
  /**
   * Stops a market data distributor. If the distributor is
   * persistent, this call will be a no-op. If you want
   * to stop a persistent distributor, make it non-persistent first.  
   * <p>
   * If the subscription to which the distributor belongs no longer 
   * has any active distributors after this, that subscription will be deleted.
   * 
   * @param distributor The distributor to stop
   * @return true if a distributor was actually stopped. false
   *         otherwise.
   */
  public boolean stopDistributor(MarketDataDistributor distributor) {
    ArgumentChecker.notNull(distributor, "Distributor");
    
    _subscriptionLock.lock();
    try {
      MarketDataDistributor realDistributor = getMarketDataDistributor(distributor.getDistributionSpec());
      if (realDistributor != distributor) {
        return false;                        
      }
      
      if (distributor.isPersistent()) {
        return false;
      }
      
      distributor.getSubscription().removeDistributor(distributor);
      _fullyQualifiedSpec2Distributor.remove(distributor.getFullyQualifiedLiveDataSpecification());
      
      if (distributor.getSubscription().getDistributors().isEmpty()) {
        unsubscribe(distributor.getSubscription());                
      }
      
    } finally {
      _subscriptionLock.unlock();
    }

    return true;
  }

  public boolean isSubscribedTo(String securityUniqueId) {
    _subscriptionLock.lock();
    try {
      return _securityUniqueId2Subscription.containsKey(securityUniqueId);
    } finally {
      _subscriptionLock.unlock();
    }
  }
  
  public boolean isSubscribedTo(LiveDataSpecification fullyQualifiedSpec) {
    _subscriptionLock.lock();
    try {
      return _fullyQualifiedSpec2Distributor.containsKey(fullyQualifiedSpec);
    } finally {
      _subscriptionLock.unlock();
    }
  }

  public boolean isSubscribedTo(Subscription subscription) {
    return getSubscriptions().contains(subscription);
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
    for (Subscription subscription : getSubscriptions()) {
      for (DistributionSpecification distributionSpec : subscription.getDistributionSpecifications()) {
        subscriptions.add(distributionSpec.toString());
      }
    }
    return subscriptions;
  }

  public Set<String> getActiveSubscriptionIds() {
    Set<String> subscriptions = new HashSet<String>();
    for (Subscription subscription : getSubscriptions()) {
      subscriptions.add(subscription.getSecurityUniqueId());
    }
    return subscriptions;
  }

  public int getNumActiveSubscriptions() {
    return getSubscriptions().size();
  }

  public long getNumMarketDataUpdatesReceived() {
    return _numMarketDataUpdatesReceived.get();
  }
  
  public double getNumLiveDataUpdatesSentPerSecondOverLastMinute() {
    return _performanceCounter.getHitsPerSecond();
  }

  public Set<Subscription> getSubscriptions() {
    _subscriptionLock.lock();
    try {
      return new HashSet<Subscription>(_currentlyActiveSubscriptions);
    } finally {
      _subscriptionLock.unlock();
    }
  }

  public Subscription getSubscription(LiveDataSpecification fullyQualifiedSpec) {
    MarketDataDistributor distributor = getMarketDataDistributor(fullyQualifiedSpec);
    if (distributor == null) {
      return null;
    }
    return distributor.getSubscription();
  }

  public Subscription getSubscription(String securityUniqueId) {
    _subscriptionLock.lock();
    try {
      return _securityUniqueId2Subscription.get(securityUniqueId);
    } finally {
      _subscriptionLock.unlock();
    }
  }
  
  public MarketDataDistributor getMarketDataDistributor(DistributionSpecification distributionSpec) {
    Subscription subscription = getSubscription(distributionSpec.getFullyQualifiedLiveDataSpecification());
    if (subscription == null) {
      return null;
    }
    return subscription.getMarketDataDistributor(distributionSpec);
  }
  
  public MarketDataDistributor getMarketDataDistributor(LiveDataSpecification fullyQualifiedSpec) {
    _subscriptionLock.lock();
    try {
      return _fullyQualifiedSpec2Distributor.get(fullyQualifiedSpec);
    } finally {
      _subscriptionLock.unlock();
    }
  }
  
  /**
   * This method is mainly useful in tests.
   * 
   * @param securityUniqueId Security unique ID
   * @return The only market data distributor associated with the 
   * security unique ID. 
   * @throws OpenGammaRuntimeException If there is no distributor
   * associated with the given {@code securityUniqueId}, or
   * if there is more than 1 such distributor.  
   */
  public MarketDataDistributor getMarketDataDistributor(String securityUniqueId) {
    Subscription sub = getSubscription(securityUniqueId);
    if (sub == null) {
      throw new OpenGammaRuntimeException("Subscription " + securityUniqueId + " not found");
    }
    Collection<MarketDataDistributor> distributors = sub.getDistributors();
    if (distributors.size() != 1) {
      throw new OpenGammaRuntimeException(distributors.size() + " distributors found for subscription " + securityUniqueId);
    }
    return distributors.iterator().next();
  }
  
}
