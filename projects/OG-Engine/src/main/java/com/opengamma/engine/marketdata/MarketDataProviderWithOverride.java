/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Combines a {@link MarketDataProvider} with another which provides overrides.
 */
public class MarketDataProviderWithOverride implements MarketDataProvider {

  private final MarketDataProvider _underlying;
  private final MarketDataInjectorImpl _override;
  private final Set<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();
  private final Set<ValueSpecification> _pendingSubscriptions = new HashSet<ValueSpecification>();

  private boolean _listenerAttached;
  private final MarketDataListener _listener = new MarketDataListener() {

    @Override
    public void subscriptionsSucceeded(Collection<ValueSpecification> specifications) {
      MarketDataProviderWithOverride.this.subscriptionsSucceeded(specifications);
    }

    @Override
    public void subscriptionFailed(ValueSpecification specification, String msg) {
      MarketDataProviderWithOverride.this.subscriptionFailed(specification, msg);
    }

    @Override
    public void subscriptionStopped(ValueSpecification specification) {
      MarketDataProviderWithOverride.this.subscriptionStopped(specification);
    }

    @Override
    public void valuesChanged(Collection<ValueSpecification> specifications) {
      MarketDataProviderWithOverride.this.valuesChanged(specifications);
    }

  };

  /**
   * Constructs an instance using the specified providers.
   * 
   * @param underlying the underlying, or default, provider, not null
   * @param override the override provider, not null
   */
  public MarketDataProviderWithOverride(final MarketDataProvider underlying, final MarketDataInjectorImpl override) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(override, "override");
    _underlying = underlying;
    _override = override;
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
    synchronized (_listener) {
      final boolean anyListeners = _listeners.size() > 0;
      if (anyListeners && !_listenerAttached) {
        _underlying.addListener(_listener);
        _listenerAttached = true;
      } else if (!anyListeners && _listenerAttached) {
        _underlying.removeListener(_listener);
        _listenerAttached = false;
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    _pendingSubscriptions.add(valueSpecification);
    _underlying.subscribe(valueSpecification);
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    _pendingSubscriptions.addAll(valueSpecifications);
    _underlying.subscribe(valueSpecifications);
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    _underlying.unsubscribe(valueSpecification);
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    _underlying.unsubscribe(valueSpecifications);
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
    return _underlying.isCompatible(marketDataSpec);
  }

  @Override
  public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    final MarketDataSnapshot underlyingSnapshot = _underlying.snapshot(marketDataSpec);
    final MarketDataInjectorImpl.Snapshot overrideSnapshot = _override.snapshot(getAvailabilityProvider(marketDataSpec));
    return new MarketDataSnapshotWithOverride(underlyingSnapshot, overrideSnapshot);
  }

  @Override
  public Duration getRealTimeDuration(final Instant fromInstant, final Instant toInstant) {
    return _underlying.getRealTimeDuration(fromInstant, toInstant);
  }

  //--------------------------------------------------------------------------
  private void subscriptionsSucceeded(final Collection<ValueSpecification> specifications) {
    for (final MarketDataListener listener : _listeners) {
      listener.subscriptionsSucceeded(specifications);
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
