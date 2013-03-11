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
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.UnionMarketDataAvailability;
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

  private final UniqueId _snapshotId;
  private final CopyOnWriteArraySet<ValueSpecification> _listeningValueSpecifications = new CopyOnWriteArraySet<ValueSpecification>();
  private final MarketDataSnapshotSource _snapshotSource;
  private final MarketDataPermissionProvider _permissionProvider;
  private final MarketDataSnapshotChangeListener _snapshotSourceChangeListener;
  private final Object _listenerLock = new Object();

  private MarketDataAvailabilityProvider _baseMarketDataAvailabilityProvider;

  public UserMarketDataProvider(final MarketDataSnapshotSource snapshotSource, final UniqueId snapshotId) {
    ArgumentChecker.notNull(snapshotSource, "snapshotSource");
    ArgumentChecker.notNull(snapshotId, "snapshotId");
    _snapshotSource = snapshotSource;
    _snapshotId = snapshotId;
    // Assume no permission issues
    _permissionProvider = new PermissiveMarketDataPermissionProvider();
    _snapshotSourceChangeListener = new MarketDataSnapshotChangeListener() {
      @Override
      public void objectChanged(final ObjectId oid) {
        valuesChanged(_listeningValueSpecifications);
      }
    };
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
    subscriptionSucceeded(valueSpecifications);
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
    final UserMarketDataSnapshot snapshot = snapshot();
    snapshot.init();
    if (getBaseMarketDataAvailabilityProvider() == null) {
      return snapshot.getAvailabilityProvider();
    } else {
      // [PLAT-1459] 2011-10-03 -- missing values in the snapshot will prevent the dep graph from building even though
      // it builds in the live case where the availability provider is more optimistic. Using a union of the two works
      // around this problem.
      return new UnionMarketDataAvailability.Provider(Arrays.asList(getBaseMarketDataAvailabilityProvider(), snapshot.getAvailabilityProvider()));
    }
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
  public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    return snapshot();
  }

  //-------------------------------------------------------------------------
  private UserMarketDataSnapshot snapshot() {
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

  public void setBaseMarketDataAvailabilityProvider(final MarketDataAvailabilityProvider baseMarketDataAvailabilityProvider) {
    _baseMarketDataAvailabilityProvider = baseMarketDataAvailabilityProvider;
  }

}
