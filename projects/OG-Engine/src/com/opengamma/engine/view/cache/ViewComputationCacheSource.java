/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

/**
 * Generates and manages the lifecycle of {@link ViewCalculationCache} instances. 
 *
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
  
  // REVIEW kirk 2010-08-07 -- This might be better suited with another method. It's not currently
  // being called by anything.
  /**
   * Take a deep copy of a cache, usually, to pass it over to a viewer
   * or other tool.  This doens't need to be released once you're through 
   * with it.
   * 
   * @param viewName The name of the view.
   * @param calculationConfigurationName The name of the calculation configuration within that view. 
   * @param timestamp The timestamp for the computation cache
   * @return The closed cache
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
