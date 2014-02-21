/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.wire.types.FudgeWireType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.MBeanExporter;

import com.google.common.base.Supplier;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A {@link MarketDataProvider} for live data backed by an {@link InMemoryLKVMarketDataProvider}.
 */
public class InMemoryLKVLiveMarketDataProvider extends AbstractMarketDataProvider implements LiveMarketDataProvider, LiveDataListener, SubscriptionReporter {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVLiveMarketDataProvider.class);
  private static final AtomicInteger s_nextObjectName = new AtomicInteger();

  // Injected Inputs:
  private final LiveDataClient _liveDataClient;
  private final MarketDataAvailabilityProvider _availabilityProvider;

  // Runtime State:
  private final InMemoryLKVMarketDataProvider _underlyingProvider;
  private final MarketDataPermissionProvider _permissionProvider;
  
  private final Multimap<LiveDataSpecification, ValueSpecification> _pendingSubscriptionsByRequestedSpec = createReferenceCountingMultimap();
  private final Multimap<LiveDataSpecification, ValueSpecification> _activeSubscriptionsByQualifiedSpec = createReferenceCountingMultimap();
  private final Map<LiveDataSpecification, LiveDataSpecification> _requestedSpecToFullyQualifiedSpec = new HashMap<>();
  
  private final UserPrincipal _marketDataUser;
  
  private final ReentrantReadWriteLock _subscriptionLock = new ReentrantReadWriteLock();
  private final WriteLock _subscriptionWriteLock = _subscriptionLock.writeLock();
  private final ReadLock _subscriptionReadLock = _subscriptionLock.readLock();

  public InMemoryLKVLiveMarketDataProvider(final LiveDataClient liveDataClient, final MarketDataAvailabilityFilter availabilityFilter, final UserPrincipal marketDataUser) {
    this(liveDataClient, availabilityFilter, new LiveMarketDataPermissionProvider(liveDataClient), marketDataUser);
  }

  public InMemoryLKVLiveMarketDataProvider(final LiveDataClient liveDataClient, final MarketDataAvailabilityFilter availabilityFilter, final MarketDataPermissionProvider permissionProvider,
      final UserPrincipal marketDataUser) {
    ArgumentChecker.notNull(liveDataClient, "liveDataClient");
    ArgumentChecker.notNull(availabilityFilter, "availabilityFilter");
    ArgumentChecker.notNull(permissionProvider, "permissionProvider");
    ArgumentChecker.notNull(marketDataUser, "marketDataUser");
    _liveDataClient = liveDataClient;
    // TODO: Should we use the default normalization rules from the live data client rather than hard code the standard rule set here?
    _availabilityProvider = availabilityFilter.withProvider(new LiveMarketDataAvailabilityProvider(StandardRules.getOpenGammaRuleSetId()));
    _underlyingProvider = new InMemoryLKVMarketDataProvider();
    _permissionProvider = permissionProvider;
    _marketDataUser = marketDataUser;

    try {
      MBeanServer jmxServer = ManagementFactory.getPlatformMBeanServer();
      ObjectName objectName = createObjectName();
      if (objectName != null) {
        MBeanExporter exporter = new MBeanExporter();
        exporter.setServer(jmxServer);
        exporter.registerManagedResource(this, objectName);
      }
    } catch (SecurityException e) {
      s_logger.warn("No permissions for platform MBean server - JMX will not be available", e);
    }
  }

  @Override
  public String getMarketDataUser() {
    return _marketDataUser.toString();
  }

  @Override
  public int getRequestedLiveDataSubscriptionCount() {
    _subscriptionReadLock.lock();
    try {
      return Sets.union(_requestedSpecToFullyQualifiedSpec.keySet(), _pendingSubscriptionsByRequestedSpec.keySet()).size();
    } finally {
      _subscriptionReadLock.unlock();
    }
  }

  @Override
  public int getActiveValueSpecificationSubscriptionCount() {
    _subscriptionReadLock.lock();
    try {
      return _activeSubscriptionsByQualifiedSpec.size();
    } finally {
      _subscriptionReadLock.unlock();
    }
  }

  @Override
  public Map<String, SubscriptionInfo> queryByTicker(String ticker) {
    Map<String, SubscriptionInfo> results = new HashMap<>();
    _subscriptionReadLock.lock();
    try {
      for (LiveDataSpecification requestedLiveDataSpec : _pendingSubscriptionsByRequestedSpec.keySet()) {
        String requestedLiveDataSpecString = requestedLiveDataSpec.toString();
        if (requestedLiveDataSpecString.contains(ticker)) {
          Collection<ValueSpecification> pendingSubscribers = _pendingSubscriptionsByRequestedSpec.get(requestedLiveDataSpec);
          results.put(requestedLiveDataSpecString, new SubscriptionInfo(pendingSubscribers.size(), "PENDING", null));
        }
      }
      for (LiveDataSpecification requestedLiveDataSpec : _requestedSpecToFullyQualifiedSpec.keySet()) {
        String requestedLiveDataSpecString = requestedLiveDataSpec.toString();
        if (requestedLiveDataSpecString.contains(ticker)) {
          LiveDataSpecification fullyQualifiedLiveDataSpec = _requestedSpecToFullyQualifiedSpec.get(requestedLiveDataSpec);
          Collection<ValueSpecification> activeSubscribers = _activeSubscriptionsByQualifiedSpec.get(fullyQualifiedLiveDataSpec);
          if (_activeSubscriptionsByQualifiedSpec.isEmpty()) {
            // No longer any subscribers
            continue;
          }
          Object currentValue = _underlyingProvider.getCurrentValue(Iterables.getFirst(activeSubscribers, null));
          results.put(requestedLiveDataSpecString, new SubscriptionInfo(activeSubscribers.size(), "ACTIVE", currentValue));
        }
      }
    } finally {
      _subscriptionReadLock.unlock();
    }
    return results;
  }
  
  /*package*/ InMemoryLKVMarketDataProvider getUnderlyingProvider() {
    return _underlyingProvider;
  }
  
  private <K, V> Multimap<K, V> createReferenceCountingMultimap() {
    return Multimaps.newMultimap(new HashMap<K, Collection<V>>(), new Supplier<Multiset<V>>() {

      @Override
      public Multiset<V> get() {
        return HashMultiset.create();
      }
      
    });
  }

  private ObjectName createObjectName() {
    try {
      return new ObjectName("com.opengamma:type=InMemoryLKVLiveMarketDataProvider,name=InMemoryLKVLiveMarketDataProvider " + s_nextObjectName.getAndIncrement());
    } catch (MalformedObjectNameException e) {
      s_logger.warn("Invalid object name - unable to setup JMX bean", e);
      return null;
    }
  }

  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    subscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    Collection<LiveDataSpecification> toSubscribe = new HashSet<>(valueSpecifications.size());
    _subscriptionWriteLock.lock();
    try {
      for (ValueSpecification valueSpecification : valueSpecifications) {
        LiveDataSpecification requestLiveDataSpec = LiveMarketDataAvailabilityProvider.getLiveDataSpecification(valueSpecification);
        LiveDataSpecification fullyQualifiedSpec = _requestedSpecToFullyQualifiedSpec.get(requestLiveDataSpec);
        if (fullyQualifiedSpec == null || !_activeSubscriptionsByQualifiedSpec.containsKey(fullyQualifiedSpec)) {
          if (!_pendingSubscriptionsByRequestedSpec.containsKey(requestLiveDataSpec)) {
            toSubscribe.add(requestLiveDataSpec);
          }
          _pendingSubscriptionsByRequestedSpec.put(requestLiveDataSpec, valueSpecification);
        } else {
          _activeSubscriptionsByQualifiedSpec.put(fullyQualifiedSpec, valueSpecification);
          toSubscribe.add(requestLiveDataSpec);
        }
      }
      // Downgrade to read lock, allowing value updates but preventing further subscribes/unsubscribes until we have completely finished subscribing.
      _subscriptionReadLock.lock();
    } finally {
      _subscriptionWriteLock.unlock();
    }
    try {
      if (!toSubscribe.isEmpty()) {
        s_logger.info("Subscribing {} to {} live data specifications", _marketDataUser, toSubscribe.size());
        _liveDataClient.subscribe(_marketDataUser, toSubscribe, this);
      }
    } finally {
      _subscriptionReadLock.unlock();
    }
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    unsubscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    final Set<LiveDataSpecification> toFullyUnsubscribe = Sets.newHashSetWithExpectedSize(valueSpecifications.size());
    _subscriptionWriteLock.lock();
    try {
      for (final ValueSpecification valueSpecification : valueSpecifications) {
        LiveDataSpecification requestLiveDataSpec = LiveMarketDataAvailabilityProvider.getLiveDataSpecification(valueSpecification);
        if (_pendingSubscriptionsByRequestedSpec.containsKey(requestLiveDataSpec)) {
          _pendingSubscriptionsByRequestedSpec.remove(requestLiveDataSpec, valueSpecification);
        } else {
          LiveDataSpecification fullyQualifiedSpec = _requestedSpecToFullyQualifiedSpec.get(requestLiveDataSpec);
          if (fullyQualifiedSpec != null && _activeSubscriptionsByQualifiedSpec.containsKey(fullyQualifiedSpec)) {
            _activeSubscriptionsByQualifiedSpec.remove(fullyQualifiedSpec, valueSpecification);
            s_logger.debug("Unsubscribed from " + valueSpecification);
            if (!_activeSubscriptionsByQualifiedSpec.get(fullyQualifiedSpec).contains(valueSpecification)) {
              // Remove the value from the underlying LKV to prevent the return of
              // stale data which can happen if subscription reference counting goes awry
              _underlyingProvider.removeValue(valueSpecification);
            }
            if (!_activeSubscriptionsByQualifiedSpec.containsKey(fullyQualifiedSpec)) {
              // Last subscription removed
              s_logger.debug("Now fully unsubscribed from " + valueSpecification);              
              toFullyUnsubscribe.add(fullyQualifiedSpec);
            }
          } else {
            s_logger.warn("Received unsubscription request for " + valueSpecification + " with no existing subscription, which indicates that something is maintaining reference counts incorrectly.");
          }
        }
      }
      // Downgrade to read lock, allowing value updates but preventing further subscribes/unsubscribes until we have completely finished unsubscribing.
      _subscriptionReadLock.lock();
    } finally {
      _subscriptionWriteLock.unlock();
    }
    try {
      if (!toFullyUnsubscribe.isEmpty()) {
        s_logger.info("Unsubscribing {} from {} live data specifications", _marketDataUser, toFullyUnsubscribe.size());
        _liveDataClient.unsubscribe(_marketDataUser, toFullyUnsubscribe, this);
      }
    } finally {
      _subscriptionReadLock.unlock();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
    return _availabilityProvider;
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionProvider;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    // We don't look at the live data provider field at the moment
    return marketDataSpec instanceof LiveMarketDataSpecification;
  }

  @Override
  public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    return new LiveMarketDataSnapshot(_underlyingProvider.snapshot(marketDataSpec), this);
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscriptionResultReceived(final LiveDataSubscriptionResponse subscriptionResult) {
    subscriptionResultsReceived(Collections.singleton(subscriptionResult));
  }

  @Override
  public void subscriptionResultsReceived(final Collection<LiveDataSubscriptionResponse> subscriptionResults) {
    Set<ValueSpecification> successfulSubscriptions = new HashSet<>();
    Set<ValueSpecification> failedSubscriptions = new HashSet<>();
    Set<LiveDataSpecification> toFullyUnsubscribe = new HashSet<>();
    
    _subscriptionWriteLock.lock();
    try {
      for (LiveDataSubscriptionResponse subscriptionResult : subscriptionResults) {
        s_logger.debug("Processing subscription result " + subscriptionResult);
        LiveDataSpecification requestedSpec = subscriptionResult.getRequestedSpecification();
        LiveDataSpecification fullyQualifiedSpec = subscriptionResult.getFullyQualifiedSpecification();
        Collection<ValueSpecification> subscribers = _pendingSubscriptionsByRequestedSpec.removeAll(requestedSpec);
        if (subscribers.isEmpty()) {
          s_logger.debug("Received subscription result for requested spec {} but there are no pending subscriptions. " +
              "Either these were unsubscribed in the meantime or this is a duplicate subscription result.", requestedSpec);
          if (!_activeSubscriptionsByQualifiedSpec.containsKey(fullyQualifiedSpec)) {
            // All pending subscriptions have been unsubscribed from whilst waiting. Additionally, there are no existing
            // active subscriptions for the same fully qualified specification, so the subscription is no longer required.
            toFullyUnsubscribe.add(fullyQualifiedSpec);
          }
        } else {
          _requestedSpecToFullyQualifiedSpec.put(requestedSpec, fullyQualifiedSpec);
          _activeSubscriptionsByQualifiedSpec.putAll(fullyQualifiedSpec, subscribers);
          Collection<ValueSpecification> allSubscribers = _activeSubscriptionsByQualifiedSpec.get(fullyQualifiedSpec);
          if (subscriptionResult.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
            successfulSubscriptions.addAll(allSubscribers);
            s_logger.debug("Subscription made to {} resulted in fully qualified {}", subscriptionResult.getRequestedSpecification(), subscriptionResult.getFullyQualifiedSpecification());
          } else {
            failedSubscriptions.addAll(allSubscribers);
            if (subscriptionResult.getSubscriptionResult() == LiveDataSubscriptionResult.NOT_AUTHORIZED) {
              s_logger.warn("Subscription to {} failed because user is not authorised: {}", subscriptionResult.getRequestedSpecification(), subscriptionResult);
            } else {
              s_logger.debug("Subscription to {} failed: {}", subscriptionResult.getRequestedSpecification(), subscriptionResult);
            }
          }
        }
      }
      // Downgrade to read lock, allowing value updates but preventing further subscribes/unsubscribes until we have completely finished unsubscribing.
      _subscriptionReadLock.lock();
    } finally {
      _subscriptionWriteLock.unlock();
    }
    try {
      if (!toFullyUnsubscribe.isEmpty()) {
        s_logger.info("Unsubscribing {} from {} live data specifications which were pending but have since been unsubscribed", _marketDataUser, toFullyUnsubscribe.size());
        _liveDataClient.unsubscribe(_marketDataUser, toFullyUnsubscribe, this);
      }
    } finally {
      _subscriptionReadLock.unlock();
    }
    
    s_logger.info("Subscription results - {} success, {} failures", successfulSubscriptions.size(), failedSubscriptions.size());
    if (!failedSubscriptions.isEmpty()) {
      valuesChanged(failedSubscriptions); // PLAT-1429: wake up the init call
      subscriptionFailed(failedSubscriptions, "TODO: get/concat message(s) from " + failedSubscriptions.size() + " failures"/*subscriptionResult.getUserMessage()*/);
    }
    if (!successfulSubscriptions.isEmpty()) {
      subscriptionsSucceeded(successfulSubscriptions);
    }
  }

  @Override
  public boolean isActive(final ValueSpecification specification) {
    LiveDataSpecification requestedSpec = LiveMarketDataAvailabilityProvider.getLiveDataSpecification(specification);
    LiveDataSpecification fullyQualifiedSpec = _requestedSpecToFullyQualifiedSpec.get(requestedSpec);
    return fullyQualifiedSpec != null && _activeSubscriptionsByQualifiedSpec.get(fullyQualifiedSpec).contains(specification);
  }

  @Override
  public void subscriptionStopped(final LiveDataSpecification fullyQualifiedSpecification) {
    // Ignore. We've already done our housekeeping before calling out to the liveData client.
  }

  @Override
  public void valueUpdate(final LiveDataValueUpdate valueUpdate) {
    s_logger.debug("Update received {}", valueUpdate);
    LiveDataSpecification fullyQualifiedSpec = valueUpdate.getSpecification();
    Collection<ValueSpecification> subscribers;
    _subscriptionReadLock.lock();
    try {
      subscribers = ImmutableSet.copyOf(_activeSubscriptionsByQualifiedSpec.get(fullyQualifiedSpec));
    } finally {
      _subscriptionReadLock.unlock();
    }
    if (subscribers.isEmpty()) {
      s_logger.warn("Received value update for which no active subscriptions were found: {}", fullyQualifiedSpec);
      return;        
    }
    s_logger.debug("Subscribed values are {}", subscribers);
    final FudgeMsg msg = valueUpdate.getFields();
    for (final ValueSpecification subscription : subscribers) {
      String valueName = subscription.getValueName();
      Object value;
      if (MarketDataRequirementNames.ALL.equals(valueName)) {
        Object previousValue = _underlyingProvider.getCurrentValue(subscription);
        if (previousValue == null) {
          value = msg;
        } else if (!(previousValue instanceof FudgeMsg)) {
          s_logger.error("Found unexpected previous market value " + previousValue + " of type " + previousValue.getClass() + " for specification " + subscription);
          value = msg;
        } else {
          FudgeMsg currentValueMsg = (FudgeMsg) previousValue;
          MutableFudgeMsg unionMsg = OpenGammaFudgeContext.getInstance().newMessage(msg);
          Set<String> missingFields = currentValueMsg.getAllFieldNames();
          missingFields.removeAll(msg.getAllFieldNames());
          for (String missingField : missingFields) {
            unionMsg.add(currentValueMsg.getByName(missingField));
          }
          value = unionMsg;
        }
      } else {
        final FudgeField field = msg.getByName(valueName);
        if (field == null) {
          s_logger.debug("No market data value for {} on target {}", valueName, subscription.getTargetSpecification());
          continue;
        } else {
          switch (field.getType().getTypeId()) {
            case FudgeWireType.BYTE_TYPE_ID:
            case FudgeWireType.SHORT_TYPE_ID:
            case FudgeWireType.INT_TYPE_ID:
            case FudgeWireType.LONG_TYPE_ID:
            case FudgeWireType.FLOAT_TYPE_ID:
              // All numeric data is presented as a double downstream - convert
              value = ((Number) field.getValue()).doubleValue();
              break;
            case FudgeWireType.DOUBLE_TYPE_ID:
              // Already a double
              value = field.getValue();
              break;
            case FudgeWireType.DATE_TYPE_ID:
              value = ((FudgeDate) field.getValue()).toLocalDate();
              break;
            case FudgeWireType.DATETIME_TYPE_ID:
              value = ((FudgeDateTime) field.getValue()).toLocalDateTime();
              break;
            default:
              s_logger.warn("Unexpected market data type {}", field);
              continue;
          }
        }
      }
      _underlyingProvider.addValue(subscription, value);
    }
    valuesChanged(subscribers);
  }

  /**
   * Reattempts subscriptions for any data identified by the specified schemes. If a data provider becomes available this method will be invoked with the schemes handled by the provider. This gives
   * this class the opportunity to reattempt previously failed subscriptions.
   * 
   * @param schemes The schemes for which market data subscriptions should be reattempted.
   */
  /* package */void resubscribe(Set<ExternalScheme> schemes) {
    _subscriptionReadLock.lock();
    try {
      Collection<LiveDataSpecification> toSubscribe = new HashSet<>();
      // Include pending subscriptions too to be safe
      for (LiveDataSpecification requestedSpec : _pendingSubscriptionsByRequestedSpec.keySet()) {
        for (ExternalId id : requestedSpec.getIdentifiers()) {
          if (schemes.contains(id.getScheme())) {
            toSubscribe.add(requestedSpec);
          }
        }
      }
      for (LiveDataSpecification fullyQualifiedSpec : _activeSubscriptionsByQualifiedSpec.keySet()) {
        for (ExternalId id : fullyQualifiedSpec.getIdentifiers()) {
          if (schemes.contains(id.getScheme())) {
            toSubscribe.add(fullyQualifiedSpec);
          }
        }
      }
      if (!toSubscribe.isEmpty()) {
        s_logger.info("Subscribing {} to {} live data specifications", _marketDataUser, toSubscribe.size());
        _liveDataClient.subscribe(_marketDataUser, toSubscribe, this);
      }
    } finally {
      _subscriptionReadLock.unlock();
    }
  }
}
