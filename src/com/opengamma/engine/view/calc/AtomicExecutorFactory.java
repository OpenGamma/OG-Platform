/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

/**
 * 
 */
public class AtomicExecutorFactory implements DependencyGraphExecutorFactory {

  @Override
  public DependencyGraphExecutor createExecutor(SingleComputationCycle cycle) {
    return new AtomicExecutor(cycle);
  }

}
