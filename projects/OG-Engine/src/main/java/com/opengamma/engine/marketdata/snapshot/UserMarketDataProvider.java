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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.ExternalIdBundleLookup;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataSnapshotAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.UnionMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link MarketDataProvider} which sources its data from user-generated snapshots from a
 * {@link MarketDataSnapshotSource}.
 */
public class UserMarketDataProvider extends AbstractMarketDataProvider {

  private final MarketDataSnapshotSource _snapshotSource;
  private final UniqueId _snapshotId;
  private final ExternalIdBundleLookup _identifierLookup;
  
  private final CopyOnWriteArraySet<ValueRequirement> _listeningValueRequirements = new CopyOnWriteArraySet<ValueRequirement>();
  private final MarketDataPermissionProvider _permissionProvider;
  private final MarketDataSnapshotChangeListener _snapshotSourceChangeListener;
  private final Object _listenerLock = new Object();
  private final Object _initSnapshotLock = new Object();
  
  private MarketDataSnapshot _snapshot;
  
  private MarketDataAvailabilityProvider _baseMarketDataAvailabilityProvider;

  public UserMarketDataProvider(MarketDataSnapshotSource snapshotSource, UniqueId snapshotId, ExternalIdBundleLookup identifierLookup) {
    ArgumentChecker.notNull(snapshotSource, "snapshotSource");
    ArgumentChecker.notNull(snapshotId, "snapshotId");
    ArgumentChecker.notNull(identifierLookup, "identifierLookup");
    _snapshotSource = snapshotSource;
    _snapshotId = snapshotId;
    _identifierLookup = identifierLookup;
    // Assume no permission issues
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
    _snapshotSourceChangeListener = new MarketDataSnapshotChangeListener() {
      @Override
      public void objectChanged(ObjectId oid) {
        if (!oid.equals(getSnapshotId().getObjectId())) {
          return;
        }
        synchronized (_initSnapshotLock) {
          _snapshot = null;          
        }
        valuesChanged(_listeningValueRequirements);
      }
    };
  }

  private MarketDataSnapshot getSnapshot() {
    MarketDataSnapshot snapshot = _snapshot;
    if (snapshot != null) {
      return snapshot;
    }
    synchronized (_initSnapshotLock) {
      if (_snapshot == null) {
        StructuredMarketDataSnapshot structuredSnapshot = getSnapshotSource().get(getSnapshotId());
        _snapshot = new UserMarketDataSnapshot(structuredSnapshot, getIdentifierLookup());
      }
      return _snapshot;
    }
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
  public void subscribe(ValueRequirement valueRequirement) {
    subscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void subscribe(Set<ValueRequirement> valueRequirements) {
    _listeningValueRequirements.addAll(valueRequirements);
    subscriptionSucceeded(valueRequirements);
  }
  
  @Override
  public void unsubscribe(ValueRequirement valueRequirement) {
    unsubscribe(Collections.singleton(valueRequirement));
  }

  @Override
  public void unsubscribe(Set<ValueRequirement> valueRequirements) {
    // TODO
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    MarketDataSnapshotAvailabilityProvider snapshotAvailabilityProvider = new MarketDataSnapshotAvailabilityProvider(_snapshot);
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
    if (!isCompatible(marketDataSpec)) {
      throw new OpenGammaRuntimeException("Market data specification " + marketDataSpec + " is incompatible with " +
          UserMarketDataProvider.class.getSimpleName() + " for snapshot " + getSnapshotId());
    }
    return getSnapshot();
  }

  //-------------------------------------------------------------------------
  private MarketDataSnapshot snapshot() {
    return getSnapshot();
  }
  
  private UniqueId getSnapshotId() {
    return _snapshotId;
  }
  
  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }
  
  private ExternalIdBundleLookup getIdentifierLookup() {
    return _identifierLookup;
  }

  //-------------------------------------------------------------------------
  public MarketDataAvailabilityProvider getBaseMarketDataAvailabilityProvider() {
    return _baseMarketDataAvailabilityProvider;
  }
  
  public void setBaseMarketDataAvailabilityProvider(MarketDataAvailabilityProvider baseMarketDataAvailabilityProvider) {
    _baseMarketDataAvailabilityProvider = baseMarketDataAvailabilityProvider;
  }
  
}
