/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BatchExecutorFactory implements DependencyGraphExecutorFactory {
  
  private SingleNodeExecutorFactory _delegate;
  
  public BatchExecutorFactory(SingleNodeExecutorFactory delegate) {
    ArgumentChecker.notNull(delegate, "Delegate executor factory");
    _delegate = delegate;
  }

  @Override
  public BatchExecutor createExecutor(SingleComputationCycle cycle) {
    SingleNodeExecutor delegate = _delegate.createExecutor(cycle);
    return new BatchExecutor(delegate);
  }

}
