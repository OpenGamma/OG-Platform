/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link MarketDataProvider} for live data.
 */
public class LiveMarketDataProvider extends AbstractMarketDataProvider implements LiveDataListener {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(LiveMarketDataProvider.class);

  // Injected Inputs:
  private final LiveDataClient _liveDataClient;
  private final MarketDataAvailabilityProvider _availabilityProvider;

  // Runtime State:
  private final InMemoryLKVMarketDataProvider _underlyingProvider;
  private final MarketDataPermissionProvider _permissionProvider;
  private final ConcurrentMap<LiveDataSpecification, Set<ValueSpecification>> _liveDataSpec2Subscriptions = new ConcurrentHashMap<LiveDataSpecification, Set<ValueSpecification>>();
  private final Set<ValueSpecification> _failedSubscriptions = new CopyOnWriteArraySet<ValueSpecification>();
  private final UserPrincipal _marketDataUser;

  public LiveMarketDataProvider(final LiveDataClient liveDataClient,
      final MarketDataAvailabilityProvider availabilityProvider,
      final UserPrincipal marketDataUser) {
    this(liveDataClient,
        availabilityProvider,
        new LiveMarketDataPermissionProvider(liveDataClient),
        marketDataUser);
  }

  // [PLAT-3044] The MDAP has to do the ValueSpecification creation through LiveDataSpecificationLookup

  public LiveMarketDataProvider(final LiveDataClient liveDataClient,
      final MarketDataAvailabilityProvider availabilityProvider,
      final MarketDataPermissionProvider permissionProvider,
      final UserPrincipal marketDataUser) {
    ArgumentChecker.notNull(liveDataClient, "liveDataClient");
    ArgumentChecker.notNull(availabilityProvider, "availabilityProvider");
    ArgumentChecker.notNull(permissionProvider, "permissionProvider");
    ArgumentChecker.notNull(marketDataUser, "marketDataUser");
    _liveDataClient = liveDataClient;
    _availabilityProvider = availabilityProvider;
    _underlyingProvider = new InMemoryLKVMarketDataProvider();
    _permissionProvider = permissionProvider;
    _marketDataUser = marketDataUser;
  }

  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    subscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    final Set<LiveDataSpecification> liveDataSpecs = new HashSet<LiveDataSpecification>();
    synchronized (_liveDataSpec2Subscriptions) {
      _failedSubscriptions.removeAll(valueSpecifications); //Put these back to a waiting state so that we can try again
      for (final ValueSpecification valueSpecification : valueSpecifications) {
        final LiveDataSpecification liveDataSpec = LiveDataSpecificationLookup.getLiveDataSpecification(valueSpecification);
        Set<ValueSpecification> subscriptions = _liveDataSpec2Subscriptions.get(liveDataSpec);
        if (subscriptions == null) {
          subscriptions = new CopyOnWriteArraySet<ValueSpecification>();
          subscriptions.add(valueSpecification);
          _liveDataSpec2Subscriptions.put(liveDataSpec, subscriptions);
          liveDataSpecs.add(liveDataSpec);
        } else {
          if (subscriptions.isEmpty()) {
            liveDataSpecs.add(liveDataSpec);
          }
          subscriptions.add(valueSpecification);
        }
      }
    }
    if (!liveDataSpecs.isEmpty()) {
      _liveDataClient.subscribe(_marketDataUser, liveDataSpecs, this);
    }
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    unsubscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    final Set<LiveDataSpecification> liveDataSpecs = Sets.newHashSetWithExpectedSize(valueSpecifications.size());
    synchronized (_liveDataSpec2Subscriptions) {
      _failedSubscriptions.removeAll(valueSpecifications);
      for (final ValueSpecification valueSpecification : valueSpecifications) {
        final LiveDataSpecification liveDataSpec = LiveDataSpecificationLookup.getLiveDataSpecification(valueSpecification);
        final Set<ValueSpecification> subscriptions = _liveDataSpec2Subscriptions.get(liveDataSpec);
        if (subscriptions != null) {
          subscriptions.remove(valueSpecification);
          if (subscriptions.isEmpty()) {
            // This was the last subscription
            liveDataSpecs.add(liveDataSpec);
          }
        }
      }
    }
    if (liveDataSpecs.isEmpty()) {
      _liveDataClient.unsubscribe(_marketDataUser, liveDataSpecs, this);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
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
    final Set<ValueSpecification> subscriptions;
    synchronized (_liveDataSpec2Subscriptions) {
      subscriptions = _liveDataSpec2Subscriptions.remove(subscriptionResult.getRequestedSpecification());
      if (subscriptions == null) {
        s_logger.warn("Received subscription result for which no subscription was requested: {}", subscriptionResult);
        s_logger.debug("Current subscriptions: {}", _liveDataSpec2Subscriptions);
        return;
      }
      if (subscriptionResult.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
        final Set<ValueSpecification> existingSubscriptions = _liveDataSpec2Subscriptions.get(subscriptionResult.getFullyQualifiedSpecification());
        if (existingSubscriptions != null) {
          existingSubscriptions.addAll(subscriptions);
        } else {
          _liveDataSpec2Subscriptions.put(subscriptionResult.getFullyQualifiedSpecification(), subscriptions);
        }
        _failedSubscriptions.removeAll(subscriptions); // We expect a valueUpdate call for this later
        s_logger.debug("Subscription made to {} resulted in fully qualified {}", subscriptionResult.getRequestedSpecification(), subscriptionResult.getFullyQualifiedSpecification());
      } else {
        _failedSubscriptions.addAll(subscriptions);
        // TODO: could be more precise here, only those which weren't in _failedSpecifications
        valuesChanged(subscriptions); // PLAT-1429: wake up the init call
        if (subscriptionResult.getSubscriptionResult() == LiveDataSubscriptionResult.NOT_AUTHORIZED) {
          s_logger.warn("Subscription to {} failed because user is not authorised: {}", subscriptionResult.getRequestedSpecification(), subscriptionResult);
        } else {
          s_logger.debug("Subscription to {} failed: {}", subscriptionResult.getRequestedSpecification(), subscriptionResult);
        }
      }
    }
    if (subscriptionResult.getSubscriptionResult() == LiveDataSubscriptionResult.SUCCESS) {
      subscriptionSucceeded(subscriptions);
    } else {
      subscriptionFailed(subscriptions, subscriptionResult.getUserMessage());
    }
  }

  /* package */boolean isFailed(final ValueSpecification specification) {
    return _failedSubscriptions.contains(specification);
  }

  @Override
  public void subscriptionStopped(final LiveDataSpecification fullyQualifiedSpecification) {
    // This shouldn't really happen because there's no removeSubscription() method on this class...
    s_logger.warn("Subscription stopped " + fullyQualifiedSpecification);
  }

  @Override
  public void valueUpdate(final LiveDataValueUpdate valueUpdate) {
    s_logger.debug("Update received {}", valueUpdate);
    final Set<ValueSpecification> subscriptions = _liveDataSpec2Subscriptions.get(valueUpdate.getSpecification());
    if (subscriptions == null) {
      s_logger.warn("Received value update for which no subscriptions were found: {}", valueUpdate.getSpecification());
      return;
    }
    s_logger.debug("Subscribed values are {}", subscriptions);
    final FudgeMsg msg = valueUpdate.getFields();
    for (final ValueSpecification subscription : subscriptions) {
      // We assume all market data can be represented as a Double. The request for the field as a Double also ensures
      // that we consistently provide a Double downstream, even if the value has been represented as a more efficient
      // type in the message.
      final Double value = msg.getDouble(subscription.getValueName());
      if (value == null) {
        // TODO: Should we report this?
        continue;
      }
      _underlyingProvider.addValue(subscription, value);
    }
    valuesChanged(subscriptions);
  }

}
