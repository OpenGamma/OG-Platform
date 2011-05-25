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
import com.opengamma.id.UniqueIdentifier;
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

  private final ConcurrentHashMap<Pair<UniqueIdentifier, MarketDataSnapshotChangeListener>, MasterChangeListener> _registeredListeners = 
    new  ConcurrentHashMap<Pair<UniqueIdentifier, MarketDataSnapshotChangeListener>, MasterChangeListener>();
  
  /**
   * Creates an instance with an underlying master which does not override versions.
   * 
   * @param master  the master, not null
   */
  public MasterSnapshotSource(final MarketDataSnapshotMaster master) {
    super(master);
  }

  @Override
  public StructuredMarketDataSnapshot getSnapshot(UniqueIdentifier uid) {
    return getMaster().get(uid).getSnapshot();
  }

  @Override
  public void addChangeListener(final UniqueIdentifier uid, final MarketDataSnapshotChangeListener listener) {
    MasterChangeListener masterListener = new MasterChangeListener() {

      @Override
      public void masterChanged(MasterChanged event) {
        UniqueIdentifier changedId = event.getAfterId();
        if (changedId != null && changedId.getScheme().equals(uid.getScheme()) && changedId.getValue().equals(uid.getValue())) {
          //TODO This is over cautious in the case of corrections to non latest versions
          if (uid.getVersion() == null || changedId.getVersion().equals(uid.getVersion())) {
            listener.snapshotChanged(uid);
          }
        }
      }
    };
    _registeredListeners.put(Pair.of(uid, listener), masterListener);
    getMaster().changeManager().addChangeListener(masterListener);
  }

  @Override
  public void removeChangeListener(UniqueIdentifier uid, MarketDataSnapshotChangeListener listener) {
    MasterChangeListener masterListener = _registeredListeners.remove(Pair.of(uid, listener));
    getMaster().changeManager().removeChangeListener(masterListener);
  }

}
