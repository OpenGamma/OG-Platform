/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.view.calcnode.stats.FunctionCost;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MultipleNodeExecutorFactory implements DependencyGraphExecutorFactory<Object> {

  private int _minimumJobItems = 1;
  private int _maximumJobItems = Integer.MAX_VALUE;
  private long _minimumJobCost = 1;
  private long _maximumJobCost = Long.MAX_VALUE;
  private int _maximumConcurrency = Integer.MAX_VALUE;
  private FunctionCost _functionCost = new FunctionCost();

  public void setMinimumJobItems(final int minimumJobItems) {
    _minimumJobItems = minimumJobItems;
  }

  public int getMinimumJobItems() {
    return _minimumJobItems;
  }

  public void setMaximumJobItems(final int maximumJobItems) {
    _maximumJobItems = maximumJobItems;
  }

  public int getMaximumJobItems() {
    return _maximumJobItems;
  }

  public void setMinimumJobCost(final long minimumJobCost) {
    _minimumJobCost = minimumJobCost;
  }

  public long getMinimumJobCost() {
    return _minimumJobCost;
  }

  public void setMaximumJobCost(final long maximumJobCost) {
    _maximumJobCost = maximumJobCost;
  }

  public long getMaximumJobCost() {
    return _maximumJobCost;
  }

  public void setMaximumConcurrency(final int maximumConcurrency) {
    _maximumConcurrency = maximumConcurrency;
  }

  public int getMaximumConcurrency() {
    return _maximumConcurrency;
  }

  public void setFunctionCost(final FunctionCost functionCost) {
    ArgumentChecker.notNull(functionCost, "functionCost");
    _functionCost = functionCost;
  }

  public FunctionCost getFunctionCost() {
    return _functionCost;
  }

  @Override
  public MultipleNodeExecutor createExecutor(final SingleComputationCycle cycle) {
    ArgumentChecker.notNull(cycle, "cycle");
    return new MultipleNodeExecutor(cycle, getMinimumJobItems(), getMaximumJobItems(), getMinimumJobCost(), getMaximumJobCost(), getMaximumConcurrency(), getFunctionCost());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
