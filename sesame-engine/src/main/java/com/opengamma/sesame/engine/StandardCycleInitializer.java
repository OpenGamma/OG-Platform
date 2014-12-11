/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import com.google.common.cache.Cache;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.graph.Graph;
import com.opengamma.util.ArgumentChecker;

/**
 * A cycle initializer to be used in standard (non-capturing)
 * cycles.
 */
class StandardCycleInitializer implements CycleInitializer {

  private final ServiceContext _context;
  private final Graph _graph;
  private final Cache<Object, Object> _cache;

  /**
   * Create the cycle initializer.
   *
   * @param originalContext the current service context source for the cycle
   * @param calculationArguments the calculation arguments
   * @param graph the current graph
   * @param cache the cache that should be used by functions during the cycle
   */
  StandardCycleInitializer(ServiceContext originalContext,
                           CalculationArguments calculationArguments,
                           Graph graph,
                           Cache<Object, Object> cache) {
    ArgumentChecker.notNull(originalContext, "originalContext");
    ArgumentChecker.notNull(calculationArguments, "calculationArguments");
    VersionCorrectionProvider vcProvider = getVersionCorrectionProvider(calculationArguments);
    _context = originalContext.with(VersionCorrectionProvider.class, vcProvider);
    _graph = graph;
    _cache = cache;
  }

  private VersionCorrectionProvider getVersionCorrectionProvider(CalculationArguments calculationArguments) {
    VersionCorrection correction = calculationArguments.getConfigVersionCorrection();
    return correction == null || correction.containsLatest() ?
        new FixedInstantVersionCorrectionProvider() :
        new FixedInstantVersionCorrectionProvider(correction.getVersionAsOf());
  }

  @Override
  public ServiceContext getServiceContext() {
    return _context;
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
