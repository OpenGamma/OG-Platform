/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataNotSatisfiableException;
import com.opengamma.engine.marketdata.spec.CombinedMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.UserPrincipal;

/**
 * Implementation of {@link MarketDataProvider} which sources its data from one of two {@link MarketDataProvider}s,
 * choosing based on the availability of data.
 */
public class CombinedMarketDataProvider extends AbstractMarketDataProvider {

  private final MarketDataProvider _preferred;
  private final MarketDataProvider _fallBack;

  private final CombinedMarketDataListener _preferredListener;
  private final CombinedMarketDataListener _fallBackListener;
  private final MarketDataAvailabilityProvider _availabilityProvider;

  private final Map<ValueRequirement, MarketDataProvider> _providerByRequirement = new ConcurrentHashMap<ValueRequirement, MarketDataProvider>();

  private final Object _listenerLock = new Object();
  private boolean _listenerAttached;

  public CombinedMarketDataProvider(final MarketDataProvider preferred, final MarketDataProvider fallBack) {
    _preferred = preferred;
    _fallBack = fallBack;
    _preferredListener = new CombinedMarketDataListener(this, _preferred);
    _fallBackListener = new CombinedMarketDataListener(this, _fallBack);
    _availabilityProvider = buildAvailabilityProvider();
  }

  @Override
  public void addListener(final MarketDataListener listener) {
    super.addListener(listener);
    checkListenerAttach();
  }

  @Override
  public void removeListener(final MarketDataListener listener) {
    super.removeListener(listener);
    checkListenerAttach();
  }

  private void checkListenerAttach() {
    //TODO: dedupe with CombinedMarketDataProvider
    synchronized (_listenerLock) {
      final boolean anyListeners = getListeners().size() > 0;
      if (anyListeners && !_listenerAttached) {
        _preferred.addListener(_preferredListener);
        _fallBack.addListener(_fallBackListener);
        _listenerAttached = true;
      } else if (!anyListeners && _listenerAttached) {
        _preferred.removeListener(_preferredListener);
        _fallBack.removeListener(_fallBackListener);
        _listenerAttached = false;
      }
    }
  }

  private class CombinedMarketDataListener implements MarketDataListener {
    private final CombinedMarketDataProvider _combinedMarketDataProvider;
    private final MarketDataProvider _provider;

    public CombinedMarketDataListener(final CombinedMarketDataProvider combinedMarketDataProvider, final MarketDataProvider provider) {
      _combinedMarketDataProvider = combinedMarketDataProvider;
      _provider = provider;
    }

    @Override
    public void subscriptionSucceeded(final ValueRequirement requirement) {
      final MarketDataProvider provider = _providerByRequirement.get(requirement);
      if (provider == _provider) {
        _combinedMarketDataProvider.subscriptionSucceeded(requirement);
      }
    }

    @Override
    public void subscriptionFailed(final ValueRequirement requirement, final String msg) {
      final MarketDataProvider provider = _providerByRequirement.get(requirement);
      if (provider == _provider) {
        _combinedMarketDataProvider.subscriptionFailed(requirement, msg);
      }
    }

    @Override
    public void subscriptionStopped(final ValueRequirement requirement) {
      final MarketDataProvider provider = _providerByRequirement.get(requirement);
      if (provider == _provider) {
        _combinedMarketDataProvider.subscriptionStopped(requirement);
      }
    }

    @Override
    public void valuesChanged(final Collection<ValueRequirement> requirements) {
      final Map<MarketDataProvider, Set<ValueRequirement>> grouped = groupByProvider(requirements);
      final Set<ValueRequirement> set = grouped.get(_provider);
      if (set != null) {
        _combinedMarketDataProvider.valuesChanged(set);
      }
    }
  }

  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return _availabilityProvider;
  }

  /**
   * Creates an availability provider that combines results from the two providers. The returned specification is either:
   * <ul>
   * <li>The value specification from the preferred provider
   * <li>The value specification from the non-preferred provider
   * <li>Null if either provider returned null
   * <li>{@link MarketDataNotSatisfiableException} being thrown if both providers do so
   * </ul>
   */
  private MarketDataAvailabilityProvider buildAvailabilityProvider() {
    return new MarketDataAvailabilityProvider() {

      private final MarketDataAvailabilityProvider _preferredProvider = _preferred.getAvailabilityProvider();
      private final MarketDataAvailabilityProvider _fallbackProvider = _fallBack.getAvailabilityProvider();

      @Override
      public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
        MarketDataNotSatisfiableException preferredMissing = null;
        try {
          final ValueSpecification preferred = _preferredProvider.getAvailability(targetSpec, target, desiredValue);
          if (preferred != null) {
            // preferred is available
            _providerByRequirement.put(desiredValue, _preferred);
            return preferred;
          }
        } catch (final MarketDataNotSatisfiableException e) {
          preferredMissing = e;
        }
        try {
          final ValueSpecification fallback = _fallbackProvider.getAvailability(targetSpec, target, desiredValue);
          if (fallback != null) {
            // fallback is available
            _providerByRequirement.put(desiredValue, _fallBack);
            return fallback;
          }
        } catch (final MarketDataNotSatisfiableException e) {
          if (preferredMissing != null) {
            // both are not available - use preferred
            _providerByRequirement.put(desiredValue, _preferred);
            throw preferredMissing;
          } else {
            // fallback is not available
            _providerByRequirement.put(desiredValue, _fallBack);
            throw e;
          }
        }
        // preferred is either not available or missing
        _providerByRequirement.put(desiredValue, _preferred);
        if (preferredMissing != null) {
          throw preferredMissing;
        } else {
          return null;
        }
      }

    };
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return new MarketDataPermissionProvider() {

      @Override
      public Set<ValueRequirement> checkMarketDataPermissions(final UserPrincipal user, final Set<ValueRequirement> requirements) {
        final Map<MarketDataProvider, Set<ValueRequirement>> reqsByProvider = groupByProvider(requirements);

        final Set<ValueRequirement> failures = Sets.newHashSet();
        for (final Entry<MarketDataProvider, Set<ValueRequirement>> entry : reqsByProvider.entrySet()) {
          final MarketDataPermissionProvider permissionProvider = entry.getKey().getPermissionProvider();
          final Set<ValueRequirement> failed = permissionProvider.checkMarketDataPermissions(user, entry.getValue());
          failures.addAll(failed);
        }
        return failures;
      }
    };
  }

  @Override
  public void subscribe(final ValueRequirement valueRequirement) {
    subscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(final Set<ValueRequirement> valueRequirements) {
    final Map<MarketDataProvider, Set<ValueRequirement>> reqsByProvider = groupByProvider(valueRequirements);
    for (final Entry<MarketDataProvider, Set<ValueRequirement>> entry : reqsByProvider.entrySet()) {
      entry.getKey().subscribe(entry.getValue());
    }
  }

  @Override
  public void unsubscribe(final ValueRequirement valueRequirement) {
    unsubscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void unsubscribe(final Set<ValueRequirement> valueRequirements) {
    final Map<MarketDataProvider, Set<ValueRequirement>> reqsByProvider = groupByProvider(valueRequirements);
    for (final Entry<MarketDataProvider, Set<ValueRequirement>> entry : reqsByProvider.entrySet()) {
      entry.getKey().unsubscribe(entry.getValue());
    }
  }

  @Override
  public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    final CombinedMarketDataSpecification combinedSpec = (CombinedMarketDataSpecification) marketDataSpec;
    final Map<MarketDataProvider, MarketDataSnapshot> snapByProvider = new HashMap<MarketDataProvider, MarketDataSnapshot>();
    snapByProvider.put(_preferred, _preferred.snapshot(combinedSpec.getPreferredSpecification()));
    snapByProvider.put(_fallBack, _fallBack.snapshot(combinedSpec.getFallbackSpecification()));
    final MarketDataSnapshot preferredSnap = snapByProvider.get(_preferred);
    return new CombinedMarketDataSnapshot(preferredSnap, snapByProvider, this);
  }

  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof CombinedMarketDataSpecification)) {
      return false;
    }
    final CombinedMarketDataSpecification combinedMarketDataSpec = (CombinedMarketDataSpecification) marketDataSpec;
    return _preferred.isCompatible(combinedMarketDataSpec.getPreferredSpecification())
        && _fallBack.isCompatible(combinedMarketDataSpec.getFallbackSpecification());
  }

  public Map<MarketDataProvider, Set<ValueRequirement>> groupByProvider(final Collection<ValueRequirement> requirements) {
    final Map<MarketDataProvider, Set<ValueRequirement>> reqsByProvider = new HashMap<MarketDataProvider, Set<ValueRequirement>>();
    for (final ValueRequirement valueRequirement : requirements) {
      final MarketDataProvider provider = getProvider(valueRequirement);
      if (provider == null) {
        throw new IllegalArgumentException("Don't know how to provide requirement " + valueRequirement);
      }
      Set<ValueRequirement> set = reqsByProvider.get(provider);
      if (set == null) {
        set = new HashSet<ValueRequirement>();
        reqsByProvider.put(provider, set);
      }
      set.add(valueRequirement);
    }
    return reqsByProvider;
  }

  public MarketDataProvider getProvider(final ValueRequirement valueRequirement) {
    MarketDataProvider provider = _providerByRequirement.get(valueRequirement);
    if (provider == null) {
      // TODO: [PLAT-3044] the NULL here is wrong
      getAvailabilityProvider().getAvailability(ComputationTargetSpecification.NULL, null, valueRequirement); //This populates the map
      provider = _providerByRequirement.get(valueRequirement);
    }
    return provider;
  }

}
