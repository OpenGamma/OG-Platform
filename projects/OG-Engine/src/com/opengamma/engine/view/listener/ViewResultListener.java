/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import javax.time.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

/**
 * A listener to the output of a view process. Calls to the listener are always made in the sequence in which they
 * occur; it may be assumed that the listener will not be used concurrently.
 */
@PublicAPI
public interface ViewResultListener {

  /**
   * Gets the user represented by this listener.
   * 
   * @return the user represented by this listener, not null
   */
  UserPrincipal getUser();

  //-------------------------------------------------------------------------
  // REVIEW jonathan 2011-06-27 -- a boolean permission flag might not be enough in future as it would be useful to
  // know exactly what a user doesn't have permissions on, at the very least for information, but possibly to allow
  // access to a subset of the results. The boolean flag preserves the previous functionality but it's easy to see how
  // it might be extended.
  /**
   * Called to indicate that the view definition has been compiled.
   * This is always called before
   * {@link #cycleCompleted(ViewComputationResultModel, ViewDeltaResultModel)} for
   * exactly those results calculated from the compiled view definition;
   * it will be called again if recompilation is necessary for future results.
   * 
   * @param compiledViewDefinition  the compiled view definition, not null
   * @param hasMarketDataPermissions  true if the listener's user has permission to access all market data
   *                                      requirements of the compiled view definition
   */
  void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions);

  /**
   * Called to indicate that the view definition failed to compile.
   * 
   * @param valuationTime  the valuation time at which compilation was attempted, not null
   * @param exception  an exception associated with the failure, may be null
   */
  void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception);

  //-------------------------------------------------------------------------
  /**
   * Called following the successful completion of a computation cycle.
   * 
   * @param fullResult  the entire computation cycle result
   * @param deltaResult  the delta result representing only the differences since the previous result
   */
  void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult);

  /**
   * Called following the successful completion of a fragment of a computation cycle.
   * <p>
   * A computation cycle may be broken into many fragments, each representing internal groups of calculations. This
   * allows the results to be received as they are calculated, without waiting for the cycle to complete.
   *
   * @param fullFragment  the full computation cycle result fragment
   * @param deltaFragment  the delta fragment representing only the differences since the previous cycle
   */
  void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment);

  /**
   * Called to indicate that execution of a view cycle failed.
   * 
   * @param executionOptions  the cycle execution options which caused the failure, not null
   * @param exception an exception associated with the failure, may be null
   * 
   */
  void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception);

  //-------------------------------------------------------------------------
  /**
   * Called to indicate that the view process has completed, meaning that there are no more computation cycles to
   * execute. This does not necessarily imply that the process has been entirely successful, but no further results
   * will be produced.
   */
  void processCompleted();

  /**
   * Called to indicate that the view process has terminated. No further results will be produced.
   * <p>
   * This could be the result of an administrator forcibly terminating the process.
   * 
   * @param executionInterrupted  true if the process termination caused execution to be interrupted;
   *                              false otherwise, for example if execution has already completed
   */
  void processTerminated(boolean executionInterrupted);

}
