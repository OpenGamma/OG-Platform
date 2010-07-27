/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

/**
 * 
 */
public interface DependencyGraphExecutorFactory {
  
  DependencyGraphExecutor createExecutor(SingleComputationCycle cycle);

}
