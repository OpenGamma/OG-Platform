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

import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.spec.CombinedMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;

/**
 * Implementation of {@link MarketDataProvider} which sources its data from on of two {@link MarketDataProvider}s, 
 *  choosing based on the availability of data.
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

  public CombinedMarketDataProvider(MarketDataProvider preferred, MarketDataProvider fallBack) {
    _preferred = preferred;
    _fallBack = fallBack;
    _preferredListener = new CombinedMarketDataListener(this, _preferred);
    _fallBackListener = new CombinedMarketDataListener(this, _fallBack);
    _availabilityProvider = buildAvailabilityProvider();
  }
  
  @Override
  public void addListener(MarketDataListener listener) {
    super.addListener(listener);
    checkListenerAttach();
  }

  @Override
  public void removeListener(MarketDataListener listener) {
    super.removeListener(listener);
    checkListenerAttach();
  }

  private void checkListenerAttach() { 
    //TODO: dedupe with CombinedMarketDataProvider
    synchronized (_listenerLock) {
      boolean anyListeners = getListeners().size() > 0;
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

    public CombinedMarketDataListener(CombinedMarketDataProvider combinedMarketDataProvider, MarketDataProvider provider) {
      _combinedMarketDataProvider = combinedMarketDataProvider;
      _provider = provider;
    }

    @Override
    public void subscriptionSucceeded(ValueRequirement requirement) {
      MarketDataProvider provider = _providerByRequirement.get(requirement);
      if (provider == _provider) {
        _combinedMarketDataProvider.subscriptionSucceeded(requirement);
      }
    }

    @Override
    public void subscriptionFailed(ValueRequirement requirement, String msg) {
      MarketDataProvider provider = _providerByRequirement.get(requirement);
      if (provider == _provider) {
        _combinedMarketDataProvider.subscriptionFailed(requirement, msg);
      }
    }

    @Override
    public void subscriptionStopped(ValueRequirement requirement) {
      MarketDataProvider provider = _providerByRequirement.get(requirement);
      if (provider == _provider) {
        _combinedMarketDataProvider.subscriptionStopped(requirement);
      }
    }

    @Override
    public void valuesChanged(Collection<ValueRequirement> requirements) {
      Map<MarketDataProvider, Set<ValueRequirement>> grouped = groupByProvider(requirements);
      Set<ValueRequirement> set = grouped.get(_provider);
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
   * Creates an availability provider that combines results from the two providers.
   * The availability status is:
   * <ul> 
   *  <li>AVAILABLE if either provider reports AVAILABLE
   *  <li>MISSING if neither provider reports AVAILABLE, and at least one reports MISSING
   *  <li>NOT_AVAILABLE if both providers report NOT_AVAILABLE
   * </ul>
   */
  private MarketDataAvailabilityProvider buildAvailabilityProvider() {
    return new MarketDataAvailabilityProvider() {

      private final MarketDataAvailabilityProvider _preferredProvider = _preferred.getAvailabilityProvider();
      private final MarketDataAvailabilityProvider _fallbackProvider = _fallBack.getAvailabilityProvider();

      @Override
      public MarketDataAvailability getAvailability(final ValueRequirement requirement) {
        final MarketDataAvailability preferred = _preferredProvider.getAvailability(requirement);
        if (preferred == MarketDataAvailability.AVAILABLE) {
          // preferred is available
          _providerByRequirement.put(requirement, _preferred);
          return preferred;
        }
        final MarketDataAvailability fallback = _fallbackProvider.getAvailability(requirement);
        if (fallback != MarketDataAvailability.NOT_AVAILABLE) {
          // fallback is either available or missing
          _providerByRequirement.put(requirement, _fallBack);
          return fallback;
        }
        // preferred is either not available or missing
        _providerByRequirement.put(requirement, _preferred);
        return preferred;
      }

    };
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return new MarketDataPermissionProvider() {
      
      @Override
      public boolean canAccessMarketData(UserPrincipal user, Set<ValueRequirement> requirements) {
        Map<MarketDataProvider, Set<ValueRequirement>> reqsByProvider = groupByProvider(requirements);
        
        for (Entry<MarketDataProvider, Set<ValueRequirement>> entry : reqsByProvider.entrySet()) {
          if (!entry.getKey().getPermissionProvider().canAccessMarketData(user, entry.getValue())) {
            return false;
          }
        }
        return true;
      }
    };
  }

  @Override
  public void subscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    subscribe(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    Map<MarketDataProvider, Set<ValueRequirement>> reqsByProvider = groupByProvider(valueRequirements);
    for (Entry<MarketDataProvider, Set<ValueRequirement>> entry : reqsByProvider.entrySet()) {
      entry.getKey().subscribe(user, entry.getValue());
    }
  }

  @Override
  public void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    unsubscribe(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    Map<MarketDataProvider, Set<ValueRequirement>> reqsByProvider = groupByProvider(valueRequirements);
    for (Entry<MarketDataProvider, Set<ValueRequirement>> entry : reqsByProvider.entrySet()) {
      entry.getKey().unsubscribe(user, entry.getValue());
    }
  }
  
  @Override
  public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    CombinedMarketDataSpecification combinedSpec = (CombinedMarketDataSpecification) marketDataSpec;
    Map<MarketDataProvider, MarketDataSnapshot> snapByProvider = new HashMap<MarketDataProvider, MarketDataSnapshot>();
    snapByProvider.put(_preferred, _preferred.snapshot(combinedSpec.getPrefferedSpecification()));
    snapByProvider.put(_fallBack, _fallBack.snapshot(combinedSpec.getFallbackSpecification()));
    MarketDataSnapshot preferredSnap = snapByProvider.get(_preferred);
    return new CombinedMarketDataSnapshot(preferredSnap, snapByProvider, this);
  }
  
  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof CombinedMarketDataSpecification)) {
      return false;
    }
    CombinedMarketDataSpecification combinedMarketDataSpec = (CombinedMarketDataSpecification) marketDataSpec;
    return _preferred.isCompatible(combinedMarketDataSpec.getPrefferedSpecification())
        && _fallBack.isCompatible(combinedMarketDataSpec.getFallbackSpecification());
  }

  public Map<MarketDataProvider, Set<ValueRequirement>> groupByProvider(Collection<ValueRequirement> requirements) {
    Map<MarketDataProvider, Set<ValueRequirement>> reqsByProvider = new HashMap<MarketDataProvider, Set<ValueRequirement>>();
    for (ValueRequirement valueRequirement : requirements) {
      MarketDataProvider provider = getProvider(valueRequirement);
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

  public MarketDataProvider getProvider(ValueRequirement valueRequirement) {
    MarketDataProvider provider = _providerByRequirement.get(valueRequirement);
    if (provider == null) {
      getAvailabilityProvider().getAvailability(valueRequirement); //This populates the map
      provider = _providerByRequirement.get(valueRequirement);
    }
    return provider;
  }

}
