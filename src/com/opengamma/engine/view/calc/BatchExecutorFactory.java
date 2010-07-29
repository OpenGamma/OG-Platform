/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

/**
 * 
 */
public class BatchExecutorFactory implements DependencyGraphExecutorFactory {

  @Override
  public DependencyGraphExecutor createExecutor(SingleComputationCycle cycle) {
    return new BatchExecutor(new SingleNodeExecutor(cycle));
  }

}
