/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.writer;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
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
  public void flush() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeCurves(Map<CurveKey, CurveSnapshot> curves) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeGlobalValues(UnstructuredMarketDataSnapshot globalValues) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeVoliatilitySurface(Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> volatilitySurface) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeYieldCurves(Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeName(String name) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void writeBasisViewName(String basisName) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void close() {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
