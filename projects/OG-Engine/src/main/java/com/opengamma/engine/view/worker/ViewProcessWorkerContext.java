/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessContext;
import com.opengamma.engine.view.impl.ViewProcessImpl;

/**
 * Context information for a {@link ViewComputationJob}. This would probably be eventually backed by a {@link ViewProcessImpl}, but distribution of work over remote nodes or multiple local threads
 * might mean there are intermediate synthetic contexts.
 * <p>
 * Some of the callback methods can have very large parameters (for example dependency graphs, or result models) when called remotely. Any callbacks that are not required by the receiver (for example
 * the process has no active listeners for that event) should be silently discarded at the earliest opportunity and not sent over the network.
 * <p>
 * The callback methods must be called from a worker's critical path, allowing the context to control the execution sequence by blocking if required. If it is not appropriate for the worker to be
 * blocked during the notification then the context (and not the worker) must implement an appropriate mechanism to allow control to return immediately to the worker while it performs an appropriate
 * action.
 */
public interface ViewProcessWorkerContext {

  /**
   * Returns the context for the owning process. The process context may contain both shared, immutable, state and configurable items. In the case where the job is spawned remotely, the remote host
   * should construct its own equivalent process context with direct reference to the shared items. Only the configurable items should read (or write) through to the origin process.
   * 
   * @return the context, not null
   */
  ViewProcessContext getProcessContext();

  /**
   * Notifies of successful view compilation.
   * 
   * @param dataProvider the market data the compilation was run against, not null
   * @param compiled the compiled view definition, including the dependency graphs, not null
   */
  void viewDefinitionCompiled(ViewExecutionDataProvider dataProvider, CompiledViewDefinitionWithGraphs compiled);

  // TODO: If implementing viewDefinitionCompiled as a remote call then only marketDataUser and marketDataSpecifications fields need to be sent.
  // The receiving context can construct a suitable object from those.

  /**
   * Notifies of unsuccessful view compilation.
   * 
   * @param compilationTime the compilation time used, not null
   * @param exception the fault that was detected and not recovered from, not null
   */
  void viewDefinitionCompilationFailed(Instant compilationTime, Exception exception);

  /**
   * Notifies of a cycle starting.
   * 
   * @param cycleMetadata information about the cycle, not null
   */
  void cycleStarted(ViewCycleMetadata cycleMetadata);

  /**
   * Notifies of a result, or partial result, fragment being produced.
   * 
   * @param result the result data, not null
   * @param viewDefinition the view definition this fragment was from
   */
  void cycleFragmentCompleted(ViewComputationResultModel result, ViewDefinition viewDefinition);

  // TODO: If implementing cycleFragmentCompleted as a remote call then it is reasonable to only send the view definition's ID. The receiver must be
  // able to resolve it as it originally supplied the view definition to the job. The whole view definition is sent here to optimise the more common
  // case of the job local to the process to avoid hitting even the cached view definition repository

  /**
   * Notifies of the cycle completing.
   * 
   * @param cycle the cycle that has completed, containing the full result model, not null
   */
  void cycleCompleted(ViewCycle cycle);

  /**
   * Notifies of a major execution failure. It was not possible to produce any results for the cycle because of a fault.
   * 
   * @param options the options used to attempt the cycle, not null
   * @param exception the fault that was detected and not recovered from, not null
   */
  void cycleExecutionFailed(ViewCycleExecutionOptions options, Exception exception);

  /**
   * Notifies of worker completion - all the requested cycles have been executed.
   */
  void workerCompleted();

}
