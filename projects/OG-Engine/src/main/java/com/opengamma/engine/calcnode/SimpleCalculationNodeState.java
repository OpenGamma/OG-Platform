/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import com.opengamma.engine.cache.DeferredViewComputationCache;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.FunctionExecutionContext;

/**
 * The per-thread state for a calculation node.
 */
/* package */class SimpleCalculationNodeState {

  private FunctionExecutionContext _functionExecutionContext;
  private CalculationJob _job;
  private CompiledFunctionRepository _functions;
  private DeferredViewComputationCache _cache;
  private String _calculationConfiguration;
  private long _executionTime;

  private SimpleCalculationNodeState(final SimpleCalculationNodeState copyFrom) {
    restoreState(copyFrom);
  }

  protected SimpleCalculationNodeState(final FunctionExecutionContext executionContext) {
    _functionExecutionContext = executionContext.clone();
  }

  public SimpleCalculationNodeState saveState() {
    final SimpleCalculationNodeState state = new SimpleCalculationNodeState(this);
    setFunctionExecutionContext(getFunctionExecutionContext().clone());
    return state;
  }

  public void restoreState(final SimpleCalculationNodeState state) {
    setFunctionExecutionContext(state.getFunctionExecutionContext());
    setJob(state.getJob());
    setFunctions(state.getFunctions());
    setCache(state.getCache());
    setConfiguration(state.getConfiguration());
    setExecutionStartTime(state.getExecutionStartTime());
  }

  protected void setFunctionExecutionContext(FunctionExecutionContext functionExecutionContext) {
    _functionExecutionContext = functionExecutionContext;
  }

  protected FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }

  protected void setJob(CalculationJob job) {
    _job = job;
  }

  protected CalculationJob getJob() {
    return _job;
  }

  protected void setFunctions(final CompiledFunctionRepository functions) {
    _functions = functions;
  }

  protected CompiledFunctionRepository getFunctions() {
    return _functions;
  }

  protected void setCache(final DeferredViewComputationCache cache) {
    _cache = cache;
  }

  protected DeferredViewComputationCache getCache() {
    return _cache;
  }

  protected void setConfiguration(final String configuration) {
    _calculationConfiguration = configuration;
  }

  protected String getConfiguration() {
    return _calculationConfiguration;
  }

  protected long getExecutionStartTime() {
    return _executionTime;
  }

  protected void setExecutionStartTime(final long executionTime) {
    _executionTime = executionTime;
  }

}
