/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.id.UniqueId;

/**
 * Generates, and manages the lifecycle of, {@link ViewComputationCache} instances.
 */
public interface ViewComputationCacheSource {
  
  /**
   * Generates a new computation cache, or returns the existing one, for the given identifying arguments.
   * 
   * @param viewCycleId  the unique identifier of the view cycle, not null
   * @param calculationConfigurationName  the name of the view calculation configuration, not null 
   * @return the computation cache for the given arguments
   */
  ViewComputationCache getCache(UniqueId viewCycleId, String calculationConfigurationName);
  
  // REVIEW kirk 2010-08-07 -- This might be better suited with another method. It's not currently
  // being called by anything.
  /**
   * Takes a deep copy of the cache identified by the arguments, usually to pass it over to a viewer or other tool. The
   * clone does not need to be released after use.
   * 
   * @param viewCycleId  the unique identifier of the view cycle, not null
   * @param calculationConfigurationName  the name of the view calculation configuration, not null 
   * @return the cloned cache
   */
  ViewComputationCache cloneCache(UniqueId viewCycleId, String calculationConfigurationName);
  
  /**
   * Releases all caches previously generated using {@link #getCache(UniqueId, String)} for the given
   * view cycle.
   * 
   * @param viewCycleId  the unique identifier of the view cycle
   */
  void releaseCaches(UniqueId viewCycleId);
  
}
