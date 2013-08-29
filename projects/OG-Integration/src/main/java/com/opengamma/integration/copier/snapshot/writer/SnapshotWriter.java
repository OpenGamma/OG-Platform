/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.snapshot.writer;

import java.util.Map;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;

/**
 * Interface for a snapshot writer, which is able to write positions and securities, and manipulate the snapshot's
 * tree structure.
 */
public interface SnapshotWriter {

  void flush();

  void writeCurves(Map<CurveKey, CurveSnapshot> curves);

  void writeGlobalValues(UnstructuredMarketDataSnapshot globalValues);

  void writeVolatilitySurface(Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> volatilitySurface);

  void writeYieldCurves(Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves);

  void writeName(String name);

  void writeBasisViewName(String basisName);
  
  void close();
}
