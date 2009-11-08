/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

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
   * @param viewName The name of the view. 
   * @return The cache for that timestamp.
   */
  ViewComputationCache getCache(String viewName, long timestamp);
  
  /**
   * Take a deep copy of a cache, usually, to pass it over to a viewer
   * or other tool.  This doens't need to be released once you're through 
   * with it.
   * 
   * @param timestamp The timestamp for the computation cache
   * @param viewName The name of the view.
   */
  ViewComputationCache cloneCache(String viewName, long timestamp);
  
  /**
   * Release a cache that was previously generated using
   * {@link #getCache(long)}.
   * 
   * @param timestamp The timestamp for the cache.
   * @param viewName The name of the view.
   */
  void releaseCache(String viewName, long timestamp);
  
}
