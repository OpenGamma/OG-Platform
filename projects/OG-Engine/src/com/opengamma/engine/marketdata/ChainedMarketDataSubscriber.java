/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Subscribes to data from an underlying {@link MarketDataProvider} or forwards requests to the next subscriber
 * in the chain if the data isn't available from the provider or if subscription fails. If subscription fails for
 * the last subscriber in the chain the underlying listener is informed.
 * TODO does this need a close() method to remove itself as a listener from the provider?
 */
/* package */ class ChainedMarketDataSubscriber {

  private final MarketDataListener _listener;
  private final MarketDataProvider _provider;
  private final ChainedMarketDataSubscriber _nextSubscriber;
  private final Set<ValueRequirement> _subscriptions = Collections.newSetFromMap(new ConcurrentHashMap<ValueRequirement, Boolean>());
  private final MarketDataSpecification _marketDataSpec;

  private ChainedMarketDataSubscriber(MarketDataListener listener,
                                      MarketDataProvider provider,
                                      MarketDataSpecification marketDataSpec,
                                      ChainedMarketDataSubscriber nextSubscriber) {
    ArgumentChecker.notNull(listener, "listener");
    ArgumentChecker.notNull(provider, "provider");
    ArgumentChecker.notNull(marketDataSpec, "marketDataSpec");
    _listener = listener;
    _provider = provider;
    _marketDataSpec = marketDataSpec;
    _nextSubscriber = nextSubscriber;
    _provider.addListener(new Listener());
  }

  private ChainedMarketDataSubscriber(MarketDataListener listener,
                                      MarketDataProvider provider,
                                      MarketDataSpecification marketDataSpec) {
    this(listener, provider, marketDataSpec, null);
  }

  /* package */ static ChainedMarketDataSubscriber createChain(List<ProviderWithSpec> providers, MarketDataListener listener) {
    ArgumentChecker.notEmpty(providers, "providers");
    ArgumentChecker.notNull(listener, "listener");
    // each subscriber needs a reference to the next so they must be created in reverse order
    List<ProviderWithSpec> reverseProviders = Lists.reverse(providers);
    // the first subscriber created is the last in the chain and doesn't have a following subscriber
    ProviderWithSpec lastProviderWithSpec = reverseProviders.get(0);
    MarketDataProvider lastProvider = lastProviderWithSpec.getProvider();
    MarketDataSpecification lastSpec = lastProviderWithSpec.getSpecification();
    ChainedMarketDataSubscriber subscriber = new ChainedMarketDataSubscriber(listener, lastProvider, lastSpec);
    for (ProviderWithSpec providerWithSpec : reverseProviders.subList(1, reverseProviders.size())) {
      MarketDataProvider provider = providerWithSpec.getProvider();
      MarketDataSpecification spec = providerWithSpec.getSpecification();
      subscriber = new ChainedMarketDataSubscriber(listener, provider, spec, subscriber);
    }
    // return the first subscriber in the chain
    return subscriber;
  }

  /* package */ void subscribe(ValueRequirement requirement) {
    MarketDataAvailability availability = _provider.getAvailabilityProvider().getAvailability(requirement);
    if (availability == MarketDataAvailability.AVAILABLE) {
      _provider.subscribe(requirement);
    } else if (_nextSubscriber != null) {
      _nextSubscriber.subscribe(requirement);
    } else {
      _listener.subscriptionFailed(requirement, "No provider has data available for requirement " + requirement);
    }
  }

  /* package */ void unsubscribe(Set<ValueRequirement> requirements) {
    Set<ValueRequirement> subscribed = Sets.intersection(requirements, _subscriptions);
    Set<ValueRequirement> notSubscribed = Sets.difference(requirements, _subscriptions);
    _provider.unsubscribe(subscribed);
    if (_nextSubscriber != null) {
      _nextSubscriber.unsubscribe(notSubscribed);
    }
  }

  /* package */ List<UnderlyingSnapshot> snapshot() {
    MarketDataSnapshot snapshot = _provider.snapshot(_marketDataSpec);
    Set<ValueRequirement> subscriptions = Collections.unmodifiableSet(_subscriptions);
    UnderlyingSnapshot underlyingSnapshot = new UnderlyingSnapshot(snapshot, subscriptions);
    if (_nextSubscriber == null) {
      return Collections.singletonList(underlyingSnapshot);
    } else {
      return ImmutableList.<UnderlyingSnapshot>builder().add(underlyingSnapshot).addAll(_nextSubscriber.snapshot()).build();
    }
  }

  /* package */ MarketDataPermissionProvider getPermissionProvider() {
    return new PermissionsProvider();
  }

  /**
   * This is an inner class to avoid polluting the API with public listener methods that users of the class
   * aren't interested in.
   */
  private class Listener implements MarketDataListener {

    @Override
    public void subscriptionSucceeded(ValueRequirement requirement) {
      _subscriptions.add(requirement);
      _listener.subscriptionSucceeded(requirement);
    }

    @Override
    public void subscriptionFailed(ValueRequirement requirement, String msg) {
      if (_nextSubscriber != null) {
        _nextSubscriber.subscribe(requirement);
      } else {
        _listener.subscriptionFailed(requirement, msg);
      }
    }

    @Override
    public void subscriptionStopped(ValueRequirement requirement) {
      _subscriptions.remove(requirement);
      _listener.subscriptionStopped(requirement);
    }

    @Override
    public void valuesChanged(Collection<ValueRequirement> requirements) {
      _listener.valuesChanged(requirements);
    }
  }

  /**
   * Checks permissions using the underlying provider and passes the request down the chain. This relies on
   * subscriptions being set up before the permissions provider is used.
   */
  private class PermissionsProvider implements MarketDataPermissionProvider {

    @Override
    public Set<ValueRequirement> checkMarketDataPermissions(UserPrincipal user, Set<ValueRequirement> requirements) {
      // only check permissions for data which we are subscribed to
      Set<ValueRequirement> reqsToCheck = Sets.intersection(requirements, _subscriptions);
      Set<ValueRequirement> permissionDenied = Sets.newHashSet();
      // TODO this logic is nonsense, also need to return requirements that no-one is subscribed to
      // I think that's going to be hard the way things are structured
      permissionDenied.addAll(_provider.getPermissionProvider().checkMarketDataPermissions(user, reqsToCheck));
      if (_nextSubscriber != null) {
        permissionDenied.addAll(_nextSubscriber.getPermissionProvider().checkMarketDataPermissions(user, requirements));
      }
      return permissionDenied;
    }
  }
}

