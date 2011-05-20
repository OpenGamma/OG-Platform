package com.opengamma.core.marketdatasnapshot;

import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public interface MarketDataSnapshotChangeListener {

  /**
   * Called if the uid now refers to a different snapshot
   */
  void snapshotChanged(UniqueIdentifier uid);
}
