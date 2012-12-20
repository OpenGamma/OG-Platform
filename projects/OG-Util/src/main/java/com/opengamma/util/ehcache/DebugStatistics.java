/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Statistics;

import org.slf4j.Logger;

import com.opengamma.util.TerminatableJob;

/**
 * Very crude diagnostic tool for monitoring the traffic to a cache.
 */
public final class DebugStatistics extends TerminatableJob {

  private final Logger _logger;
  private final Cache[] _caches;

  private DebugStatistics(final Logger logger, final Cache[] caches) {
    _logger = logger;
    _caches = caches;
  }

  public static TerminatableJob forCaches(final Logger logger, final Cache... caches) {
    logger.error("DebugStatistics object created - do not use in production code");
    final DebugStatistics stats = new DebugStatistics(logger, caches);
    final Thread statsThread = new Thread(stats);
    statsThread.setDaemon(true);
    statsThread.start();
    return stats;
  }

  private void report(final Cache cache) {
    final Statistics stats = cache.getStatistics();
    final long hits = stats.getCacheHits();
    final long misses = stats.getCacheMisses();
    final double ratio = (misses != 0) ? (double) hits / (double) misses : 0d;
    _logger.info("Cache {} hits = {}, misses = {}, ratio = {}", new Object[] {cache.getName(), hits, misses, ratio});
  }

  @Override
  protected void runOneCycle() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
    }
    for (Cache cache : _caches) {
      report(cache);
    }
  }

}
