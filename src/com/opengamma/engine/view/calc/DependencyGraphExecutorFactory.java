/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

/**
 *
 * @param <T> return type of the executor
 */
public interface DependencyGraphExecutorFactory<T> {
  
  DependencyGraphExecutor<T> createExecutor(SingleComputationCycle cycle);

}
