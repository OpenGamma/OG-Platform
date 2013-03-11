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

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataNotSatisfiableException;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

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

  /** The underlying providers in priority order. */
  private final List<MarketDataProvider> _providers;
  /** The specs for the underlying providers in the same order as the providers. */
  private final List<MarketDataSpecification> _specs;
  /** The current subscriptions for each of the underlying providers, in the same order as the providers. */
  private final List<Set<ValueRequirement>> _subscriptions;
  private final MarketDataAvailabilityProvider _availabilityProvider = new AvailabilityProvider();
  private final PermissionsProvider _permissionsProvider = new PermissionsProvider();
  private final CopyOnWriteArraySet<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();

  /**
   * @param user The user requesting the data, not null
   * @param specs Specifications of the underlying providers in priority order, not empty
   * @param resolver For resolving market data specifications into providers, not null
   * @throws IllegalArgumentException If any of the data providers in {@code specs} can't be resolved
   */
  /* package */ ViewComputationJobDataProvider(UserPrincipal user,
                                               List<MarketDataSpecification> specs,
                                               MarketDataProviderResolver resolver) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notEmpty(specs, "specs");
    ArgumentChecker.notNull(resolver, "resolver");
    _specs = ImmutableList.copyOf(specs);
    _providers = Lists.newArrayListWithCapacity(specs.size());
    _subscriptions = Lists.newArrayListWithCapacity(specs.size());
    Listener listener = new Listener();

    for (MarketDataSpecification spec : specs) {
      MarketDataProvider provider = resolver.resolve(user, spec);
      if (provider == null) {
        throw new IllegalArgumentException("Unable to resolve market data spec " + spec);
      }
      _providers.add(provider);
      _subscriptions.add(Sets.<ValueRequirement>newHashSet());
      provider.addListener(listener);
    }
  }

  /**
   * Adds a listener that will be notified of market data updates and subscription changes.
   * @param listener The listener, not null
   */
  /* package */ void addListener(MarketDataListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  /**
   * Removes a listener.
   * @param listener The listener, not null
   */
  /* package */ void removeListener(MarketDataListener listener) {
    _listeners.remove(listener);
  }

  /**
   * Divides up the requirements into a set for each underlying provider.
   * @param requirements The market data requirements
   * @return A set of requirements for each underlying provider, in the same order as the providers
   */
  private List<Set<ValueRequirement>> partitionRequirementsByProvider(Set<ValueRequirement> requirements) {
    List<Set<ValueRequirement>> reqsByProvider = Lists.newArrayListWithCapacity(_providers.size());
    for (@SuppressWarnings("unused") MarketDataProvider ignored : _providers) {
      Set<ValueRequirement> reqs = Sets.newHashSet();
      reqsByProvider.add(reqs);
    }
    for (ValueRequirement valueRequirement : requirements) {
      for (int i = 0; i < _providers.size(); i++) {
        MarketDataProvider provider = _providers.get(i);
        if (MarketDataUtils.isAvailable(provider.getAvailabilityProvider(), valueRequirement)) {
          reqsByProvider.get(i).add(valueRequirement);
        }
      }
    }
    return reqsByProvider;
  }

  /**
   * Sets up subscriptions for market data
   * @param requirements The market data requirements, not null
   */
  /* package */ void subscribe(Set<ValueRequirement> requirements) {
    ArgumentChecker.notNull(requirements, "requirements");
    List<Set<ValueRequirement>> reqsByProvider = partitionRequirementsByProvider(requirements);
    for (int i = 0; i < reqsByProvider.size(); i++) {
      Set<ValueRequirement> newSubs = reqsByProvider.get(i);
      _providers.get(i).subscribe(newSubs);
      Set<ValueRequirement> currentSubs = _subscriptions.get(i);
      currentSubs.addAll(newSubs);
    }
  }

  /**
   * Unsubscribes from market data.
   * @param requirements The subscriptions that should be removed, not null
   */
  /* package */ void unsubscribe(Set<ValueRequirement> requirements) {
    ArgumentChecker.notNull(requirements, "requirements");
    Set<ValueRequirement> remainingReqs = Sets.newHashSet(requirements);
    for (int i = 0; i < _subscriptions.size(); i++) {
      Set<ValueRequirement> providerSubscriptions = _subscriptions.get(i);
      Set<ValueRequirement> subsToRemove = Sets.intersection(remainingReqs, providerSubscriptions);
      if (!subsToRemove.isEmpty()) {
        _providers.get(i).unsubscribe(subsToRemove);
        providerSubscriptions.removeAll(subsToRemove);
        remainingReqs.removeAll(subsToRemove);
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
    if (_providers.size() == 1) {
      MarketDataSpecification spec = Iterables.getOnlyElement(_specs);
      return Iterables.getOnlyElement(_providers).snapshot(spec);
    }
    List<MarketDataSnapshot> snapshots = Lists.newArrayListWithCapacity(_providers.size());
    for (int i = 0; i < _providers.size(); i++) {
      MarketDataSnapshot snapshot = _providers.get(i).snapshot(_specs.get(i));
      snapshots.add(snapshot);
    }
    return new CompositeMarketDataSnapshot(snapshots, new SubscriptionSupplier());
  }

  /* package */ Duration getRealTimeDuration(Instant fromInstant, Instant toInstant) {
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
    public void subscriptionSucceeded(ValueRequirement requirement) {
      for (MarketDataListener listener : _listeners) {
        listener.subscriptionSucceeded(requirement);
      }
    }

    @Override
    public void subscriptionFailed(ValueRequirement requirement, String msg) {
      for (MarketDataListener listener : _listeners) {
        listener.subscriptionFailed(requirement, msg);
      }
    }

    @Override
    public void subscriptionStopped(ValueRequirement requirement) {
      for (MarketDataListener listener : _listeners) {
        listener.subscriptionStopped(requirement);
      }
    }

    @Override
    public void valuesChanged(Collection<ValueRequirement> requirements) {
      for (MarketDataListener listener : _listeners) {
        listener.valuesChanged(requirements);
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
     * @param requirement the market data requirement, not null
     * @return The satisfaction of the requirement from the underlying providers.
     */
    @Override
    public ValueSpecification getAvailability(ValueRequirement requirement) {
      MarketDataNotSatisfiableException missing = null;
      for (MarketDataProvider provider : _providers) {
        try {
          final ValueSpecification availability = provider.getAvailabilityProvider().getAvailability(requirement);
          if (availability != null) {
            return availability;
          }
        } catch (MarketDataNotSatisfiableException e) {
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
   * {@link MarketDataPermissionProvider} that checks the permissions using the underlying {@link MarketDataProvider}s.
   * If the underlying provider's {@link MarketDataAvailabilityProvider} says the data is available the underlying
   * provider's permission provider is checked. If the user doesn't have permission the check moves on to the next
   * underlying provider.
   * TODO it would be much safer if permissions were checked using the subscriptions
   * the current impl depends on the availability providers always returning the same value for a requirement.
   * if a provider changes its mind about the availability of a requirement it's possible that the permissions check
   * would use one provider and the subscription would use another.
   * permissions are checked before subscriptions are set up so it's not possible to use the subscriptions in the
   * permissions check.
   */
  private class PermissionsProvider implements MarketDataPermissionProvider {

    /**
     * Checks permissions with the underlying providers and returns any requirements for which the user has no
     * permissions with any provider.
     * @param user The user whose market data permissions should be checked
     * @param requirements The requirements that specify the market data
     * @return Requirements for which the user has no permissions with any of the underlying providers
     */
    @Override
    public Set<ValueRequirement> checkMarketDataPermissions(UserPrincipal user, Set<ValueRequirement> requirements) {
      List<Set<ValueRequirement>> reqsByProvider = partitionRequirementsByProvider(requirements);
      Set<ValueRequirement> missingRequirements = Sets.newHashSet();
      for (int i = 0; i < _providers.size(); i++) {
        MarketDataPermissionProvider permissionProvider = _providers.get(i).getPermissionProvider();
        Set<ValueRequirement> reqsForProvider = reqsByProvider.get(i);
        missingRequirements.addAll(permissionProvider.checkMarketDataPermissions(user, reqsForProvider));
      }
      return missingRequirements;
    }
  }

  /**
   * Supplies a list of the current subscriptions for each underlying provider. This is necessary because snapshots
   * are created before subscriptions are set up but the snapshots need access to the subscriptions.
   */
  private class SubscriptionSupplier implements Supplier<List<Set<ValueRequirement>>> {

    @Override
    public List<Set<ValueRequirement>> get() {
      return _subscriptions;
    }
  }
}
