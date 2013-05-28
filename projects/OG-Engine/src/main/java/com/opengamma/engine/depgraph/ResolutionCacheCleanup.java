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
  /**
   * Run cleanup whenever the free memory is below this threshold. For example 0.3 gives 300Mb on a 1Gb VM.
   */
  private static final double THRESHOLD = 0.3;
  /**
   * Only run cleanup whenever the free memory is below this threshold, regardless of the % of total memory this is. For example never run if 1Gb or more is free.
   */
  private static final long MAX_FREE = 1024L * 1024L * 1024L;
  /**
   * Always run cleanup whenever the free memory is below this threshold, regardless of the % of total memory this is. For example always run if under 150Mb is free.
   */
  private static final long MIN_FREE = 150L * 1024L * 1024L;

  private ResolutionCacheCleanup() {
  }

  private boolean isLowMemory() {
    final long free = s_runtime.freeMemory();
    if (free >= MAX_FREE) {
      return false;
    }
    if (free < MIN_FREE) {
      return true;
    }
    final double fractionFree = (double) s_runtime.freeMemory() / (double) s_runtime.totalMemory();
    if (s_logger.isInfoEnabled()) {
      s_logger.info("Free memory = {}", fractionFree);
    }
    return fractionFree < THRESHOLD;
  }

  @Override
  public boolean tick(final DependencyGraphBuilder builder, final Void data) {
    if (isLowMemory()) {
      final int originalActive = builder.getActiveResolveTasks();
      if (builder.flushCachedStates()) {
        final int freedActive = originalActive - builder.getActiveResolveTasks();
        if (s_logger.isInfoEnabled()) {
          s_logger.info("Freed {} tasks for {}", freedActive, builder);
        }
      } else {
        s_logger.warn("Low memory detected, but no intermediate state to flush");
      }
    }
    builder.reportStateSize();
    return true;
  }

  @Override
  public boolean cancelled(final DependencyGraphBuilder builder, final Void data) {
    // After a cancel, don't want any more ticks
    return false;
  }

  @Override
  public boolean completed(final DependencyGraphBuilder builder, final Void data) {
    // Flush the cache and stop. If this is an intermediate state we'll be restarted. If the cache isn't empty, we'll run again
    // and keep going until the cache is empty.
    return builder.flushCachedStates();
  }

}
