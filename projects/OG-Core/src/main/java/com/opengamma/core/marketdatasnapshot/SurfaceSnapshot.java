/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import com.opengamma.util.tuple.Pair;

/**
 * A snapshot of surface data.
 */
public interface SurfaceSnapshot {

  /**
   * Gets the value snapshots.
   * @return The values
   */
  Map<Pair<Object, Object>, ValueSnapshot> getValues();
}
