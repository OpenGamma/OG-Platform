/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;

/**
 * A class that writes securities and snapshot positions and trades to the OG masters
 */
public class MasterSnapshotWriter implements SnapshotWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(MasterSnapshotWriter.class);

  private final MarketDataSnapshotMaster _snapshotMaster;

  /**
   * Create a master snapshot writer
   * @param snapshotMaster The snapshot master to which to write the snapshot
   */

  public MasterSnapshotWriter(MarketDataSnapshotMaster snapshotMaster) {
    _snapshotMaster = snapshotMaster;
  }

  @Override
  public void close() {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
