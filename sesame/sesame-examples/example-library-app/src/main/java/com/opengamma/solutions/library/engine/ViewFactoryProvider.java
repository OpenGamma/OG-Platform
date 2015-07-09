/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.engine;

import java.util.concurrent.ExecutorService;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.opengamma.sesame.cache.NoOpCacheInvalidator;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FunctionService;
import com.opengamma.sesame.engine.ViewFactory;
import com.opengamma.sesame.function.AvailableImplementationsImpl;
import com.opengamma.sesame.function.AvailableOutputs;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract provider which creates a ViewFactory instance.
 */
@Singleton
public class ViewFactoryProvider implements Provider<ViewFactory> {
  
  //TODO parameterise this
  private static final int s_defaultCacheSize = 5000;
  private final ComponentMap _componentMap;
  private final ExecutorService _executorService;
  private final AvailableOutputs _availableOutputs;
  private final MetricRegistry _metricRegistry;
  
  /**
   * @param componentMap the component map to use
   * @param executorService the environment's {@link ExecutorService}
   * @param availableOutputs the available outputs
   * @param metricRegistry the metric registry for the environment
   */
  @Inject
  public ViewFactoryProvider(ComponentMap componentMap, 
                             ExecutorService executorService, 
                             AvailableOutputs availableOutputs,
                             MetricRegistry metricRegistry) {
    _componentMap = ArgumentChecker.notNull(componentMap, "componentMap");
    _executorService = ArgumentChecker.notNull(executorService, "executorService");
    _availableOutputs = ArgumentChecker.notNull(availableOutputs, "availableOutputs");
    _metricRegistry = ArgumentChecker.notNull(metricRegistry, "metricRegistry");
  }
  
  @Override
  public ViewFactory get() {
    
    return new ViewFactory(_executorService, 
                           _componentMap, 
                           _availableOutputs, 
                           new AvailableImplementationsImpl(), 
                           FunctionModelConfig.EMPTY, 
                           FunctionService.DEFAULT_SERVICES, 
                           createCacheBuilder(), 
                           new NoOpCacheInvalidator(), 
                           Optional.fromNullable(_metricRegistry));
  }

  /**
   * Creates a cache builder used by the view factory when it needs to create a new cache.
   * <p>
   * New caches are created are created whenever data in the current cache needs to be discarded.
   * Caches are shared between multiple views so it isn't safe to clear an existing cache as
   * it may be in use. So a new, empty cache is created and supplied to each view at the
   * start of its next calculation cycle.
   * 
   * @return the cache builder, not null
   */
  private CacheBuilder<Object, Object> createCacheBuilder() {
    int nProcessors = Runtime.getRuntime().availableProcessors();
    // concurrency level controls how many segments are created in the cache. a segment is locked while a value
    // is being calculated so we want enough segments to make it highly unlikely that two threads will try
    // to write a value to the same segment at the same time.
    // N.B. read operations can happen concurrently with writes, so the concurrency level only affects cache writes
    int concurrencyLevel = nProcessors * 8;
    return CacheBuilder.newBuilder()
        .maximumSize(s_defaultCacheSize)
        .concurrencyLevel(concurrencyLevel);
  }
  
}
