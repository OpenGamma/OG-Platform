/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataNotSatisfiableException;
import com.opengamma.engine.marketdata.availability.UnionMarketDataAvailability;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A source of market data that aggregates data from multiple underlying {@link MarketDataProvider}s. Each request for market data is handled by one of the underlying providers. When a subscription is
 * made the underlying providers are checked in priority order until one of them is able to provide the data.
 * <p>
 * All notifications of market data updates and subscription changes are delivered to all listeners. Therefore instances of this class shouldn't be shared between multiple view processes.
 */
public class SnapshottingViewExecutionDataProvider extends ViewExecutionDataProvider {

  private final MarketDataAvailabilityProvider _availabilityProvider;
  private final CopyOnWriteArraySet<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();

  /**
   * @param user The user requesting the data, not null
   * @param specs Specifications of the underlying providers in priority order, not empty
   * @param resolver For resolving market data specifications into providers, not null
   * @throws IllegalArgumentException If any of the data providers in {@code specs} can't be resolved
   */
  public SnapshottingViewExecutionDataProvider(final UserPrincipal user,
      final List<MarketDataSpecification> specs,
      final MarketDataProviderResolver resolver) {
    super(user, specs, resolver);
    final MarketDataListener listener = new Listener();
    if (getSpecifications().size() == 1) {
      final MarketDataProvider provider = getProviders().get(0);
      provider.addListener(listener);
      _availabilityProvider = provider.getAvailabilityProvider(getSpecifications().get(0));
    } else {
      int index = 0;
      for (MarketDataProvider provider : getProviders()) {
        provider.addListener(new CompositeListener(index++, listener));
      }
      _availabilityProvider = new CompositeAvailabilityProvider(getProviders(), getSpecifications());
    }
  }

  /**
   * Adds a listener that will be notified of market data updates and subscription changes.
   * 
   * @param listener The listener, not null
   */
  public void addListener(final MarketDataListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  /**
   * Removes a listener.
   * 
   * @param listener The listener, not null
   */
  public void removeListener(final MarketDataListener listener) {
    _listeners.remove(listener);
  }

  /**
   * Sets up subscriptions for market data
   * 
   * @param specifications The market data items, not null
   */
  public void subscribe(final Set<ValueSpecification> specifications) {
    ArgumentChecker.notNull(specifications, "specifications");
    final List<Set<ValueSpecification>> specificationsByProvider = partitionSpecificationsByProvider(getProviders().size(), specifications);
    for (int i = 0; i < specificationsByProvider.size(); i++) {
      final Set<ValueSpecification> subscribe = specificationsByProvider.get(i);
      if (!subscribe.isEmpty()) {
        getProviders().get(i).subscribe(subscribe);
      }
    }
  }

  /**
   * Unsubscribes from market data.
   * 
   * @param specifications The subscriptions that should be removed, not null
   */
  public void unsubscribe(final Set<ValueSpecification> specifications) {
    ArgumentChecker.notNull(specifications, "requirements");
    final List<Set<ValueSpecification>> specificationsByProvider = partitionSpecificationsByProvider(getProviders().size(), specifications);
    for (int i = 0; i < specificationsByProvider.size(); i++) {
      final Set<ValueSpecification> unsubscribe = specificationsByProvider.get(i);
      if (!unsubscribe.isEmpty()) {
        getProviders().get(i).unsubscribe(unsubscribe);
      }
    }
  }

  /**
   * @return An availability provider backed by the availability providers of the underlying market data providers
   */
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return _availabilityProvider;
  }

  /**
   * @return A snapshot of market data backed by snapshots from the underlying providers.
   */
  public MarketDataSnapshot snapshot() {
    final int providers = getProviders().size();
    if (providers == 1) {
      return getProviders().get(0).snapshot(getSpecifications().get(0));
    }
    final List<MarketDataSnapshot> snapshots = Lists.newArrayListWithCapacity(providers);
    for (int i = 0; i < providers; i++) {
      final MarketDataSnapshot snapshot = getProviders().get(i).snapshot(getSpecifications().get(i));
      snapshots.add(snapshot);
    }
    return new CompositeMarketDataSnapshot(snapshots, new ValueSpecificationProvider(providers));
  }

  public Duration getRealTimeDuration(final Instant fromInstant, final Instant toInstant) {
    return Duration.between(fromInstant, toInstant);
  }

  /**
   * Distributes the updates to the listeners.
   */
  private class Listener implements MarketDataListener {

    @Override
    public void subscriptionsSucceeded(Collection<ValueSpecification> valueSpecifications) {
      for (final MarketDataListener listener : _listeners) {
        listener.subscriptionsSucceeded(valueSpecifications);
      }
    }

    @Override
    public void subscriptionFailed(ValueSpecification valueSpecification, final String msg) {
      for (final MarketDataListener listener : _listeners) {
        listener.subscriptionFailed(valueSpecification, msg);
      }
    }

    @Override
    public void subscriptionStopped(ValueSpecification valueSpecification) {
      for (final MarketDataListener listener : _listeners) {
        listener.subscriptionStopped(valueSpecification);
      }
    }

    @Override
    public void valuesChanged(Collection<ValueSpecification> valueSpecifications) {
      for (final MarketDataListener listener : _listeners) {
        listener.valuesChanged(valueSpecifications);
      }
    }

  }

  /**
   * Listens for updates from the underlying providers and distributes them to the listeners.
   */
  private static class CompositeListener implements MarketDataListener {

    private final int _providerId;
    private final MarketDataListener _underlying;

    public CompositeListener(final int providerId, final MarketDataListener underlying) {
      _providerId = providerId;
      _underlying = underlying;
    }

    private ValueSpecification convertSpecification(final ValueSpecification valueSpecification) {
      return convertUnderlyingSpecification(_providerId, valueSpecification);
    }

    private Collection<ValueSpecification> convertSpecifications(final Collection<ValueSpecification> valueSpecifications) {
      final Collection<ValueSpecification> result = new ArrayList<ValueSpecification>(valueSpecifications.size());
      for (final ValueSpecification valueSpecification : valueSpecifications) {
        result.add(convertSpecification(valueSpecification));
      }
      return result;
    }

    @Override
    public void subscriptionsSucceeded(Collection<ValueSpecification> valueSpecifications) {
      _underlying.subscriptionsSucceeded(convertSpecifications(valueSpecifications));
    }

    @Override
    public void subscriptionFailed(ValueSpecification valueSpecification, final String msg) {
      _underlying.subscriptionFailed(convertSpecification(valueSpecification), msg);
    }

    @Override
    public void subscriptionStopped(ValueSpecification valueSpecification) {
      _underlying.subscriptionStopped(convertSpecification(valueSpecification));
    }

    @Override
    public void valuesChanged(Collection<ValueSpecification> valueSpecifications) {
      _underlying.valuesChanged(convertSpecifications(valueSpecifications));
    }

  }

  /**
   * {@link MarketDataAvailabilityProvider} that checks the underlying providers for availability. If the data is available from any underlying provider then it is available. If it isn't available but
   * is missing from any of the underlying providers then it is missing. Otherwise it is unavailable.
   */
  private static final class CompositeAvailabilityProvider implements MarketDataAvailabilityProvider {

    private final List<MarketDataAvailabilityProvider> _providers;
    private final Serializable _cacheHint;

    public CompositeAvailabilityProvider(final List<MarketDataProvider> providers, final List<MarketDataSpecification> specs) {
      _providers = new ArrayList<MarketDataAvailabilityProvider>(providers.size());
      final ArrayList<Serializable> cacheHints = new ArrayList<Serializable>(providers.size());
      for (int i = 0; i < providers.size(); i++) {
        final MarketDataAvailabilityProvider availabilityProvider = providers.get(i).getAvailabilityProvider(specs.get(i));
        _providers.add(availabilityProvider);
        cacheHints.add(availabilityProvider.getAvailabilityHintKey());
      }
      _cacheHint = cacheHints;
    }

    /**
     * @param desiredValue the market data requirement, not null
     * @return The satisfaction of the requirement from the underlying providers.
     */
    @Override
    public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
      MarketDataNotSatisfiableException missing = null;
      for (int i = 0; i < _providers.size(); i++) {
        final MarketDataAvailabilityProvider provider = _providers.get(i);
        try {
          final ValueSpecification underlying = provider.getAvailability(targetSpec, target, desiredValue);
          if (underlying != null) {
            return convertUnderlyingSpecification(i, underlying);
          }
        } catch (final MarketDataNotSatisfiableException e) {
          missing = e;
        }
      }
      if (missing != null) {
        throw missing;
      } else {
        return null;
      }
    }

    @Override
    public MarketDataAvailabilityFilter getAvailabilityFilter() {
      final List<MarketDataAvailabilityFilter> union = new ArrayList<MarketDataAvailabilityFilter>(_providers.size());
      for (MarketDataAvailabilityProvider provider : _providers) {
        union.add(provider.getAvailabilityFilter());
      }
      return new UnionMarketDataAvailability.Filter(union);
    }

    @Override
    public Serializable getAvailabilityHintKey() {
      return _cacheHint;
    }

  }

  /* package */static class ValueSpecificationProvider {

    private final int _numProviders;

    /* package */ValueSpecificationProvider(final int numProviders) {
      _numProviders = numProviders;
    }

    public Pair<Integer, ValueSpecification> getUnderlyingAndSpecification(final ValueSpecification specification) {
      return SnapshottingViewExecutionDataProvider.getProviderSpecification(specification);
    }

    public List<Set<ValueSpecification>> getUnderlyingSpecifications(final Set<ValueSpecification> specifications) {
      return SnapshottingViewExecutionDataProvider.partitionSpecificationsByProvider(_numProviders, specifications);
    }

    public ValueSpecification convertUnderlyingSpecification(final int providerId, final ValueSpecification specification) {
      return SnapshottingViewExecutionDataProvider.convertUnderlyingSpecification(providerId, specification);
    }

  }

}
