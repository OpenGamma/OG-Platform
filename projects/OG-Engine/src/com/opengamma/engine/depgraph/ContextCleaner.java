/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

/**
 * Cleanup task for the graph building context.
 */
/* package */final class ContextCleaner implements Housekeeper.Callback<Void> {

  public static final ContextCleaner INSTANCE = new ContextCleaner();

  private ContextCleaner() {
  }

  @Override
  public boolean tick(final DependencyGraphBuilder builder, final Void data) {
    if (builder.getContext().getActiveResolveTasks() > builder.getResolutionCacheSize()) {
      builder.getContext().flushCachedStates();
      int active = builder.getContext().getActiveResolveTasks();
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
