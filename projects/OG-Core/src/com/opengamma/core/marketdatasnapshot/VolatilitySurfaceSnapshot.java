/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public interface VolatilitySurfaceSnapshot {
  Map<Pair<Object, Object>, ValueSnapshot> getValues();
}
