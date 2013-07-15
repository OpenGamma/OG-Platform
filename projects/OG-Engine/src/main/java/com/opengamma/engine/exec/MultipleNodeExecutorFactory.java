/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import net.sf.ehcache.CacheManager;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.calcnode.stats.FunctionCosts;
import com.opengamma.engine.exec.plan.CachingExecutionPlanner;
import com.opengamma.engine.exec.plan.MultipleNodeExecutionPlanner;

/**
 * 
 */
public class MultipleNodeExecutorFactory extends PlanBasedGraphExecutorFactory implements InitializingBean {

  private final MultipleNodeExecutionPlanner _basePlanner;
  private CachingExecutionPlanner _cachingPlanner;
  private CacheManager _cacheManager;

  private MultipleNodeExecutorFactory(final MultipleNodeExecutionPlanner planner) {
    super(planner);
    _basePlanner = planner;
  }

  public MultipleNodeExecutorFactory() {
    this(new MultipleNodeExecutionPlanner());
  }

  public void setCacheManager(CacheManager cacheManager) {
    _cacheManager = cacheManager;
  }

  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the minimum number of items that the planner will attempt to put into each job.
   * 
   * @param minimumJobItems the number of items
   * @see MultipleNodeExecutionPlanner#setMinimumJobItems
   */
  public void setMinimumJobItems(final int minimumJobItems) {
    _basePlanner.setMininumJobItems(minimumJobItems);
  }

  /**
   * Returns the minimum number of items that the planner will attempt to put into each job.
   * 
   * @return the number of items
   * @see MultipleNodeExecutionPlanner#getMinimumJobItems
   */
  public int getMinimumJobItems() {
    return _basePlanner.getMinimumJobItems();
  }

  /**
   * Sets the maximum number of items that the planner will attempt to put into each job.
   * 
   * @param maximumJobItems the number of items
   * @see MultipleNodeExecutionPlanner#setMaximumJobItems
   */
  public void setMaximumJobItems(final int maximumJobItems) {
    _basePlanner.setMaximimJobItems(maximumJobItems);
  }

  /**
   * Returns the maximum number of items that the planner will attempt to put into each job.
   * 
   * @return the number of items
   * @see MultipleNodeExecutionPlanner#getMaximumJobItems
   */
  public int getMaximumJobItems() {
    return _basePlanner.getMaximumJobItems();
  }

  /**
   * Sets the minimum estimated cost of jobs that the planner will attempt to produce.
   * 
   * @param minimumJobCost the estimated cost
   * @see MultipleNodeExecutionPlanner#setMinimumJobCost
   */
  public void setMinimumJobCost(final long minimumJobCost) {
    _basePlanner.setMinimumJobCost(minimumJobCost);
  }

  /**
   * Returns the minimum estimated cost of jobs that the planner will attempt to produce.
   * 
   * @return the estimated cost
   * @see MultipleNodeExecutionPlanner#getMinimumJobCost
   */
  public long getMinimumJobCost() {
    return _basePlanner.getMinimumJobCost();
  }

  /**
   * Sets the maximum estimated cost of jobs that the planner will attempt to produce.
   * 
   * @param maximumJobCost the estimated cost
   * @see MultipleNodeExecutionPlanner#setMaximumJobCost
   */
  public void setMaximumJobCost(final long maximumJobCost) {
    _basePlanner.setMaximumJobCost(maximumJobCost);
  }

  /**
   * Returns the maximum estimated cost of jobs that the planner will attempt to produce.
   * 
   * @return the estimated cost
   * @see MultipleNodeExecutionPlanner#getMaximumJobCost
   */
  public long getMaximumJobCost() {
    return _basePlanner.getMaximumJobCost();
  }

  /**
   * Sets the concurrency limit for job tails that are streamed to a single node host.
   * 
   * @param maximumConcurrency the number of job tails that are expected to be executing in parallel
   */
  public void setMaximumConcurrency(final int maximumConcurrency) {
    _basePlanner.setMaximumConcurrency(maximumConcurrency);
  }

  /**
   * Returns the concurrency limit for job tails that are streamed to a single node host.
   * 
   * @return the number of job tails that are expected to be executing in parallel
   */
  public int getMaximumConcurrency() {
    return _basePlanner.getMaximumConcurrency();
  }

  public void setFunctionCosts(final FunctionCosts functionCosts) {
    _basePlanner.setFunctionCosts(functionCosts);
  }

  public FunctionCosts getFunctionCosts() {
    return _basePlanner.getFunctionCosts();
  }

  /**
   * Invalidates any cached execution plans (if caching is enabled). If any of the parameters have been changed then this should be called so that they will take effect instead of any previously
   * cached plans being used.
   */
  public void invalidateCache() {
    final CachingExecutionPlanner planner = _cachingPlanner;
    if (planner != null) {
      planner.invalidate();
    }
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() {
    final CacheManager cacheManager = getCacheManager();
    if (cacheManager != null) {
      if (_cachingPlanner == null) {
        _cachingPlanner = new CachingExecutionPlanner(_basePlanner, cacheManager);
      }
      setPlanner(_cachingPlanner);
    }
  }
}
