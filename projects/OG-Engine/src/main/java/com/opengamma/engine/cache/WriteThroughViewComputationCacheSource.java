/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import com.opengamma.id.UniqueId;

/**
 * Decorates an existing {@link ViewComputationCacheSource} to put a {@link WriteThroughViewComputationCache} around the returned cache instances. This can be faster in some cases where large items
 * are read multiple times rapidly after they are created. The reads will come from the object cached in memory.
 */
public class WriteThroughViewComputationCacheSource implements ViewComputationCacheSource {

  private final ViewComputationCacheSource _underlying;

  public WriteThroughViewComputationCacheSource(final ViewComputationCacheSource underlying) {
    _underlying = underlying;
  }

  protected ViewComputationCacheSource getUnderlying() {
    return _underlying;
  }

  // ViewComputationCacheSource

  @Override
  public ViewComputationCache getCache(UniqueId viewCycleId, String calculationConfigurationName) {
    return WriteThroughViewComputationCache.of(getUnderlying().getCache(viewCycleId, calculationConfigurationName));
  }

  @Override
  public ViewComputationCache cloneCache(UniqueId viewCycleId, String calculationConfigurationName) {
    return WriteThroughViewComputationCache.of(getUnderlying().cloneCache(viewCycleId, calculationConfigurationName));
  }

  @Override
  public void releaseCaches(UniqueId viewCycleId) {
    getUnderlying().releaseCaches(viewCycleId);
  }

}
