/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BatchExecutorFactory implements DependencyGraphExecutorFactory {

  private DependencyGraphExecutorFactory _delegate;

  public BatchExecutorFactory(DependencyGraphExecutorFactory delegate) {
    ArgumentChecker.notNull(delegate, "Delegate executor factory");
    _delegate = delegate;
  }

  @Override
  public BatchExecutor createExecutor(SingleComputationCycle cycle) {
    DependencyGraphExecutor delegate = _delegate.createExecutor(cycle);
    return new BatchExecutor(delegate);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " delegating to " + _delegate.toString();
  }

}
