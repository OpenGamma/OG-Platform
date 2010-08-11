/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.view.calcnode.DependencyGraphExecutorListener;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SingleNodeExecutorFactory implements DependencyGraphExecutorFactory {
  
  private final DependencyGraphExecutorListener _listener;
  
  public SingleNodeExecutorFactory(DependencyGraphExecutorListener listener) {
    ArgumentChecker.notNull(listener, "Executor listener");
    _listener = listener;
  }

  @Override
  public SingleNodeExecutor createExecutor(SingleComputationCycle cycle) {
    return new SingleNodeExecutor(cycle, _listener);
  }

}
