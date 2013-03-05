/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.StringUtils;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.PendingCombinedMarketDataSubscription.PendingCombinedSubscriptionState;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Combines a {@link MarketDataProvider} with another which provides overrides.
 */
public class MarketDataProviderWithOverride implements MarketDataProvider {

  private final MarketDataProvider _underlying;
  private final MarketDataProvider _override;
  private final Set<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();
  private final Map<ValueSpecification, PendingCombinedMarketDataSubscription> _pendingSubscriptions = new ConcurrentHashMap<ValueSpecification, PendingCombinedMarketDataSubscription>();

  private final Object _listenerLock = new Object();
  private boolean _listenerAttached;
  private final CombinedLiveDataSnapshotListener _underlyingListener;
  private final CombinedLiveDataSnapshotListener _overrideListener;

  private class CombinedLiveDataSnapshotListener implements MarketDataListener {

    private final MarketDataProvider _provider;

    public CombinedLiveDataSnapshotListener(final MarketDataProvider provider) {
      _provider = provider;
    }

    @Override
    public void subscriptionFailed(final ValueSpecification specification, final String msg) {
      final PendingCombinedMarketDataSubscription pendingSubscription = _pendingSubscriptions.get(specification);
      if (pendingSubscription == null) {
        return;
      }
      processState(pendingSubscription.subscriptionFailed(_provider, msg), pendingSubscription, specification);
    }

    @Override
    public void subscriptionStopped(final ValueSpecification specification) {
      MarketDataProviderWithOverride.this.subscriptionStopped(specification);
    }

    @Override
    public void subscriptionSucceeded(final ValueSpecification specification) {
      final PendingCombinedMarketDataSubscription pendingSubscription = _pendingSubscriptions.get(specification);
      if (pendingSubscription == null) {
        return;
      }
      processState(pendingSubscription.subscriptionSucceeded(_provider), pendingSubscription, specification);
    }

    @Override
    public void valuesChanged(final Collection<ValueSpecification> specifications) {
      MarketDataProviderWithOverride.this.valuesChanged(specifications);
    }

    private void processState(final PendingCombinedSubscriptionState state, final PendingCombinedMarketDataSubscription pendingSubscription, final ValueSpecification specification) {
      switch (state) {
        case FAILURE:
          final String msg = StringUtils.join(pendingSubscription.getFailureMessages(), ", ");
          MarketDataProviderWithOverride.this.subscriptionFailed(specification, msg);
          break;
        case SUCCESS:
          MarketDataProviderWithOverride.this.subscriptionSucceeded(specification);
          break;
      }
    }

  }

  /**
   * Constructs an instance using the specified providers.
   * 
   * @param underlying the underlying, or default, provider, not null
   * @param override the override provider, not null
   */
  public MarketDataProviderWithOverride(final MarketDataProvider underlying, final MarketDataProvider override) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(override, "override");
    _underlying = underlying;
    _underlyingListener = new CombinedLiveDataSnapshotListener(_underlying);
    _override = override;
    _overrideListener = new CombinedLiveDataSnapshotListener(_override);
  }

  //--------------------------------------------------------------------------
  @Override
  public void addListener(final MarketDataListener listener) {
    _listeners.add(listener);
    checkListenerAttach();
  }

  @Override
  public void removeListener(final MarketDataListener listener) {
    _listeners.remove(listener);
    checkListenerAttach();
  }

  private void checkListenerAttach() {
    synchronized (_listenerLock) {
      final boolean anyListeners = _listeners.size() > 0;

      if (anyListeners && !_listenerAttached) {
        _underlying.addListener(_underlyingListener);
        _override.addListener(_overrideListener);
        _listenerAttached = true;
      } else if (!anyListeners && _listenerAttached) {
        _underlying.removeListener(_underlyingListener);
        _override.removeListener(_overrideListener);
        _listenerAttached = false;
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    subscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    for (final ValueSpecification specification : valueSpecifications) {
      _pendingSubscriptions.put(specification, new PendingCombinedMarketDataSubscription(Arrays.asList(_underlying, _override)));
    }
    _underlying.subscribe(valueSpecifications);
    _override.subscribe(valueSpecifications);
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    unsubscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    _underlying.unsubscribe(valueSpecifications);
    _override.unsubscribe(valueSpecifications);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
    // Assume overrides only valid if data available from underlying
    return _underlying.getAvailabilityProvider(marketDataSpec);
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    // Assume overrides not permissioned
    return _underlying.getPermissionProvider();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    return _underlying.isCompatible(marketDataSpec) && _override.isCompatible(marketDataSpec);
  }

  @Override
  public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    final MarketDataSnapshot underlyingSnapshot = _underlying.snapshot(marketDataSpec);
    final MarketDataSnapshot overrideSnapshot = _override.snapshot(marketDataSpec);
    return new MarketDataSnapshotWithOverride(underlyingSnapshot, overrideSnapshot);
  }

  @Override
  public Duration getRealTimeDuration(final Instant fromInstant, final Instant toInstant) {
    return _underlying.getRealTimeDuration(fromInstant, toInstant);
  }

  //--------------------------------------------------------------------------
  private void subscriptionSucceeded(final ValueSpecification specification) {
    for (final MarketDataListener listener : _listeners) {
      listener.subscriptionSucceeded(specification);
    }
  }

  private void subscriptionFailed(final ValueSpecification specification, final String msg) {
    for (final MarketDataListener listener : _listeners) {
      listener.subscriptionFailed(specification, msg);
    }
  }

  private void subscriptionStopped(final ValueSpecification specification) {
    for (final MarketDataListener listener : _listeners) {
      listener.subscriptionStopped(specification);
    }
  }

  private void valuesChanged(final Collection<ValueSpecification> specifications) {
    for (final MarketDataListener listener : _listeners) {
      listener.valuesChanged(specifications);
    }
  }

}
