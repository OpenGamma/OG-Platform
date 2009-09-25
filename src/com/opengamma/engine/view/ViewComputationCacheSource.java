/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

/**
 * Generates {@link ViewCalculationCache} instances. 
 *
 * @author kirk
 */
public interface ViewComputationCacheSource {
  
  /**
   * Generate a new source, or return the existing one for the
   * specified timestamp.
   * 
   * @param timestamp The timestamp for the computation cache.
   * @return The cache for that timestamp.
   */
  ViewComputationCache getCache(String viewName, long timestamp);
  
  /**
   * Release a cache that was previously generated using
   * {@link #getCache(long)}.
   * 
   * @param timestamp The timestamp for the cache.
   */
  void releaseCache(String viewName, long timestamp);
  
}
