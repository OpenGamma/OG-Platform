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
 * Abstract snapshot loader class that merely specifies the ability to write imported trades/positions to a
 * SnapshotWriter
 * (This tight linkage between reader and writer might have to change)
 */
public abstract interface SnapshotReader {

  Map<CurveKey, CurveSnapshot> readCurves();

  UnstructuredMarketDataSnapshot readGlobalValues();

  Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> readVolatilitySurfaces();

  Map<YieldCurveKey, YieldCurveSnapshot> readYieldCurves();

  /**
   * Get the current snapshot path.
   *
   * @return the current node
   */
  String[] getCurrentPath();

  void close();

  /**
   * Read the name of the snapshot from the source.
   *
   * @return the snapshot name.
   */
  String getName();

  String getBasisViewName();
}
