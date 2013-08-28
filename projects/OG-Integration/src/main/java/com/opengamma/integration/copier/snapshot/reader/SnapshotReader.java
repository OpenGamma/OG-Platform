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
 * Abstract snapshot reader with methods that provide getters for the specific elements a snapshot
 * note that VolatilityCubes are not present.
 */
public abstract interface SnapshotReader {

  Map<CurveKey, CurveSnapshot> readCurves();

  UnstructuredMarketDataSnapshot readGlobalValues();

  Map<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> readVolatilitySurfaces();

  Map<YieldCurveKey, YieldCurveSnapshot> readYieldCurves();

  void close();

  String getName();

  String getBasisViewName();
}
