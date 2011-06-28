/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.StringUtils;

import com.opengamma.engine.marketdata.PendingCombinedMarketDataSubscription.PendingCombinedSubscriptionState;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSnapshotSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Combines a {@link MarketDataProvider} with another which provides overrides.
 */
public class MarketDataProviderWithOverride implements MarketDataProvider {
  
  private final MarketDataProvider _underlying;
  private final MarketDataProvider _override;
  private final Set<MarketDataListener> _listeners = new CopyOnWriteArraySet<MarketDataListener>();
  private final Map<ValueRequirement, PendingCombinedMarketDataSubscription> _pendingSubscriptions = new ConcurrentHashMap<ValueRequirement, PendingCombinedMarketDataSubscription>();

  private class CombinedLiveDataSnapshotListener implements MarketDataListener {
    
    private final MarketDataProvider _provider;
    
    public CombinedLiveDataSnapshotListener(MarketDataProvider provider) {
      _provider = provider;
    }

    @Override
    public void subscriptionFailed(ValueRequirement requirement, String msg) {
      PendingCombinedMarketDataSubscription pendingSubscription = _pendingSubscriptions.get(requirement);
      if (pendingSubscription == null) {
        return;
      }
      processState(pendingSubscription.subscriptionFailed(_provider, msg), pendingSubscription, requirement);
    }

    @Override
    public void subscriptionStopped(ValueRequirement requirement) {
      MarketDataProviderWithOverride.this.subscriptionStopped(requirement);
    }

    @Override
    public void subscriptionSucceeded(ValueRequirement requirement) {
      PendingCombinedMarketDataSubscription pendingSubscription = _pendingSubscriptions.get(requirement);
      if (pendingSubscription == null) {
        return;
      }
      processState(pendingSubscription.subscriptionSucceeded(_provider), pendingSubscription, requirement);
    }

    @Override
    public void valueChanged(ValueRequirement requirement) {
      MarketDataProviderWithOverride.this.valueChanged(requirement);
    }
    
    private void processState(PendingCombinedSubscriptionState state, PendingCombinedMarketDataSubscription pendingSubscription, ValueRequirement requirement) {
      switch (state) {
        case FAILURE:
          String msg = StringUtils.join(pendingSubscription.getFailureMessages(), ", ");
          MarketDataProviderWithOverride.this.subscriptionFailed(requirement, msg);
          break;
        case SUCCESS:
          MarketDataProviderWithOverride.this.subscriptionSucceeded(requirement);
          break;
      }
    }
    
  }
  
  /**
   * Constructs an instance using the specified providers.
   * 
   * @param underlying  the underlying, or default, provider, not {@code null}
   * @param override  the override provider, not {@code null}
   */
  public MarketDataProviderWithOverride(MarketDataProvider underlying, MarketDataProvider override) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(override, "override");
    _underlying = underlying;
    _underlying.addListener(new CombinedLiveDataSnapshotListener(_underlying));
    _override = override;
    _override.addListener(new CombinedLiveDataSnapshotListener(_override));
  }
  
  //--------------------------------------------------------------------------
  @Override
  public void addListener(MarketDataListener listener) {
    _listeners.add(listener);
  }
  
  @Override
  public void removeListener(MarketDataListener listener) {
    _listeners.remove(listener);
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    subscribe(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    for (ValueRequirement requirement : valueRequirements) {
      _pendingSubscriptions.put(requirement, new PendingCombinedMarketDataSubscription(Arrays.asList(_underlying, _override)));
    }
    _underlying.subscribe(user, valueRequirements);
    _override.subscribe(user, valueRequirements);
  }
  
  @Override
  public void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    unsubscribe(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    _underlying.unsubscribe(user, valueRequirements);
    _override.unsubscribe(user, valueRequirements);
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    // Assume overrides only valid if data available from underlying
    return _underlying.getAvailabilityProvider();
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    // Assume overrides not permissioned
    return _underlying.getPermissionProvider();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(MarketDataSnapshotSpecification snapshotSpec) {
    return _underlying.isCompatible(snapshotSpec) && _override.isCompatible(snapshotSpec);
  }
  
  @Override
  public MarketDataSnapshot snapshot(MarketDataSnapshotSpecification snapshotSpec) {
    MarketDataSnapshot underlyingSnapshot = _underlying.snapshot(snapshotSpec);
    MarketDataSnapshot overrideSnapshot = _override.snapshot(snapshotSpec);    
    return new MarketDataSnapshotWithOverride(underlyingSnapshot, overrideSnapshot);
  }
    
  //--------------------------------------------------------------------------
  private void subscriptionSucceeded(ValueRequirement requirement) {
    for (MarketDataListener listener : _listeners) {
      listener.subscriptionSucceeded(requirement);
    }
  }
  
  private void subscriptionFailed(ValueRequirement requirement, String msg) {
    for (MarketDataListener listener : _listeners) {
      listener.subscriptionFailed(requirement, msg);
    }
  }
  
  private void subscriptionStopped(ValueRequirement requirement) {
    for (MarketDataListener listener : _listeners) {
      listener.subscriptionStopped(requirement);
    }
  }
  
  private void valueChanged(ValueRequirement requirement) {
    for (MarketDataListener listener : _listeners) {
      listener.valueChanged(requirement);
    }
  }

}
