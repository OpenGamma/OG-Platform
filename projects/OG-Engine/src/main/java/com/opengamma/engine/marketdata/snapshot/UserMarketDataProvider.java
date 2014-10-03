/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link MarketDataProvider} which sources its data from user-generated snapshots from a {@link MarketDataSnapshotSource}.
 */
public class UserMarketDataProvider extends AbstractMarketDataProvider {

  private final MarketDataSnapshotSource _snapshotSource;
  private final UniqueId _snapshotId;
  private final CopyOnWriteArraySet<ValueSpecification> _listeningValueSpecifications = new CopyOnWriteArraySet<ValueSpecification>();
  private final MarketDataPermissionProvider _permissionProvider;
  private final MarketDataSnapshotChangeListener _snapshotSourceChangeListener;
  private final Object _listenerLock = new Object();
  private final Object _initSnapshotLock = new Object();
  private volatile UserMarketDataSnapshot _snapshot;

  public UserMarketDataProvider(final MarketDataSnapshotSource snapshotSource, final UniqueId snapshotId) {
    ArgumentChecker.notNull(snapshotSource, "snapshotSource");
    ArgumentChecker.notNull(snapshotId, "snapshotId");
    _snapshotSource = snapshotSource;
    _snapshotId = snapshotId;
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
        valuesChanged(_listeningValueSpecifications);
      }
    };
  }

  private UserMarketDataSnapshot getSnapshot() {
    UserMarketDataSnapshot snapshot = _snapshot;
    if (snapshot != null) {
      return snapshot;
    }
    synchronized (_initSnapshotLock) {
      if (_snapshot == null) {
        StructuredMarketDataSnapshot structuredSnapshot = (StructuredMarketDataSnapshot) getSnapshotSource().get(getSnapshotId());
        snapshot = new UserMarketDataSnapshot(structuredSnapshot);
        snapshot.init();
        _snapshot = snapshot;
      }
      return _snapshot;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void addListener(final MarketDataListener listener) {
    synchronized (_listenerLock) {
      if (getListeners().size() == 0) {
        _snapshotSource.addChangeListener(_snapshotId, _snapshotSourceChangeListener);
      }
      super.addListener(listener);
    }
  }

  @Override
  public void removeListener(final MarketDataListener listener) {
    synchronized (_listenerLock) {
      super.removeListener(listener);
      if (getListeners().size() == 0) {
        _snapshotSource.removeChangeListener(_snapshotId, _snapshotSourceChangeListener);
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscribe(final ValueSpecification valueSpecification) {
    subscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void subscribe(final Set<ValueSpecification> valueSpecifications) {
    _listeningValueSpecifications.addAll(valueSpecifications);
    subscriptionsSucceeded(valueSpecifications);
  }

  @Override
  public void unsubscribe(final ValueSpecification valueSpecification) {
    unsubscribe(Collections.singleton(valueSpecification));
  }

  @Override
  public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    // TODO
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
    return getSnapshot().getAvailabilityProvider();
  }

  @Override
  public MarketDataPermissionProvider getPermissionProvider() {
    return _permissionProvider;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof UserMarketDataSpecification)) {
      return false;
    }
    final UserMarketDataSpecification userMarketDataSpec = (UserMarketDataSpecification) marketDataSpec;
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

  private UniqueId getSnapshotId() {
    return _snapshotId;
  }

  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }

}
