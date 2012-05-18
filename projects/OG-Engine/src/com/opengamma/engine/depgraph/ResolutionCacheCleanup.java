/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

/**
 * Cleanup task for the intermediate state used by the graph builder. Periodically checks that the number of resolvers that are in the cache is not too many and discards any finished ones. This
 * releases the memory used to hold resultant specifications or errors from the resolution. If the resolution is required again then the algorithm will be repeated - this is a tradeoff between memory
 * and speed.
 */
/* package */final class ResolutionCacheCleanup implements Housekeeper.Callback<Void> {

  public static final ResolutionCacheCleanup INSTANCE = new ResolutionCacheCleanup();

  private ResolutionCacheCleanup() {
  }

  @Override
  public boolean tick(final DependencyGraphBuilder builder, final Void data) {
    if (builder.getActiveResolveTasks() > builder.getResolutionCacheSize()) {
      builder.flushCachedStates();
      int active = builder.getActiveResolveTasks();
      int cacheSize = builder.getResolutionCacheSize();
      if (active > cacheSize / 2) {
        while (active > cacheSize / 2) {
          cacheSize += cacheSize >> 1;
        }
        builder.setResolutionCacheSize(cacheSize);
      }
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
