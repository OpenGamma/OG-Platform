/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import com.opengamma.engine.view.calc.DependencyGraphExecutor;



/**
 * 
 */
public class DummyDependencyGraphExecutorListener implements DependencyGraphExecutorListener {

  @Override
  public void preExecute(DependencyGraphExecutor executor, CalculationJob job) {
  }

  @Override
  public void postExecute(DependencyGraphExecutor executor, CalculationJobResult result) {
  }

}
