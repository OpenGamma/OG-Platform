/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import org.threeten.bp.Duration;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;

/**
 * Represents a single execution pass on a view definition with a particular processing context.
 */
@PublicAPI
public interface ViewCycle extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the view cycle.
   * 
   * @return the unique identifier, not null
   */
  @Override
  UniqueId getUniqueId();

  /**
   * Gets the unique identifier of the view process.
   * 
   * @return the unique identifier of the owning view process, not null
   */
  UniqueId getViewProcessId();

  /**
   * The name of this view cycle
   * 
   * @return the name of this view cycle
   */
  String getName();
  
  /**
   * Gets the state of the view cycle.
   * 
   * @return the state of the view cycle, not null
   */
  ViewCycleState getState();

  /**
   * Gets the duration of the cycle's execution. If the cycle is currently executing, this is the current duration.
   * 
   * @return the cycle's execution duration, or null if the cycle has not started executing or failed to execute successfully
   */
  Duration getDuration();
  
  /**
   * Gets the cycle's execution options.
   * 
   * @return the cycle's execution options, not null
   */
  ViewCycleExecutionOptions getExecutionOptions();

  /**
   * Gets the compiled view definition used during the cycle's execution
   * 
   * @return the compiled view definition, not null
   */
  CompiledViewDefinitionWithGraphs getCompiledViewDefinition();

  /**
   * Gets the output of the view cycle.
   * 
   * @return the output of the view cycle, not null
   */
  ViewComputationResultModel getResultModel();

  /**
   * Queries the view cycle for values. This is a low-level query which can return only the computed object itself.
   * <p>
   * For efficiency, the result object contains only terminal outputs, i.e. values explicitly requested in the view definition at the time of the computation. A computation cycle may be queried in
   * order to obtain any values which were calculated during the cycle, including intermediate values corresponding to inputs as well as terminal output values already present in the result object.
   * 
   * @param query the query, not null
   * @return the result of performing the query against the computation caches, not null
   */
  ComputationCacheResponse queryComputationCaches(ComputationCycleQuery query);

  /**
   * Queries the view cycle for results. Results contain the computed value itself together with execution details, as seen in the result object.
   * <p>
   * For efficiency, the result object contains only terminal outputs, i.e. values explicitly requested in the view definition at the time of the computation. A computation cycle may be queried in
   * order to obtain any results which were calculated during the cycle, including intermediate results corresponding to inputs as well as terminal outputs already present in the result object.
   * 
   * @param query the query, not null
   * @return the result of performing the query, not null
   */
  ComputationResultsResponse queryResults(ComputationCycleQuery query);

}
