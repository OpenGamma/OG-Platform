/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;


/**
 * 
 */
public class SingleNodeExecutorFactory implements DependencyGraphExecutorFactory {
  
  @Override
  public SingleNodeExecutor createExecutor(SingleComputationCycle cycle) {
    return new SingleNodeExecutor(cycle);
  }

}
