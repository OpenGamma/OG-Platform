/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import javax.management.MXBean;

/**
 * MBean interface for exposing the engine cache via JMX.
 */
@MXBean
public interface CacheMonitor {

  /**
   * Discards all entries from the engine cache.
   * <p>
   * The current cache is unchanged, but it is replaced by an empty cache.
   * This doesn't affect views that are already running a calculation cycle,
   * they continue to use the old cache until they start their next cycle.
   */
  void clearCache();
}
