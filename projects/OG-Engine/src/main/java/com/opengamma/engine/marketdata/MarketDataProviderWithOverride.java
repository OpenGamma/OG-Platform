/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.lambdava.tuple.Pair;

/**
 * Combines a {@link MarketDataProvider} with another which provides overrides.
 */
public class MarketDataProviderWithOverride implements MarketDataProvider {

  private static class Subscription {

    private String _errorMessage;
    private int _pendingCount = 2;

    public boolean pending() {
      return --_pendingCount > 0;
    }

    public String getError() {
      return _errorMessage;
    }

    public void setError(final String msg) {
      _errorMessage = msg;
    }

    public String getError(final String msg) {
      if (_errorMessage != null) {
        return _errorMessage + ", " + msg;
      } else {
        return msg;
      }
    }

  }

  private final MarketDataProvider _underlying;
  private final MarketDataProvider _override;
  private final Set<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();
  private final Map<ValueSpecification, Subscription> _pendingSubscriptions = new HashMap<ValueSpecification, Subscription>();
  private int _pendingErrors;

  private boolean _listenerAttached;
  private final MarketDataListener _listener = new MarketDataListener() {

    @Override
    public void subscriptionsSucceeded(Collection<ValueSpecification> specifications) {
      Collection<ValueSpecification> success = null;
      Collection<Pair<ValueSpecification, String>> error = null;
      synchronized (_pendingSubscriptions) {
        for (ValueSpecification specification : specifications) {
          final Subscription subscription = _pendingSubscriptions.get(specification);
          if (subscription != null) {
            if (!subscription.pending()) {
              _pendingSubscriptions.remove(specification);
              String err = subscription.getError();
              if (err != null) {
                if (error == null) {
                  error = new ArrayList<Pair<ValueSpecification, String>>(_pendingErrors);
                }
                error.add(Pair.of(specification, err));
              } else {
                if (success == null) {
                  success = new ArrayList<ValueSpecification>(specifications.size());
                }
                success.add(specification);
              }
            }
          }
        }
        if (error != null) {
          _pendingErrors -= error.size();
        }
      }
      if (success != null) {
        MarketDataProviderWithOverride.this.subscriptionsSucceeded(success);
      }
      if (error != null) {
        for (Pair<ValueSpecification, String> entry : error) {
          MarketDataProviderWithOverride.this.subscriptionFailed(entry.getFirst(), entry.getSecond());
        }
      }
    }

    @Override
    public void subscriptionFailed(ValueSpecification specification, String msg) {
      String error = null;
      synchronized (_pendingSubscriptions) {
        final Subscription subscription = _pendingSubscriptions.get(specification);
        if (subscription != null) {
          if (subscription.pending()) {
            subscription.setError(msg);
            _pendingErrors++;
          } else {
            _pendingSubscriptions.remove(specification);
            error = subscription.getError(msg);
          }
        }
      }
      if (error != null) {
        MarketDataProviderWithOverride.this.subscriptionFailed(specification, error);
      }
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
  public MarketDataProviderWithOverride(final MarketDataProvider underlying, final MarketDataProvider override) {
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
        _override.addListener(_listener);
        _listenerAttached = true;
      } else if (!anyListeners && _listenerAttached) {
        _underlying.removeListener(_listener);
        _override.removeListener(_listener);
        _listenerAttached = false;
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    _pendingSubscriptions.put(valueSpecification, new Subscription());
    _underlying.subscribe(valueSpecification);
    _override.subscribe(valueSpecification);
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    for (final ValueSpecification specification : valueSpecifications) {
      _pendingSubscriptions.put(specification, new Subscription());
    }
    _underlying.subscribe(valueSpecifications);
    _override.subscribe(valueSpecifications);
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    _underlying.unsubscribe(valueSpecification);
    _override.unsubscribe(valueSpecification);
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
