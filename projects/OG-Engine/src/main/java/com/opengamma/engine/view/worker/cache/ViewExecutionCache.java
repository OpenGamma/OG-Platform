/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;

/**
 * A cache of information shared by a group of workers to limit the amount of overall work done when similar views (or the same view) is executing multiple times concurrently.
 * <p>
 * A cache may be implemented in a distributed fashion to share information among workers on separate hosts or in separate processes on the same host. A best efforts consistency is required - the
 * purpose of the cache is to provide better overall performance, rather than a consistent shared storage area.
 */
public interface ViewExecutionCache {

  /**
   * Fetches a compiled view definition from the cache, if one exists.
   * 
   * @param key the key to query the cache with, not null
   * @return the cached definition, or null if there is none
   */
  CompiledViewDefinitionWithGraphs getCompiledViewDefinitionWithGraphs(ViewExecutionCacheKey key);

  /**
   * Stores a compiled view definition into the cache. If the cache already contains a compiled view definition for the given key, it should replace the existing one with the new definition.
   * 
   * @param key the key to store the definition against, not null
   * @param viewDefinition the compiled view definition to store, not null
   */
  void setCompiledViewDefinitionWithGraphs(ViewExecutionCacheKey key, CompiledViewDefinitionWithGraphs viewDefinition);

  /**
   * Clears the cache
   */
  void clear();
}
