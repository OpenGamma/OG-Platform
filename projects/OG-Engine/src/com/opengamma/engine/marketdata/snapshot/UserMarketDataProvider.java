/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataSnapshotAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.UnionMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.permission.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link MarketDataProvider} which sources its data from user-generated snapshots from a
 * {@link MarketDataSnapshotSource}.
 */
public class UserMarketDataProvider extends AbstractMarketDataProvider {

  private final UniqueId _snapshotId;
  private final CopyOnWriteArraySet<ValueRequirement> _listeningValueRequirements = new CopyOnWriteArraySet<ValueRequirement>();
  private final MarketDataSnapshotSource _snapshotSource;
  private final MarketDataPermissionProvider _permissionProvider;
  private final MarketDataSnapshotChangeListener _snapshotSourceChangeListener;
  private final Object _listenerLock = new Object();
  
  private MarketDataAvailabilityProvider _baseMarketDataAvailabilityProvider;

  public UserMarketDataProvider(MarketDataSnapshotSource snapshotSource, UniqueId snapshotId) {
    ArgumentChecker.notNull(snapshotSource, "snapshotSource");
    ArgumentChecker.notNull(snapshotId, "snapshotId");
    _snapshotSource = snapshotSource;
    _snapshotId = snapshotId;
    // Assume no permission issues
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
    _snapshotSourceChangeListener = new MarketDataSnapshotChangeListener() {
      @Override
      public void snapshotChanged(UniqueId uid) {
        valuesChanged(_listeningValueRequirements);
      }
    };
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void addListener(MarketDataListener listener) {
    synchronized (_listenerLock) {
      if (getListeners().size() == 0) {
        _snapshotSource.addChangeListener(_snapshotId, _snapshotSourceChangeListener);
      }
      super.addListener(listener);
    }
  }

  @Override
  public void removeListener(MarketDataListener listener) {
    synchronized (_listenerLock) {
      super.removeListener(listener);
      if (getListeners().size() == 0) {
        _snapshotSource.removeChangeListener(_snapshotId, _snapshotSourceChangeListener);
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    subscribe(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    _listeningValueRequirements.addAll(valueRequirements);
    subscriptionSucceeded(valueRequirements);
  }
  
  @Override
  public void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement) {
    unsubscribe(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    // TODO
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    MarketDataSnapshot snapshot = snapshot();
    snapshot.init();
    MarketDataSnapshotAvailabilityProvider snapshotAvailabilityProvider = new MarketDataSnapshotAvailabilityProvider(snapshot);
    
    if (getBaseMarketDataAvailabilityProvider() == null) {
      return snapshotAvailabilityProvider;
    } else {
      // [PLAT-1459] 2011-10-03 -- missing values in the snapshot will prevent the dep graph from building even though
      // it builds in the live case where the availability provider is more optimistic. Using a union of the two works
      // around this problem.
      return new UnionMarketDataAvailabilityProvider(Arrays.asList(getBaseMarketDataAvailabilityProvider(), snapshotAvailabilityProvider));      
    }
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionProvider;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof UserMarketDataSpecification)) {
      return false;
    }
    UserMarketDataSpecification userMarketDataSpec = (UserMarketDataSpecification) marketDataSpec;
    return getSnapshotId().equals(userMarketDataSpec.getUserSnapshotId());
  }

  @Override
  public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    return snapshot();
  }

  //-------------------------------------------------------------------------
  private MarketDataSnapshot snapshot() {
    return new UserMarketDataSnapshot(getSnapshotSource(), getSnapshotId());
  }
  
  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }
  
  private UniqueId getSnapshotId() {
    return _snapshotId;
  }

  //-------------------------------------------------------------------------
  public MarketDataAvailabilityProvider getBaseMarketDataAvailabilityProvider() {
    return _baseMarketDataAvailabilityProvider;
  }
  
  public void setBaseMarketDataAvailabilityProvider(MarketDataAvailabilityProvider baseMarketDataAvailabilityProvider) {
    _baseMarketDataAvailabilityProvider = baseMarketDataAvailabilityProvider;
  }
  
}
