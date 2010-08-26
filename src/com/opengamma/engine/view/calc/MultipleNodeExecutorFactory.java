/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

/**
 * 
 */
public class MultipleNodeExecutorFactory implements DependencyGraphExecutorFactory<Object> {

  private int _minimumJobItems = 1;
  private int _maximumJobItems = Integer.MAX_VALUE;
  private int _minimumJobCost = 1;
  private int _maximumJobCost = Integer.MAX_VALUE;
  private int _maximumConcurrency = Integer.MAX_VALUE;

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

  public void setMinimumJobCost(final int minimumJobCost) {
    _minimumJobCost = minimumJobCost;
  }

  public int getMinimumJobCost() {
    return _minimumJobCost;
  }

  public void setMaximumJobCost(final int maximumJobCost) {
    _maximumJobCost = maximumJobCost;
  }

  public int getMaximumJobCost() {
    return _maximumJobCost;
  }

  public void setMaximumConcurrency(final int maximumConcurrency) {
    _maximumConcurrency = maximumConcurrency;
  }

  public int getMaximumConcurrency() {
    return _maximumConcurrency;
  }

  @Override
  public MultipleNodeExecutor createExecutor(final SingleComputationCycle cycle) {
    return new MultipleNodeExecutor(cycle, getMinimumJobItems(), getMaximumJobItems(), getMinimumJobCost(), getMaximumJobCost(), getMaximumConcurrency());
  }

}
