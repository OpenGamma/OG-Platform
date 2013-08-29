/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.reader;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Snapshot reader.
 */
public class MasterSnapshotReader implements SnapshotReader {

  private static final Logger s_logger = LoggerFactory.getLogger(SnapshotReader.class);

  private MarketDataSnapshotMaster _snapshotMaster;
  private StructuredMarketDataSnapshot _snapshot;

  public MasterSnapshotReader(UniqueId uniqueId, MarketDataSnapshotMaster marketDataSnapshotMaster) {
    ArgumentChecker.notNull(marketDataSnapshotMaster, "marketDataSnapshotMaster");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    _snapshotMaster = marketDataSnapshotMaster;
    _snapshot = _snapshotMaster.get(uniqueId).getSnapshot();
  }

  @Override
  public Map<CurveKey, CurveSnapshot> readCurves() {
    return _snapshot.getCurves();
  }

  @Override
  public UnstructuredMarketDataSnapshot readGlobalValues() {
    return _snapshot.getGlobalValues();
  }

  @Override
  public Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> readVolatilitySurfaces() {
    return _snapshot.getVolatilitySurfaces();
  }

  @Override
  public Map<YieldCurveKey, YieldCurveSnapshot> readYieldCurves() {
    return _snapshot.getYieldCurves();
  }

  @Override
  public void close() {
    //nothing to do
  }

  @Override
  public String getName() {
    return _snapshot.getName();
  }

  @Override
  public String getBasisViewName() {
    return _snapshot.getBasisViewName();
  }
}
