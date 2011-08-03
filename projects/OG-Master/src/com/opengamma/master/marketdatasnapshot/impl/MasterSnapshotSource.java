/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.listener.MasterChangeListener;
import com.opengamma.master.listener.MasterChanged;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.tuple.Pair;

/**
 * A {@code MarketDataSnapshotSource} implemented using an underlying {@code MarketDataSnapshotMaster}.
 * <p>
 * The {@link MarketDataSnapshotSource} interface provides snapshots to the engine via a narrow API.
 * This class provides the source on top of a standard {@link MarketDataSnapshotMaster}.
 */
@PublicSPI
public class MasterSnapshotSource extends AbstractMasterSource<MarketDataSnapshotDocument, MarketDataSnapshotMaster>
    implements MarketDataSnapshotSource {

  private final ConcurrentHashMap<Pair<UniqueId, MarketDataSnapshotChangeListener>, MasterChangeListener> _registeredListeners =
      new ConcurrentHashMap<Pair<UniqueId, MarketDataSnapshotChangeListener>, MasterChangeListener>();

  /**
   * Creates an instance with an underlying master which does not override versions.
   * 
   * @param master  the master, not null
   */
  public MasterSnapshotSource(final MarketDataSnapshotMaster master) {
    super(master);
  }

  @Override
  public StructuredMarketDataSnapshot getSnapshot(UniqueId uniqueId) {
    return getMaster().get(uniqueId).getSnapshot();
  }

  @Override
  public void addChangeListener(final UniqueId uniqueId, final MarketDataSnapshotChangeListener listener) {
    MasterChangeListener masterListener = new MasterChangeListener() {

      @Override
      public void masterChanged(MasterChanged event) {
        UniqueId changedId = event.getAfterId();
        if (changedId != null && changedId.getScheme().equals(uniqueId.getScheme()) && changedId.getValue().equals(uniqueId.getValue())) {
          //TODO This is over cautious in the case of corrections to non latest versions
          if (uniqueId.getVersion() == null || changedId.getVersion().equals(uniqueId.getVersion())) {
            listener.snapshotChanged(uniqueId);
          }
        }
      }
    };
    _registeredListeners.put(Pair.of(uniqueId, listener), masterListener);
    getMaster().changeManager().addChangeListener(masterListener);
  }

  @Override
  public void removeChangeListener(UniqueId uniqueId, MarketDataSnapshotChangeListener listener) {
    MasterChangeListener masterListener = _registeredListeners.remove(Pair.of(uniqueId, listener));
    getMaster().changeManager().removeChangeListener(masterListener);
  }

}
