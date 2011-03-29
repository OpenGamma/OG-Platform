/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.id.UniqueIdentifier;

/**
 * Generates, and manages the lifecycle of, {@link ViewComputationCache} instances.
 */
public interface ViewComputationCacheSource {
  
  /**
   * Generates a new computation cache, or returns the existing one, for the given identifying arguments.
   * 
   * @param viewProcessId  the unique identifier of the view process
   * @param calculationConfigurationName  the name of the view calculation configuration 
   * @param evaluationTime  the view evaluation time
   * @return the computation cache for the given arguments
   */
  ViewComputationCache getCache(UniqueIdentifier viewProcessId, String calculationConfigurationName, long evaluationTime);
  
  // REVIEW kirk 2010-08-07 -- This might be better suited with another method. It's not currently
  // being called by anything.
  /**
   * Takes a deep copy of the cache identified by the arguments, usually to pass it over to a viewer or other tool. The
   * clone does not need to be released after use.
   * 
   * @param viewProcessId  the unique identifier of the view process
   * @param calculationConfigurationName  the name of the view calculation configuration 
   * @param evaluationTime  the view evaluation time
   * @return the cloned cache
   */
  ViewComputationCache cloneCache(UniqueIdentifier viewProcessId, String calculationConfigurationName, long evaluationTime);
  
  /**
   * Releases all caches previously generated using {@link #getCache(UniqueIdentifier, String, long)} for the given
   * view and evaluation time.
   * 
   * @param viewProcessId  the unique identifier of the view process
   * @param evaluationTime  the view evaluation time
   */
  void releaseCaches(UniqueIdentifier viewProcessId, long evaluationTime);
  
}
