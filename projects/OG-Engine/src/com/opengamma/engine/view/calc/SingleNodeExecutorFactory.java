/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.view.calcnode.CalculationJobResult;


/**
 * 
 */
public class SingleNodeExecutorFactory implements DependencyGraphExecutorFactory<CalculationJobResult> {
  
  @Override
  public SingleNodeExecutor createExecutor(SingleComputationCycle cycle) {
    return new SingleNodeExecutor(cycle);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
