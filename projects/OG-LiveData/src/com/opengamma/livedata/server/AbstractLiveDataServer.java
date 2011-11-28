/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
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
import com.opengamma.util.PublicAPI;

/**
 * The base class from which most OpenGamma Live Data feed servers should
 * extend. Handles most common cases for distributed contract management.
 * 
 */
@PublicAPI
public abstract class AbstractLiveDataServer implements Lifecycle {
  private static final Logger s_logger = LoggerFactory
      .getLogger(AbstractLiveDataServer.class);
  
  private volatile MarketDataSenderFactory _marketDataSenderFactory = new EmptyMarketDataSenderFactory();
  private final Collection<SubscriptionListener> _subscriptionListeners = new CopyOnWriteArrayList<SubscriptionListener>();
  
  /** Access controlled via _subscriptionLock */
  private final Set<Subscription> _currentlyActiveSubscriptions = new HashSet<Subscription>();
  
  /** _Write_ access controlled via _subscriptionLock */
  private final Map<String, Subscription> _securityUniqueId2Subscription = new ConcurrentHashMap<String, Subscription>();
  
  /** Access controlled via _subscriptionLock */
  private final Map<LiveDataSpecification, MarketDataDistributor> _fullyQualifiedSpec2Distributor = new HashMap<LiveDataSpecification, MarketDataDistributor>();

  private final AtomicLong _numMarketDataUpdatesReceived = new AtomicLong(0);
  private final PerformanceCounter _performanceCounter;

  private final Lock _subscriptionLock = new ReentrantLock();

  private DistributionSpecificationResolver _distributionSpecificationResolver = new NaiveDistributionSpecificationResolver();
  private LiveDataEntitlementChecker _entitlementChecker = new PermissiveLiveDataEntitlementChecker();
  
  private volatile ConnectionStatus _connectionStatus = ConnectionStatus.NOT_CONNECTED;

  
  protected AbstractLiveDataServer() {
    this(true);
  }

  /**
   * You may wish to disable performance counting if you expect a high rate of messages, or to process messages on several threads.
   * @param isPerformanceCountingEnabled Whether to track the message rate here. See getNumLiveDataUpdatesSentPerSecondOverLastMinute
   */
  protected AbstractLiveDataServer(boolean isPerformanceCountingEnabled) {
    _performanceCounter = isPerformanceCountingEnabled ? new PerformanceCounter(60) : null;
  }
  
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
  protected abstract Map<String, FudgeMsg> doSnapshot(Collection<String> uniqueIds);

  /**
   * @return Identification domain that uniquely identifies securities for this
   *         type of server.
   */
  protected abstract ExternalScheme getUniqueIdDomain();
  
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
   * Is the server connected to underlying market data API?
   */
  public enum ConnectionStatus {
    /** Connection active */
    CONNECTED,
    /** Connection not active */
    NOT_CONNECTED
  }
  
  public ConnectionStatus getConnectionStatus() {
    return _connectionStatus;
  }
  
  public void setConnectionStatus(ConnectionStatus connectionStatus) {
    _connectionStatus = connectionStatus;
    s_logger.info("Connection status changed to " + connectionStatus);
    
    if (connectionStatus == ConnectionStatus.NOT_CONNECTED) {
      for (Subscription subscription : getSubscriptions()) {
        subscription.setHandle(null);        
      }
    }
  }
  
  void reestablishSubscriptions() {
    _subscriptionLock.lock();
    try {
      Set<String> securities = _securityUniqueId2Subscription.keySet();
      try {
        Map<String, Object> subscriptions = doSubscribe(securities);
        for (Entry<String, Object> entry : subscriptions.entrySet()) {
          Subscription subscription = _securityUniqueId2Subscription.get(entry.getKey());
          subscription.setHandle(entry.getValue());
        }
      } catch (RuntimeException e) {
        s_logger.error("Could not reestablish subscription to {}", new Object[] {securities}, e);
      }
    } finally {
      _subscriptionLock.unlock();
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
        ExternalId.of(getUniqueIdDomain(), securityUniqueId));
    return liveDataSpecification;
  }

  /**
   * Subscribes to the market data and creates a default distributor.
   *
   * @param securityUniqueId Security unique ID
   * @return Whether the subscription succeeded or failed
   * @see #getDefaultNormalizationRuleSetId()
   */
  public LiveDataSubscriptionResponse subscribe(String securityUniqueId) {
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
  public LiveDataSubscriptionResponse subscribe(String securityUniqueId, boolean persistent) {
    LiveDataSpecification liveDataSpecification = getLiveDataSpecification(securityUniqueId);
    return subscribe(liveDataSpecification, persistent);
  }
  
  public LiveDataSubscriptionResponse subscribe(LiveDataSpecification liveDataSpecificationFromClient,
      boolean persistent) {
    
    Collection<LiveDataSubscriptionResponse> results = subscribe(
        Collections.singleton(liveDataSpecificationFromClient), 
        persistent);

    if (results == null || results.size() != 1) {
      return getErrorResponse(
          liveDataSpecificationFromClient,
          LiveDataSubscriptionResult.INTERNAL_ERROR,
          "subscribe() did not fulfill its contract to populate map for each live data spec");
    }
    LiveDataSubscriptionResponse result = results.iterator().next();
    if (!liveDataSpecificationFromClient.equals(result.getRequestedSpecification())) {
      return getErrorResponse(
          liveDataSpecificationFromClient,
          LiveDataSubscriptionResult.INTERNAL_ERROR,
          "Expected a subscription result for " + liveDataSpecificationFromClient + " but received one for " + result.getRequestedSpecification());
    }
    
    return result;
  }
  
  public Collection<LiveDataSubscriptionResponse> subscribe(
      Collection<LiveDataSpecification> liveDataSpecificationsFromClient, boolean persistent) {
    ArgumentChecker.notNull(liveDataSpecificationsFromClient, "Subscriptions to be created");
    
    s_logger.info("Subscribe requested for {}, persistent = {}", liveDataSpecificationsFromClient, persistent);
    
    verifyConnectionOk();
    
    Collection<LiveDataSubscriptionResponse> responses = new ArrayList<LiveDataSubscriptionResponse>();
    Map<String, Subscription> securityUniqueId2NewSubscription = new HashMap<String, Subscription>();
    Map<String, LiveDataSpecification> securityUniqueId2SpecFromClient = new HashMap<String, LiveDataSpecification>();
    
    _subscriptionLock.lock();
    try {
    
      Map<LiveDataSpecification, DistributionSpecification> distrSpecs = getDistributionSpecificationResolver().resolve(liveDataSpecificationsFromClient);
      for (LiveDataSpecification specFromClient : liveDataSpecificationsFromClient) {
        
        // this is the only place where subscribe() can 'partially' fail
        DistributionSpecification distributionSpec = distrSpecs.get(specFromClient);
        
        if (distributionSpec == null) {
          s_logger.info("Unable to work out distribution spec for specification " + specFromClient);
          responses.add(getErrorResponse(specFromClient, LiveDataSubscriptionResult.NOT_PRESENT, "Unable to work out distribution spec"));
          continue;
        }
        
        LiveDataSpecification fullyQualifiedSpec = distributionSpec.getFullyQualifiedLiveDataSpecification();
      
        Subscription subscription = getSubscription(fullyQualifiedSpec);
        if (subscription != null) {
          s_logger.info("Already subscribed to {}", fullyQualifiedSpec);
          
          subscription.createDistributor(distributionSpec, persistent);
    
          responses.add(getSubscriptionResponse(specFromClient, distributionSpec));                    
    
        } else {
    
          String securityUniqueId = fullyQualifiedSpec.getIdentifier(getUniqueIdDomain());
          if (securityUniqueId == null) {
            responses.add(getErrorResponse(specFromClient, LiveDataSubscriptionResult.INTERNAL_ERROR,
                "Qualified spec " + fullyQualifiedSpec + " does not contain ID of domain " + getUniqueIdDomain()));
            continue;
          }
          
          subscription = new Subscription(securityUniqueId, getMarketDataSenderFactory());
          subscription.createDistributor(distributionSpec, persistent);
          securityUniqueId2NewSubscription.put(subscription.getSecurityUniqueId(), subscription);
          securityUniqueId2SpecFromClient.put(subscription.getSecurityUniqueId(), specFromClient);
        }
      }
      
      //Allow checks here, before we do the snapshot or the subscribe
      checkSubscribe(securityUniqueId2NewSubscription.keySet());
      
      // In some cases, the underlying market data API may not, when the subscription is started,
      // return a full image of all fields. If so, we need to get the full image explicitly.
      Collection<String> newSubscriptionsForWhichSnapshotIsRequired = new ArrayList<String>();
      for (Subscription subscription : securityUniqueId2NewSubscription.values()) {
        if (snapshotOnSubscriptionStartRequired(subscription)) {
          newSubscriptionsForWhichSnapshotIsRequired.add(subscription.getSecurityUniqueId());
        }
      }
      
      s_logger.info("Subscription snapshot required for {}", newSubscriptionsForWhichSnapshotIsRequired);
      Map<String, FudgeMsg> snapshots = doSnapshot(newSubscriptionsForWhichSnapshotIsRequired);
      for (Map.Entry<String, FudgeMsg> snapshot : snapshots.entrySet()) {
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
          responses.add(getErrorResponse(specFromClient, LiveDataSubscriptionResult.INTERNAL_ERROR,
              "The subscription should only have 1 distribution specification at the moment: " + subscription));
          continue;
        }
        
        for (MarketDataDistributor distributor : subscription.getDistributors()) {
          _fullyQualifiedSpec2Distributor.put(distributor.getFullyQualifiedLiveDataSpecification(),
              distributor);
          responses.add(getSubscriptionResponse(specFromClient, distributor.getDistributionSpec()));
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

    return responses;
  }
  
  /**
   * Check that a subscription request is valid.
   * Will be called before any snapshot or subscribe requests for the keys
   * @param uniqueIds The unique ids for which a subscribe is being requested  
   */
  protected void checkSubscribe(Set<String> uniqueIds) {
    //Do nothing by default
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
  public Collection<LiveDataSubscriptionResponse> snapshot(Collection<LiveDataSpecification> liveDataSpecificationsFromClient) {
    ArgumentChecker.notNull(liveDataSpecificationsFromClient, "Snapshots to be obtained");
    
    s_logger.info("Snapshot requested for {}", liveDataSpecificationsFromClient);
    
    verifyConnectionOk();
    
    Collection<LiveDataSubscriptionResponse> responses = new ArrayList<LiveDataSubscriptionResponse>();
    
    Collection<String> snapshotsToActuallyDo = new ArrayList<String>();
    Map<String, LiveDataSpecification> securityUniqueId2LiveDataSpecificationFromClient = new HashMap<String, LiveDataSpecification>(); 
    
    for (LiveDataSpecification liveDataSpecificationFromClient : liveDataSpecificationsFromClient) {
      DistributionSpecification distributionSpec = getDistributionSpecificationResolver()
        .resolve(liveDataSpecificationFromClient);
      LiveDataSpecification fullyQualifiedSpec = distributionSpec.getFullyQualifiedLiveDataSpecification();
      
      MarketDataDistributor currentlyActiveDistributor = getMarketDataDistributor(distributionSpec);
      if (currentlyActiveDistributor != null) {
        if (currentlyActiveDistributor.getSnapshot() != null) {
          //NOTE simon 28/11/2011: We presume that all the fields were provided in one go, all or nothing.
          s_logger.info("Able to satisfy {} from existing LKV", liveDataSpecificationFromClient);
          LiveDataValueUpdateBean snapshot = currentlyActiveDistributor.getSnapshot();
          responses.add(getSnapshotResponse(liveDataSpecificationFromClient, snapshot));
          continue;
        } else if (snapshotOnSubscriptionStartRequired(currentlyActiveDistributor.getSubscription())) {
          //BBG-91 - don't requery when an existing subscription indicates that the snapshot will fail
          //NOTE simon 28/11/2011: Only in the case of requiring a snapshot is it safe to use an empty snapshot from a subscription, since in the other case we may still be waiting for values  
          s_logger.info("Able to satisfy failed snapshot {} from existing LKV", liveDataSpecificationFromClient);
          responses.add(getErrorResponse(liveDataSpecificationFromClient, LiveDataSubscriptionResult.INTERNAL_ERROR,
              "Existing subscription for " + currentlyActiveDistributor.getDistributionSpec().getMarketDataId()
                  + " failed to retrieve a snapshot.  Perhaps requeried fields are unavailable."));
          continue;
        } else {
          s_logger.info("Can't use existing subscription to satisfy {} from existing LKV", liveDataSpecificationFromClient);
        }
      }
      
      String securityUniqueId = fullyQualifiedSpec.getIdentifier(getUniqueIdDomain());
      if (securityUniqueId == null) {
        responses.add(getErrorResponse(
            liveDataSpecificationFromClient,
            LiveDataSubscriptionResult.INTERNAL_ERROR,
            "Qualified spec " + fullyQualifiedSpec + " does not contain ID of domain " + getUniqueIdDomain()));
        continue;
      }
      
      snapshotsToActuallyDo.add(securityUniqueId);
      securityUniqueId2LiveDataSpecificationFromClient.put(securityUniqueId, liveDataSpecificationFromClient);      
    }

    s_logger.info("Need to actually snapshot {}", snapshotsToActuallyDo);
    Map<String, FudgeMsg> snapshots = doSnapshot(snapshotsToActuallyDo);
    for (Map.Entry<String, FudgeMsg> snapshotEntry : snapshots.entrySet()) {
      String securityUniqueId = snapshotEntry.getKey();
      FudgeMsg msg = snapshotEntry.getValue();
      
      LiveDataSpecification liveDataSpecFromClient = securityUniqueId2LiveDataSpecificationFromClient.get(securityUniqueId);
      
      DistributionSpecification distributionSpec = getDistributionSpecificationResolver()
        .resolve(liveDataSpecFromClient);
      FudgeMsg normalizedMsg = distributionSpec.getNormalizedMessage(msg, securityUniqueId);
      if (normalizedMsg == null) {
        responses.add(getErrorResponse(
            liveDataSpecFromClient,
            LiveDataSubscriptionResult.INTERNAL_ERROR,
            "When snapshot for " + securityUniqueId + " was run through normalization, the message disappeared. This" + 
            " indicates there are buggy normalization rules in place, or that buggy (or unexpected) data was" +
            " received from the underlying market data API. Check your normalization rules. Raw, unnormalized msg = "
            + msg));
        continue;
      }
      
      LiveDataValueUpdateBean snapshot = new LiveDataValueUpdateBean(0, distributionSpec.getFullyQualifiedLiveDataSpecification(), normalizedMsg);
      responses.add(getSnapshotResponse(liveDataSpecFromClient, snapshot));
    }
    
    return responses; 
  }
  
  /**
   * If you want to force a snapshot - i.e., always a request a snapshot from the underlying API -
   * you can use this method.
   * 
   * @param securityUniqueId Security unique ID
   * @return The snapshot
   */
  public FudgeMsg doSnapshot(String securityUniqueId) {
    Map<String, FudgeMsg> snapshots = doSnapshot(Collections.singleton(securityUniqueId));
    FudgeMsg snapshot = snapshots.get(securityUniqueId);
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
    
    try {
      
      return subscriptionRequestMadeImpl(subscriptionRequest);
      
    } catch (Exception e) {
      
      s_logger.error("Failed to subscribe to " + subscriptionRequest, e);
      
      ArrayList<LiveDataSubscriptionResponse> responses = new ArrayList<LiveDataSubscriptionResponse>();
      for (LiveDataSpecification requestedSpecification :  subscriptionRequest.getSpecifications()) {
        responses.add(getErrorResponse(
            requestedSpecification, 
            LiveDataSubscriptionResult.INTERNAL_ERROR,
            e.getMessage()));
      }
      return new LiveDataSubscriptionResponseMsg(subscriptionRequest
          .getUser(), responses);
    }
  }
  
  protected LiveDataSubscriptionResponseMsg subscriptionRequestMadeImpl(
      LiveDataSubscriptionRequest subscriptionRequest) {
    
    boolean persistent = subscriptionRequest.getType().equals(SubscriptionType.PERSISTENT);

    ArrayList<LiveDataSubscriptionResponse> responses = new ArrayList<LiveDataSubscriptionResponse>();
    
    ArrayList<LiveDataSpecification> snapshots = new ArrayList<LiveDataSpecification>();
    ArrayList<LiveDataSpecification> subscriptions = new ArrayList<LiveDataSpecification>();
    
    Map<LiveDataSpecification, DistributionSpecification> distributionSpecifications = getDistributionSpecificationResolver().resolve(subscriptionRequest.getSpecifications());
    ArrayList<LiveDataSpecification> distributable = new ArrayList<LiveDataSpecification>();
    for (LiveDataSpecification requestedSpecification : subscriptionRequest
        .getSpecifications()) {
      try {
        // Check that this spec can be found
        DistributionSpecification spec = distributionSpecifications.get(requestedSpecification);
        if (spec == null) {
          responses.add(new LiveDataSubscriptionResponse(requestedSpecification,
              LiveDataSubscriptionResult.NOT_PRESENT, "Could not build distribution specification for "
                  + requestedSpecification, null, null, null));
        } else {
          distributable.add(requestedSpecification);
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
    
    Map<LiveDataSpecification, Boolean> entitled = getEntitlementChecker().isEntitled(subscriptionRequest.getUser(), distributable);
    for (Entry<LiveDataSpecification, Boolean> entry : entitled.entrySet()) {
      LiveDataSpecification requestedSpecification = entry.getKey();
      try {
        Boolean entitlement = entry.getValue();
        if (!entitlement) {
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
        responses.addAll(snapshot(snapshots));
      } catch (Exception e) {
        for (LiveDataSpecification requestedSpecification : snapshots) {
          responses.add(getErrorResponse(
              requestedSpecification, 
              LiveDataSubscriptionResult.INTERNAL_ERROR,
              e.getMessage()));
        }
      }
    }
    
    if (!subscriptions.isEmpty()) {
      try {
        responses.addAll(subscribe(subscriptions, persistent));
      } catch (Exception e) {
        for (LiveDataSpecification requestedSpecification : subscriptions) {
          responses.add(getErrorResponse(
              requestedSpecification, 
              LiveDataSubscriptionResult.INTERNAL_ERROR,
              e.getMessage()));
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
    return _securityUniqueId2Subscription.containsKey(securityUniqueId);
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
      FudgeMsg liveDataFields) {
    s_logger.debug("Live data received: {}", liveDataFields);

    _numMarketDataUpdatesReceived.incrementAndGet();
    if (_performanceCounter != null) {
      _performanceCounter.hit();
    }
    
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
  
  /**
   * @return The approximate rate of live data updates received, or -1 if tracking is disabled  
   */
  public double getNumLiveDataUpdatesSentPerSecondOverLastMinute() {
    return _performanceCounter == null ? -1.0 : _performanceCounter.getHitsPerSecond();
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
    //NOTE: don't need lock here, map is safe, and this operation isn't really atomic anyway
    return _securityUniqueId2Subscription.get(securityUniqueId);
  }
  
  public MarketDataDistributor getMarketDataDistributor(DistributionSpecification distributionSpec) {
    Subscription subscription = getSubscription(distributionSpec.getFullyQualifiedLiveDataSpecification());
    if (subscription == null) {
      return null;
    }
    return subscription.getMarketDataDistributor(distributionSpec);
  }
  
  public Map<LiveDataSpecification, MarketDataDistributor> getMarketDataDistributors(Collection<LiveDataSpecification> fullyQualifiedSpecs) {
    //NOTE: this is not much (if any) faster here, but for subclasses it can be 
    _subscriptionLock.lock();
    try {
      HashMap<LiveDataSpecification, MarketDataDistributor> hashMap = new HashMap<LiveDataSpecification, MarketDataDistributor>();
      for (LiveDataSpecification liveDataSpecification : fullyQualifiedSpecs) {
        hashMap.put(liveDataSpecification, _fullyQualifiedSpec2Distributor.get(liveDataSpecification));
      }
      return hashMap;
    } finally {
      _subscriptionLock.unlock();
    }
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
  
  protected LiveDataSubscriptionResponse getErrorResponse(LiveDataSpecification liveDataSpecificationFromClient,
      LiveDataSubscriptionResult result, String message) {
    return new LiveDataSubscriptionResponse(liveDataSpecificationFromClient,
        result,
        message,
        null,
        null,
        null);
  }

  protected LiveDataSubscriptionResponse getSnapshotResponse(LiveDataSpecification liveDataSpecificationFromClient, LiveDataValueUpdateBean snapshot) {
    return new LiveDataSubscriptionResponse(
        liveDataSpecificationFromClient,
        LiveDataSubscriptionResult.SUCCESS,
        null,
        snapshot.getSpecification(),
        null,
        snapshot);
  }
  
  protected LiveDataSubscriptionResponse getSubscriptionResponse(LiveDataSpecification liveDataSpecificationFromClient, DistributionSpecification distributionSpec) {
    return new LiveDataSubscriptionResponse(
        liveDataSpecificationFromClient,
        LiveDataSubscriptionResult.SUCCESS,
        null,
        distributionSpec.getFullyQualifiedLiveDataSpecification(),
        distributionSpec.getJmsTopic(),
        null);
  }
  
}
