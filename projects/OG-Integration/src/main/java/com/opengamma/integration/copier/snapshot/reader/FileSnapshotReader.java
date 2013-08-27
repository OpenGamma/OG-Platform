/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.snapshot.reader;

import java.util.Map;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;

/**
 * Reads a snapshot from an imported file
 */
public class FileSnapshotReader implements SnapshotReader {

  public FileSnapshotReader(String filename) {
    //To change body of created methods use File | Settings | File Templates.
  }

  @Override
  public Map<CurveKey, CurveSnapshot> readCurves() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public UnstructuredMarketDataSnapshot readGlobalValues() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> readVolatilitySurfaces() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Map<YieldCurveKey, YieldCurveSnapshot> readYieldCurves() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String[] getCurrentPath() {
    return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void close() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getBasisViewName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
