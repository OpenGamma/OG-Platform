/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.view.calcnode.stats.FunctionCosts;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * 
 */
public class MultipleNodeExecutorFactory implements DependencyGraphExecutorFactory<DependencyGraph>, InitializingBean {
  
  private static final int DEFAULT_EXECUTION_PLAN_CACHE = 100;

  private ExecutionPlanCache _executionPlanCache;
  private int _minimumJobItems = 1;
  private int _maximumJobItems = Integer.MAX_VALUE;
  private long _minimumJobCost = 1;
  private long _maximumJobCost = Long.MAX_VALUE;
  private int _maximumConcurrency = Integer.MAX_VALUE;
  private FunctionCosts _functionCosts;
  
  protected ExecutionPlanCache getExecutionPlanCache() {
    return _executionPlanCache;
  }
  
  public void setMinimumJobItems(final int minimumJobItems) {
    _minimumJobItems = minimumJobItems;
    invalidateExecutionPlanCache();
  }

  public int getMinimumJobItems() {
    return _minimumJobItems;
  }

  public void setMaximumJobItems(final int maximumJobItems) {
    _maximumJobItems = maximumJobItems;
    invalidateExecutionPlanCache();
  }

  public int getMaximumJobItems() {
    return _maximumJobItems;
  }

  public void setMinimumJobCost(final long minimumJobCost) {
    _minimumJobCost = minimumJobCost;
    invalidateExecutionPlanCache();
  }

  public long getMinimumJobCost() {
    return _minimumJobCost;
  }

  public void setMaximumJobCost(final long maximumJobCost) {
    _maximumJobCost = maximumJobCost;
    invalidateExecutionPlanCache();
  }

  public long getMaximumJobCost() {
    return _maximumJobCost;
  }

  /**
   * Sets the concurrency limit for job tails that are streamed to a single node host.
   * 
   * @param maximumConcurrency the number of job tails that are expected to be executing in parallel
   */
  public void setMaximumConcurrency(final int maximumConcurrency) {
    _maximumConcurrency = maximumConcurrency;
    invalidateExecutionPlanCache();
  }

  public int getMaximumConcurrency() {
    return _maximumConcurrency;
  }

  public void setFunctionCosts(final FunctionCosts functionCosts) {
    ArgumentChecker.notNull(functionCosts, "functionCosts");
    _functionCosts = functionCosts;
    invalidateExecutionPlanCache();
  }

  public FunctionCosts getFunctionCosts() {
    return _functionCosts;
  }

  public void setCacheSize(final int size) {
    _executionPlanCache = new ExecutionPlanCache(EHCacheUtils.createCacheManager(), size);
  }

  @Override
  public MultipleNodeExecutor createExecutor(final SingleComputationCycle cycle) {
    ArgumentChecker.notNull(cycle, "cycle");
    return new MultipleNodeExecutor(cycle, getMinimumJobItems(), getMaximumJobItems(), getMinimumJobCost(), getMaximumJobCost(), getMaximumConcurrency(), getFunctionCosts(), _executionPlanCache);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  protected void invalidateExecutionPlanCache() {
    if (_executionPlanCache != null) {
      _executionPlanCache.clear();
    }
  }

  @Override
  public void afterPropertiesSet() {
    if (getFunctionCosts() == null) {
      setFunctionCosts(new FunctionCosts());
    }
    if (_executionPlanCache == null) {
      setCacheSize(DEFAULT_EXECUTION_PLAN_CACHE);
    }
  }

}
