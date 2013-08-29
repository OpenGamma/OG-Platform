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
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Snapshot reader.
 */
public class MasterSnapshotReader implements SnapshotReader {

  private static final Logger s_logger = LoggerFactory.getLogger(SnapshotReader.class);

  private MarketDataSnapshotMaster _snapshotMaster;
  private StructuredMarketDataSnapshot _snapshot;
  private MarketDataSnapshotDocument _snapshotDocument;

  public MasterSnapshotReader(UniqueId uniqueId, MarketDataSnapshotMaster marketDataSnapshotMaster) {
    ArgumentChecker.notNull(marketDataSnapshotMaster, "marketDataSnapshotMaster");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    _snapshotMaster = marketDataSnapshotMaster;
    _snapshot = _snapshotMaster.get(uniqueId).getSnapshot();

/*

    StructuredMarketDataSnapshot snapshot02 = _snapshotMaster.get(UniqueId.parse("DbSnp~34668~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot10 = _snapshotMaster.get(UniqueId.parse("DbSnp~35404~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot11 = _snapshotMaster.get(UniqueId.parse("DbSnp~35403~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot12 = _snapshotMaster.get(UniqueId.parse("DbSnp~35402~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot13 = _snapshotMaster.get(UniqueId.parse("DbSnp~35393~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot14 = _snapshotMaster.get(UniqueId.parse("DbSnp~35394~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot15 = _snapshotMaster.get(UniqueId.parse("DbSnp~35395~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot16 = _snapshotMaster.get(UniqueId.parse("DbSnp~35396~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot17 = _snapshotMaster.get(UniqueId.parse("DbSnp~35397~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot18 = _snapshotMaster.get(UniqueId.parse("DbSnp~35398~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot19 = _snapshotMaster.get(UniqueId.parse("DbSnp~35399~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot20 = _snapshotMaster.get(UniqueId.parse("DbSnp~35400~0")).getSnapshot();
    StructuredMarketDataSnapshot snapshot21 = _snapshotMaster.get(UniqueId.parse("DbSnp~35401~0")).getSnapshot();


    _snapshot = snapshot02;
*/



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
    //To change body of implemented methods use File | Settings | File Templates.
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
