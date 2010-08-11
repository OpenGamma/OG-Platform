/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import com.opengamma.engine.view.calc.DependencyGraphExecutor;

/**
 * This listener is run on the master engine node.
 * It can modify, as necessary, the job to executed
 * on a calculation node on the grid.  
 */
public interface DependencyGraphExecutorListener {
  
  /**
   * There may be a need to modify the list of job items 
   * to execute in some way. For example, you may want not to 
   * execute certain items because they have already been executed, 
   * or you may want to execute additional items. This method
   * allows you to do that.
   * <p>
   * If entries are filtered, this means that they will really
   * not be executed at all - the shared computation cache
   * will not be modified as far as the excluded entries are concerned.
   * 
   * @param executor Executor that is about to submit a job to a calc node
   * @param job The job to be (potentially) modified
   */
  void preExecute(DependencyGraphExecutor executor, CalculationJob job);
  
  /**
   * Informs the listener that a calculation job was executed
   * on a calculation node.  
   * 
   * @param executor Executor that has just received a result from a calc node
   * @param result The job result 
   */
  void postExecute(DependencyGraphExecutor executor, CalculationJobResult result);

}
