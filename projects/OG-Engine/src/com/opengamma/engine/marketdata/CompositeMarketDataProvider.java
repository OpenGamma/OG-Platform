/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.time.Duration;
import javax.time.Instant;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO should this be an interface?
 * TODO extend AbstractMarketDataProvider?
 */
public class CompositeMarketDataProvider {

  private final List<MarketDataSpecification> _specs;
  private final List<MarketDataProvider> _providers = null;
  private final MarketDataAvailabilityProvider _availabilityProvider = new AvailabilityProvider();
  private final CopyOnWriteArraySet<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();

  public CompositeMarketDataProvider(UserPrincipal user,
                                     List<MarketDataSpecification> specs,
                                     MarketDataProviderResolver resolver) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(specs, "specs");
    ArgumentChecker.notNull(resolver, "resolver");
    _specs = ImmutableList.copyOf(specs);
    for (MarketDataSpecification spec : specs) {
      MarketDataProvider provider = resolver.resolve(user, spec);
      // TODO should _providers have exactly one entry per spec? which should be null if a provider can't be resolved?
      // that might simplify the operations that take multiple specs because each spec should only apply to the
      // corresponding resolver
    }
  }

  public void addListener(MarketDataListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(MarketDataListener listener) {
    _listeners.remove(listener);
  }

  public void subscribe(Set<ValueRequirement> valueRequirements) {
    /*
    for each requirement
      for each provider
        check if the requirement is compatible
          subscribe - if subs fails try all subsequent providers - this will be async and possibly a bit involved
    */
    // TODO implement subscribe()
    throw new UnsupportedOperationException("subscribe not implemented");
  }

  public void unsubscribe(Set<ValueRequirement> valueRequirements) {
    // TODO implement this
  }

  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return _availabilityProvider;
  }

  public MarketDataPermissionProvider getPermissionProvider() {
    // TODO implement getPermissionProvider()
    throw new UnsupportedOperationException("getPermissionProvider not implemented");
    /*
    this is going to be nasty. for each requirement need to check the underlying providers. if there's no permission
    for a requirement from one provider it's not necessarily a failure - it might be available from a different
    provider. but the interface of permission provider means it's not possible to know which ones have failed. also
    the requirement is resolved into a bundle inside the permission provider so that will be done repeatedly.

    would be better if the permission provider interface took a set of live data specs and returned a set of
    requirements for which the user has permission
    */
  }

  public MarketDataSnapshot snapshot() {
    throw new UnsupportedOperationException();
  }

  public Duration getRealTimeDuration(Instant fromInstant, Instant toInstant) {
    return Duration.between(fromInstant, toInstant);
  }

  public List<MarketDataSpecification> getMarketDataSpecifications() {
    return _specs;
  }

  private class AvailabilityProvider implements MarketDataAvailabilityProvider {

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
}
