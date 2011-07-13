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

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.spec.CombinedMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;

/**
 * Implementation of {@link MarketDataProvider} which sources its data from on of two {@link MarketDataProvider}s, 
 *  choosing based on the availability of data
 */
public class CombinedMarketDataProvider extends AbstractMarketDataProvider {

  private final MarketDataProvider _preffered;
  private final MarketDataProvider _fallBack;
  
  private final CombinedMarketDataListener _prefferedListener;
  private final CombinedMarketDataListener _fallBackListener;
  
  private final Map<ValueRequirement, MarketDataProvider> _providerByRequirement = new HashMap<ValueRequirement, MarketDataProvider>();

  private final Object _listenerLock = new Object();
  private boolean _listenerAttached;
  
  public CombinedMarketDataProvider(MarketDataProvider preffered, MarketDataProvider fallBack) {
    _preffered = preffered;
    _fallBack = fallBack;
    
    _prefferedListener = new CombinedMarketDataListener(this, _preffered);
    _fallBackListener = new CombinedMarketDataListener(this, _fallBack);
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
    synchronized (_listenerLock) {
      boolean anyListeners = getListeners().size() > 0;

      if (anyListeners && !_listenerAttached) {
        _preffered.addListener(_prefferedListener);
        _fallBack.addListener(_fallBackListener);
        _listenerAttached = true;
      } else if (!anyListeners && _listenerAttached) {
        _preffered.removeListener(_prefferedListener);
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
    final MarketDataAvailabilityProvider prefProvider = _preffered.getAvailabilityProvider();
    final MarketDataAvailabilityProvider fallbackProvider = _fallBack.getAvailabilityProvider();
    return new MarketDataAvailabilityProvider() {

      @Override
      public boolean isAvailable(ValueRequirement requirement) {
        if (prefProvider.isAvailable(requirement)) {
          _providerByRequirement.put(requirement, _preffered);
          return true;
        } else if (fallbackProvider.isAvailable(requirement)) {
          _providerByRequirement.put(requirement, _fallBack);
          return true;
        } else {
          return false;
        }
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
    snapByProvider.put(_preffered, _preffered.snapshot(combinedSpec.getPrefferedSpecification()));
    snapByProvider.put(_fallBack, _fallBack.snapshot(combinedSpec.getFallbackSpecification()));
    
    MarketDataSnapshot prefferedSnap = snapByProvider.get(_preffered);
    
    return new CombinedMarketDataSnapshot(prefferedSnap, snapByProvider, this);
  }
  
  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof CombinedMarketDataSpecification)) {
      return false;
    }
    CombinedMarketDataSpecification combinedMarketDataSpec = (CombinedMarketDataSpecification) marketDataSpec;
    return _preffered.isCompatible(combinedMarketDataSpec.getPrefferedSpecification())
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
      getAvailabilityProvider().isAvailable(valueRequirement);
      provider = _providerByRequirement.get(valueRequirement);
    }
    return provider;
  }
}
