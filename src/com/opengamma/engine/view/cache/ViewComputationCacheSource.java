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
   * @param viewName The name of the view.
   * @param calculationConfigurationName The name of the calculation configuration within that view. 
   * @param timestamp The timestamp for the computation cache.
   * @return The cache for that timestamp.
   */
  ViewComputationCache getCache(String viewName, String calculationConfigurationName, long timestamp);
  
  /**
   * Take a deep copy of a cache, usually, to pass it over to a viewer
   * or other tool.  This doens't need to be released once you're through 
   * with it.
   * 
   * @param viewName The name of the view.
   * @param calculationConfigurationName The name of the calculation configuration within that view. 
   * @param timestamp The timestamp for the computation cache
   */
  ViewComputationCache cloneCache(String viewName, String calculationConfigurationName, long timestamp);
  
  /**
   * Release all caches previously generated using {@link #getCache(String, String, long)}
   * for the view name specified.
   * 
   * @param viewName The name of the view.
   * @param timestamp The timestamp for the cache.
   */
  void releaseCaches(String viewName, long timestamp);
  
}
