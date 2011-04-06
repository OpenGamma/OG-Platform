/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 * A engine-level listener to events on a view process, allowing bulk attachment to everything. Downstream, these
 * events will be generally split into separate user-level listeners, or possibly consumed and hidden.
 */
public interface ViewProcessListener {

  /**
   * Gets whether the listener requires delta results.
   * 
   * @return {@code true} if delta results are required, {@code false} otherwise.
   */
  boolean isDeltaResultRequired();
  
  /**
   * Called to indicate that the view definition has been compiled. This is always called before
   * {@link #result(ViewComputationResultModel, ViewDeltaResultModel)} for only those results calculated from the
   * compiled view definition.
   * 
   * @param compiledViewDefinition  the compiled view definition, not null
   */
  void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition);
  
  /**
   * Called following the successful completion of a computation cycle.
   * 
   * @param fullResult  the entire computation cycle result, not null
   * @param deltaResult  the delta result, if delta results are currently being requested, null otherwise
   */
  void result(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult);
  
  /**
   * Called to indicate that an error has occurred. The view process may recover from some errors, so their effects may
   * otherwise go unnoticed.
   * 
   * @param errorType  the type of error, not null
   * @param details  a description of the error, possibly null
   * @param exception  any exception associated with the error, possibly null
   */
  void error(ViewProcessErrorType errorType, String details, Exception exception);
  
  /**
   * Called to indicate that the view process has completed, meaning that there are no more computation cycles to
   * execute. This does not necessarily imply that the process has been entirely successful.
   */
  void processCompleted();
  
  /**
   * Called to indicate that the view process is shutting down.
   */
  void shutdown();
  
}
