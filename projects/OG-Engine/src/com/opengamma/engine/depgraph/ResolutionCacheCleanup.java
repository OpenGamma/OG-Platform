/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cleanup task for the intermediate state used by the graph builder. Periodically checks that the number of resolvers that are in the cache is not too many and discards any finished ones. This
 * releases the memory used to hold resultant specifications or errors from the resolution. If the resolution is required again then the algorithm will be repeated - this is a tradeoff between memory
 * and speed.
 */
/* package */final class ResolutionCacheCleanup implements Housekeeper.Callback<Void> {

  /**
   * Singleton instance.
   */
  public static final ResolutionCacheCleanup INSTANCE = new ResolutionCacheCleanup();

  private static final Logger s_logger = LoggerFactory.getLogger(ResolutionCacheCleanup.class);
  private static final Runtime s_runtime = Runtime.getRuntime();
  private static final double THRESHOLD = 0.3;

  private ResolutionCacheCleanup() {
  }

  @Override
  public boolean tick(final DependencyGraphBuilder builder, final Void data) {
    final double fractionFree = (double) s_runtime.freeMemory() / (double) s_runtime.totalMemory();
    if (s_logger.isInfoEnabled()) {
      s_logger.info("Free memory = {}", fractionFree);
    }
    if (fractionFree < THRESHOLD) {
      final int originalActive = builder.getActiveResolveTasks();
      builder.flushCachedStates();
      final int freedActive = originalActive - builder.getActiveResolveTasks();
      if (s_logger.isInfoEnabled()) {
        s_logger.info("Freed {} tasks for {}", freedActive, builder);
      }
      // TODO: monitor how successful the flush was -- don't want to hammer it too hard
    } else {
      builder.reportStateSize();
    }
    return true;
  }

  @Override
  public boolean cancelled(final DependencyGraphBuilder builder, final Void data) {
    // After a cancel, don't want any more ticks
    return false;
  }

  @Override
  public boolean completed(final DependencyGraphBuilder builder, final Void data) {
    // Ignore completion - might be an intermediate state
    return true;
  }

}
