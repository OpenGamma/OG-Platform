/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractMasterSource;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.PublicSPI;

/**
 * A {@code MarketDataSnapshotSource} implemented using an underlying {@code MarketDataSnapshotMaster}.
 * <p>
 * The {@link MarketDataSnapshotSource} interface provides snapshots to the engine via a narrow API.
 * This class provides the source on top of a standard {@link MarketDataSnapshotMaster}.
 */
@PublicSPI
public class MasterSnapshotSource extends AbstractMasterSource<MarketDataSnapshotDocument, MarketDataSnapshotMaster>
    implements MarketDataSnapshotSource {

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

}
