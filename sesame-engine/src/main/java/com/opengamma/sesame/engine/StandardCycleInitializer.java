/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import com.google.common.cache.Cache;
import com.opengamma.service.ServiceContext;
import com.opengamma.sesame.graph.Graph;
import com.opengamma.sesame.marketdata.CycleMarketDataFactory;

/**
 * A cycle initializer to be used in standard (non-capturing)
 * cycles.
 */
class StandardCycleInitializer implements CycleInitializer {

  private final ServiceContext _originalContext;
  private final CycleMarketDataFactory _cycleMarketDataFactory;
  private final Graph _graph;
  private final Cache<Object, Object> _cache;

  /**
   * Create the cycle initializer.
   *  @param originalContext the current service context
   * @param cycleMarketDataFactory the current market data
   * source for the cycle
   * @param graph the current graph
   * @param cache the cache that should be used by functions during the cycle
   */
  StandardCycleInitializer(ServiceContext originalContext,
                           CycleMarketDataFactory cycleMarketDataFactory,
                           Graph graph,
                           Cache<Object, Object> cache) {
    _originalContext = originalContext;
    _cycleMarketDataFactory = cycleMarketDataFactory;
    _graph = graph;
    _cache = cache;
  }

  @Override
  public CycleMarketDataFactory getCycleMarketDataFactory() {
    return _cycleMarketDataFactory;
  }

  @Override
  public ServiceContext getServiceContext() {
    return _originalContext;
  }

  @Override
  public Graph getGraph() {
    return _graph;
  }

  @Override
  public Cache<Object, Object> getCache() {
    return _cache;
  }

  /**
   * No processing to be done, just returns the results directly.
   *
   * @param results  the results of the cycle run
   * @return the supplied results unchanged
   */
  @Override
  public Results complete(Results results) {
    return results;
  }
}
