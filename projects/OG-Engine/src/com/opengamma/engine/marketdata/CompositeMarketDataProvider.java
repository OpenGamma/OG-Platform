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
import java.util.concurrent.CopyOnWriteArraySet;

import javax.time.Duration;
import javax.time.Instant;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO should this be an interface?
 * TODO should this be in the same package as ViewComputationJob? it's not used anywhere else and isn't general purpose
 */
public class CompositeMarketDataProvider {

  private final List<MarketDataSpecification> _specs;
  private final List<MarketDataProvider> _providers;
  private final List<Set<ValueRequirement>> _subscriptions;
  //private final ChainedMarketDataSubscriber _subscriber;
  private final MarketDataAvailabilityProvider _availabilityProvider = new AvailabilityProvider();
  private final PermissionsProvider _permissionsProvider = new PermissionsProvider();
  private final CopyOnWriteArraySet<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();

  public CompositeMarketDataProvider(UserPrincipal user,
                                     List<MarketDataSpecification> specs,
                                     MarketDataProviderResolver resolver) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notEmpty(specs, "specs");
    ArgumentChecker.notNull(resolver, "resolver");
    _specs = ImmutableList.copyOf(specs);
    _providers = Lists.newArrayListWithCapacity(specs.size());
    _subscriptions = Lists.newArrayListWithCapacity(specs.size());
    //List<ProviderWithSpec> providersWithSpecs = Lists.newArrayListWithCapacity(specs.size());
    Listener listener = new Listener();

    for (MarketDataSpecification spec : specs) {
      MarketDataProvider provider = resolver.resolve(user, spec);
      if (provider == null) {
        throw new IllegalArgumentException("Unable to resolve market data spec " + spec);
      }
      _providers.add(provider);
      _subscriptions.add(Sets.<ValueRequirement>newHashSet());
      provider.addListener(listener);
      //providersWithSpecs.add(new ProviderWithSpec(provider, spec));
    }

    //_subscriber = ChainedMarketDataSubscriber.createChain(providersWithSpecs, new Listener());
  }

  public void addListener(MarketDataListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(MarketDataListener listener) {
    _listeners.remove(listener);
  }

  private List<Set<ValueRequirement>> partitionRequirementsByProvider(Set<ValueRequirement> requirements) {
    List<Set<ValueRequirement>> reqsByProvider = Lists.newArrayListWithCapacity(_providers.size());
    for (MarketDataProvider ignored : _providers) {
      Set<ValueRequirement> reqs = Sets.newHashSet();
      reqsByProvider.add(reqs);
    }
    for (ValueRequirement valueRequirement : requirements) {
      for (int i = 0; i < _providers.size(); i++) {
        MarketDataProvider provider = _providers.get(i);
        if (provider.getAvailabilityProvider().getAvailability(valueRequirement) == MarketDataAvailability.AVAILABLE) {
          reqsByProvider.get(i).add(valueRequirement);
        }
      }
    }
    return reqsByProvider;
  }

  public void subscribe(Set<ValueRequirement> requirements) {
    List<Set<ValueRequirement>> reqsByProvider = partitionRequirementsByProvider(requirements);
    for (int i = 0; i < reqsByProvider.size(); i++) {
      Set<ValueRequirement> newSubs = reqsByProvider.get(i);
      _providers.get(i).subscribe(newSubs);
      Set<ValueRequirement> currentSubs = _subscriptions.get(i);
      currentSubs.addAll(newSubs);
    }
  }

  public void unsubscribe(Set<ValueRequirement> requirements) {
    // TODO optimise this by storing a map of requirements to provider indices?
    List<Set<ValueRequirement>> reqsByProvider = partitionRequirementsByProvider(requirements);
    for (int i = 0; i < reqsByProvider.size(); i++) {
      Set<ValueRequirement> newSubs = reqsByProvider.get(i);
      _providers.get(i).unsubscribe(newSubs);
      Set<ValueRequirement> currentSubs = _subscriptions.get(i);
      currentSubs.removeAll(newSubs);
    }
  }

  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return _availabilityProvider;
  }

  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionsProvider;
  }

  public MarketDataSnapshot snapshot() {
    List<MarketDataSnapshot> snapshots = Lists.newArrayListWithCapacity(_providers.size());
    for (int i = 0; i < _providers.size(); i++) {
      MarketDataSnapshot snapshot = _providers.get(i).snapshot(_specs.get(i));
      snapshots.add(snapshot);
    }
    return new CompositeMarketDataSnapshot(snapshots, new SubscriptionSupplier());
  }

  public Duration getRealTimeDuration(Instant fromInstant, Instant toInstant) {
    return Duration.between(fromInstant, toInstant);
  }

  // TODO get rid of this method
  public List<MarketDataSpecification> getMarketDataSpecifications() {
    return _specs;
  }

  /**
   * This is an inner class to avoid polluting the API with public listener methods that users of the class
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
     * @param requirement  the market data requirement, not null
     * @return The availablility of the requirement from the underlying providers.
     */
    @Override
    public MarketDataAvailability getAvailability(ValueRequirement requirement) {
      boolean missing = false;
      for (MarketDataProvider provider : _providers) {
        MarketDataAvailability availability = provider.getAvailabilityProvider().getAvailability(requirement);
        if (availability == MarketDataAvailability.AVAILABLE) {
          return MarketDataAvailability.AVAILABLE;
        } else if (availability == MarketDataAvailability.MISSING) {
          missing = true;
        }
      }
      if (missing) {
        return MarketDataAvailability.MISSING;
      } else {
        return MarketDataAvailability.NOT_AVAILABLE;
      }
    }
  }

  /**
   * {@link MarketDataPermissionProvider} that checks the permissions using the underlying {@link MarketDataProvider}s.
   * If the underlying provider's {@link MarketDataAvailabilityProvider} says the data is available the underlying
   * provider's permission provider is checked. If the user doesn't have permission the check moves on to the next
   * underlying provider.
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
      Set<ValueRequirement> missingRequirements = Sets.newHashSet();
      requirements:
      for (ValueRequirement requirement : requirements) {
        for (MarketDataProvider provider : _providers) {
          MarketDataAvailabilityProvider availabilityProvider = provider.getAvailabilityProvider();
          if (availabilityProvider.getAvailability(requirement) == MarketDataAvailability.AVAILABLE) {
            MarketDataPermissionProvider permissionProvider = provider.getPermissionProvider();
            Set<ValueRequirement> requirementSet = Collections.singleton(requirement);
            missingRequirements.addAll(permissionProvider.checkMarketDataPermissions(user, requirementSet));
            continue requirements;
          }
        }
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
