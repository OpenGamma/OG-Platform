/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import javax.time.Duration;

import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.engine.view.calc.ComputationCacheResponse;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.calc.ViewCycleState;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.id.UniqueId;

/**
 *
 */
/* package */ class EmptyViewCycle implements ViewCycle {

  /* package */ static final EngineResourceReference<ViewCycle> REFERENCE = new EmptyViewCycleReference();
  /* package */ static final ViewCycle INSTANCE = new EmptyViewCycle();

  private static final ComputationCacheResponse EMPTY_RESPONSE = new ComputationCacheResponse();
  private static final InMemoryViewComputationResultModel EMPTY_RESULTS = new InMemoryViewComputationResultModel();

  private EmptyViewCycle() {
  }

  @Override
  public UniqueId getUniqueId() {
    throw new UnsupportedOperationException("getUniqueId not supported");
  }

  @Override
  public UniqueId getViewProcessId() {
    throw new UnsupportedOperationException("getViewProcessId not supported");
  }

  @Override
  public ViewCycleState getState() {
    throw new UnsupportedOperationException("getState not supported");
  }

  @Override
  public Duration getDuration() {
    return Duration.ZERO;
  }

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinition() {
    throw new UnsupportedOperationException("getCompiledViewDefinition not supported");
  }

  @Override
  public ViewComputationResultModel getResultModel() {
    return EMPTY_RESULTS;
  }

  @Override
  public ComputationCacheResponse queryComputationCaches(ComputationCacheQuery computationCacheQuery) {
    return EMPTY_RESPONSE;
  }

  /* package */ static class EmptyViewCycleReference implements EngineResourceReference<ViewCycle> {

    private EmptyViewCycleReference() {
    }

    @Override
    public ViewCycle get() {
      return EmptyViewCycle.INSTANCE;
    }

    @Override
    public void release() {
      // do nothing
    }
  }
}
