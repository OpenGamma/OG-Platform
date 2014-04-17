/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A {@code MarketDataSnapshotSource} implemented using an underlying {@code MarketDataSnapshotMaster}.
 * <p>
 * The {@link MarketDataSnapshotSource} interface provides snapshots to the engine via a narrow API.
 * This class provides the source on top of a standard {@link MarketDataSnapshotMaster}.
 */
@PublicSPI
public class MasterSnapshotSource extends AbstractMasterSource<StructuredMarketDataSnapshot, MarketDataSnapshotDocument, MarketDataSnapshotMaster> implements MarketDataSnapshotSource {

  /**
   * The listeners.
   */
  private final ConcurrentHashMap<Pair<UniqueId, MarketDataSnapshotChangeListener>, ChangeListener> _registeredListeners = 
    new ConcurrentHashMap<Pair<UniqueId, MarketDataSnapshotChangeListener>, ChangeListener>();

  /**
   * Creates an instance with an underlying master which does not override versions.
   *
   * @param master  the master, not null
   */
  public MasterSnapshotSource(final MarketDataSnapshotMaster master) {
    super(master);
  }

  //-------------------------------------------------------------------------
  @Override
  public void addChangeListener(final UniqueId uniqueId, final MarketDataSnapshotChangeListener listener) {
    ChangeListener changeListener = new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        ObjectId changedId = event.getObjectId();
        if (changedId != null && changedId.getScheme().equals(uniqueId.getScheme()) && changedId.getValue().equals(uniqueId.getValue())) {
          //TODO This is over cautious in the case of corrections to non latest versions
          listener.objectChanged(uniqueId.getObjectId());
        }
      }
    };
    _registeredListeners.put(Pairs.of(uniqueId, listener), changeListener);
    getMaster().changeManager().addChangeListener(changeListener);
  }

  @Override
  public void removeChangeListener(UniqueId uid, MarketDataSnapshotChangeListener listener) {
    ChangeListener changeListener = _registeredListeners.remove(Pairs.of(uid, listener));
    getMaster().changeManager().removeChangeListener(changeListener);
  }

}
