/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;

/**
 * A trivial in-memory cache. This should be used for test infrastructure only; a cache that can spool to disk would probably be better.
 */
public class InMemoryViewExecutionCache implements ViewExecutionCache {

  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryViewExecutionCache.class);

  /**
   * The buffer to hold compiled view definitions.
   */
  private final Map<ViewExecutionCacheKey, CompiledViewDefinitionWithGraphs> _compiledViewDefinitions = new MapMaker().softValues().makeMap();

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinitionWithGraphs(ViewExecutionCacheKey key) {
    CompiledViewDefinitionWithGraphs viewDefinition = _compiledViewDefinitions.get(key);
    if (s_logger.isDebugEnabled()) {
      if (viewDefinition == null) {
        s_logger.debug("Cache miss CompiledViewDefinitionWithGraphs for {}", key);
      } else {
        s_logger.debug("Cache hit CompiledViewDefinitionWithGraphs for {}", key);
      }
    }
    return viewDefinition;
  }

  @Override
  public void setCompiledViewDefinitionWithGraphs(ViewExecutionCacheKey key, CompiledViewDefinitionWithGraphs viewDefinition) {
    s_logger.info("Storing CompiledViewDefinitionWithGraphs for {}", key);
    _compiledViewDefinitions.put(key, viewDefinition);
  }

  @Override
  public void clear() {
    s_logger.info("Clearing all CompiledViewDefinitionWithGraphs");
    _compiledViewDefinitions.clear();
  }

}
