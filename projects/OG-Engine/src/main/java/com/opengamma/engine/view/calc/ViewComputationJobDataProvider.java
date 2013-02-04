/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataNotSatisfiableException;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * <p>A source of market data that aggregates data from multiple underlying {@link MarketDataProvider}s.
 * Each request for market data is handled by one of the underlying providers. When a subscription is made
 * the underlying providers are checked in priority order until one of them is able to provide the data.</p>
 * <p>All notifications of market data updates and subscription changes are delivered to all listeners. Therefore
 * instances of this class shouldn't be shared between multiple view processes.</p>
 * <p>This class isn't thread safe. It is intended for use in ViewComputationJob where it will only ever be accessed
 * by a single thread.</p>
 */
/* package */ class ViewComputationJobDataProvider {

  private static final String PROVIDER_PROPERTY = "Provider";

  /** The underlying providers in priority order. */
  private final List<MarketDataProvider> _providers;
  /** The specs for the underlying providers in the same order as the providers. */
  private final List<MarketDataSpecification> _specs;
  private final MarketDataAvailabilityProvider _availabilityProvider = new AvailabilityProvider();
  private final PermissionsProvider _permissionsProvider = new PermissionsProvider();
  private final CopyOnWriteArraySet<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();

  /**
   * @param user The user requesting the data, not null
   * @param specs Specifications of the underlying providers in priority order, not empty
   * @param resolver For resolving market data specifications into providers, not null
   * @throws IllegalArgumentException If any of the data providers in {@code specs} can't be resolved
   */
  /* package */ ViewComputationJobDataProvider(final UserPrincipal user,
      final List<MarketDataSpecification> specs,
      final MarketDataProviderResolver resolver) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notEmpty(specs, "specs");
    ArgumentChecker.notNull(resolver, "resolver");
    _specs = ImmutableList.copyOf(specs);
    _providers = Lists.newArrayListWithCapacity(specs.size());
    final Listener listener = new Listener();
    for (final MarketDataSpecification spec : specs) {
      final MarketDataProvider provider = resolver.resolve(user, spec);
      if (provider == null) {
        throw new IllegalArgumentException("Unable to resolve market data spec " + spec);
      }
      _providers.add(provider);
      provider.addListener(listener);
    }
  }

  /**
   * Adds a listener that will be notified of market data updates and subscription changes.
   * @param listener The listener, not null
   */
  /* package */ void addListener(final MarketDataListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  /**
   * Removes a listener.
   * @param listener The listener, not null
   */
  /* package */ void removeListener(final MarketDataListener listener) {
    _listeners.remove(listener);
  }

  /**
   * Divides up the specifications into a set for each underlying provider. The values from each underlying will have been tagged with a provider property which indicates the underlying they came
   * from. This is removed from the returned result so the original value specifications as used by the underlying are returned.
   *
   * @param specifications The market data specifications
   * @return A set of specifications for each underlying provider, in the same order as the providers
   */
  private static List<Set<ValueSpecification>> partitionSpecificationsByProvider(final int numProviders, final Set<ValueSpecification> specifications) {
    final List<Set<ValueSpecification>> result = Lists.newArrayListWithCapacity(numProviders);
    for (int i = 0; i < numProviders; i++) {
      result.add(Sets.<ValueSpecification>newHashSet());
    }
    for (final ValueSpecification specification : specifications) {
      String provider = specification.getProperty(PROVIDER_PROPERTY);
      if (provider != null) {
        final ValueProperties.Builder underlyingProperties = specification.getProperties().copy().withoutAny(PROVIDER_PROPERTY);
        final int slash = provider.indexOf('/');
        if (slash > 0) {
          underlyingProperties.with(PROVIDER_PROPERTY, provider.substring(0, slash));
          provider = provider.substring(slash + 1);
        }
        try {
          result.get(Integer.parseInt(provider)).add(new ValueSpecification(specification.getValueName(), specification.getTargetSpecification(), underlyingProperties.get()));
        } catch (final NumberFormatException e) {
          // Ignore
        }
      }
    }
    return result;
  }

  /**
   * Identifies the provider a given specification is used for. The values from the underlying will have been tagged with a provider property which indicates the underlying. Thsi is removed from the
   * result so the original value specification as used by the underlying is returned.
   *
   * @param specification the specification to test
   * @return the provider index and the underlying's specification, or null if it could not be found
   */
  private static Pair<Integer, ValueSpecification> getProviderSpecification(final ValueSpecification specification) {
    String provider = specification.getProperty(PROVIDER_PROPERTY);
    if (provider != null) {
      final ValueProperties.Builder underlyingProperties = specification.getProperties().copy().withoutAny(PROVIDER_PROPERTY);
      final int slash = provider.indexOf('/');
      if (slash > 0) {
        underlyingProperties.with(PROVIDER_PROPERTY, provider.substring(0, slash));
        provider = provider.substring(slash + 1);
      }
      try {
        return Pair.of(Integer.parseInt(provider), new ValueSpecification(specification.getValueName(), specification.getTargetSpecification(), underlyingProperties.get()));
      } catch (final NumberFormatException e) {
        // Ignore
      }
    }
    return null;
  }

  /**
   * Converts a value specification as used by a given underlying to one that can be used by this provider. An integer identifier for the underlying provider will be put into a property that the
   * {@link #partitionSpecificationsByProvider} helper will use to map the specification back to the originating underlying.
   *
   * @param providerId the index of the provider in the list
   * @param underlying the value specification as used by the underlying
   * @return a value specification for external use
   */
  private static ValueSpecification convertUnderlyingSpecification(final int providerId, final ValueSpecification underlying) {
    final ValueProperties.Builder properties = underlying.getProperties().copy();
    final String dataProvider = underlying.getProperty(ValuePropertyNames.DATA_PROVIDER);
    if (dataProvider != null) {
      properties.withoutAny(ValuePropertyNames.DATA_PROVIDER).with(ValuePropertyNames.DATA_PROVIDER, dataProvider + "/" + Integer.toString(providerId));
    } else {
      properties.with(ValuePropertyNames.DATA_PROVIDER, Integer.toString(providerId));
    }
    return new ValueSpecification(underlying.getValueName(), underlying.getTargetSpecification(), properties.get());
  }

  /**
   * Sets up subscriptions for market data
   *
   * @param specifications The market data items, not null
   */
  /* package */void subscribe(final Set<ValueSpecification> specifications) {
    ArgumentChecker.notNull(specifications, "specifications");
    final List<Set<ValueSpecification>> specificationsByProvider = partitionSpecificationsByProvider(_providers.size(), specifications);
    for (int i = 0; i < specificationsByProvider.size(); i++) {
      final Set<ValueSpecification> subscribe = specificationsByProvider.get(i);
      if (!subscribe.isEmpty()) {
        _providers.get(i).subscribe(subscribe);
      }
    }
  }

  /**
   * Unsubscribes from market data.
   *
   * @param specifications The subscriptions that should be removed, not null
   */
  /* package */void unsubscribe(final Set<ValueSpecification> specifications) {
    ArgumentChecker.notNull(specifications, "requirements");
    final List<Set<ValueSpecification>> specificationsByProvider = partitionSpecificationsByProvider(_providers.size(), specifications);
    for (int i = 0; i < specificationsByProvider.size(); i++) {
      final Set<ValueSpecification> unsubscribe = specificationsByProvider.get(i);
      if (!unsubscribe.isEmpty()) {
        _providers.get(i).unsubscribe(unsubscribe);
      }
    }
  }

  /**
   * @return An availability provider backed by the availability providers of the underlying market data providers
   */
  /* package */ MarketDataAvailabilityProvider getAvailabilityProvider() {
    return _availabilityProvider;
  }

  /**
   * @return An permissions provider backed by the permissions providers of the underlying market data providers
   */
  /* package */ MarketDataPermissionProvider getPermissionProvider() {
    return _permissionsProvider;
  }

  /**
   * @return A snapshot of market data backed by snapshots from the underlying providers.
   */
  /* package */ MarketDataSnapshot snapshot() {
    final List<MarketDataSnapshot> snapshots = Lists.newArrayListWithCapacity(_providers.size());
    for (int i = 0; i < _providers.size(); i++) {
      final MarketDataSnapshot snapshot = _providers.get(i).snapshot(_specs.get(i));
      snapshots.add(snapshot);
    }
    return new CompositeMarketDataSnapshot(snapshots, new ValueSpecificationProvider(_providers.size()));
  }

  /* package */ Duration getRealTimeDuration(final Instant fromInstant, final Instant toInstant) {
    return Duration.between(fromInstant, toInstant);
  }

  /* package */ List<MarketDataSpecification> getMarketDataSpecifications() {
    return _specs;
  }

  /**
   * Listens for updates from the underlying providers and distributes them to the listeners. This is
   * an inner class to avoid polluting the API with public listener methods that users of the class
   * aren't interested in.
   */
  private class Listener implements MarketDataListener {

    @Override
    public void subscriptionSucceeded(final ValueSpecification valueSpecification) {
      for (final MarketDataListener listener : _listeners) {
        listener.subscriptionSucceeded(valueSpecification);
      }
    }

    @Override
    public void subscriptionFailed(final ValueSpecification valueSpecification, final String msg) {
      for (final MarketDataListener listener : _listeners) {
        listener.subscriptionFailed(valueSpecification, msg);
      }
    }

    @Override
    public void subscriptionStopped(final ValueSpecification valueSpecification) {
      for (final MarketDataListener listener : _listeners) {
        listener.subscriptionStopped(valueSpecification);
      }
    }

    @Override
    public void valuesChanged(final Collection<ValueSpecification> valueSpecifications) {
      for (final MarketDataListener listener : _listeners) {
        listener.valuesChanged(valueSpecifications);
      }
    }
  }

  /**
   * {@link MarketDataAvailabilityProvider} that checks the underlying providers for availability. If the data
   * is available from any underlying provider then it is available. If it isn't available but is missing from any
   * of the underlying providers then it is missing. Otherwise it is unavailable.
   */
  private class AvailabilityProvider implements MarketDataAvailabilityProvider {

    /**
     * @param desiredValue the market data requirement, not null
     * @return The satisfaction of the requirement from the underlying providers.
     */
    @Override
    public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
      MarketDataNotSatisfiableException missing = null;
      for (int i = 0; i < _providers.size(); i++) {
        final MarketDataProvider provider = _providers.get(i);
        try {
          final ValueSpecification underlying = provider.getAvailabilityProvider().getAvailability(targetSpec, target, desiredValue);
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
  }

  /**
   * {@link MarketDataPermissionProvider} that checks the permissions using the underlying {@link MarketDataProvider}s. The underlying provider will be the one that returned the original availability
   * of the data.
   */
  private class PermissionsProvider implements MarketDataPermissionProvider {

    /**
     * Checks permissions with the underlying providers and returns any requirements for which the user has no permissions with any provider.
     *
     * @param user The user whose market data permissions should be checked
     * @param specifications The market data to check access to
     * @return Values for which the user has no permissions with any of the underlying providers
     */
    @Override
    public Set<ValueSpecification> checkMarketDataPermissions(final UserPrincipal user, final Set<ValueSpecification> specifications) {
      final List<Set<ValueSpecification>> specsByProvider = partitionSpecificationsByProvider(_providers.size(), specifications);
      final Set<ValueSpecification> missingSpecifications = Sets.newHashSet();
      for (int i = 0; i < _providers.size(); i++) {
        final MarketDataPermissionProvider permissionProvider = _providers.get(i).getPermissionProvider();
        final Set<ValueSpecification> specsForProvider = specsByProvider.get(i);
        final Set<ValueSpecification> missing = permissionProvider.checkMarketDataPermissions(user, specsForProvider);
        if (!missing.isEmpty()) {
          for (final ValueSpecification specification : missing) {
            missingSpecifications.add(convertUnderlyingSpecification(i, specification));
          }
        }
      }
      return missingSpecifications;
    }
  }

  /* package */static class ValueSpecificationProvider {

    private final int _numProviders;

    /* package */ValueSpecificationProvider(final int numProviders) {
      _numProviders = numProviders;
    }

    public Pair<Integer, ValueSpecification> getUnderlyingAndSpecification(final ValueSpecification specification) {
      return ViewComputationJobDataProvider.getProviderSpecification(specification);
    }

    public List<Set<ValueSpecification>> getUnderlyingSpecifications(final Set<ValueSpecification> specifications) {
      return ViewComputationJobDataProvider.partitionSpecificationsByProvider(_numProviders, specifications);
    }

    public ValueSpecification convertUnderlyingSpecification(final int providerId, final ValueSpecification specification) {
      return ViewComputationJobDataProvider.convertUnderlyingSpecification(providerId, specification);
    }

  }

}
