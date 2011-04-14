/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
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
  UniqueIdentifier getUniqueId();
  
  /**
   * Gets the unique identifier of the view process.
   * 
   * @return the unique identifier of the owning view process, not null
   */
  UniqueIdentifier getViewProcessId();
  
  /**
   * Gets the state of the view cycle.
   * 
   * @return the state of the view cycle, not null
   */
  ViewCycleState getState();
  
  /**
   * Gets the duration of the cycle's execution. If the cycle is currently executing, this is the current duration.
   * 
   * @return the cycle's execution time, or -1 if the cycle has not started executing or failed to execute successfully
   */
  long getDurationNanos();
  
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
   * Queries the view cycle for values.
   * <p>
   * For efficiency, the result object contains only terminal outputs, i.e. values explicitly requested in the view
   * definition at the time of the computation. A computation cycle may be queried in order to obtain any values which
   * were calculated during the cycle, including intermediate values corresponding to inputs as well as terminal output
   * values already present in the result object.
   * 
   * @param computationCacheQuery  the query, not null
   * @return  the result of performing the query against the computation caches, not null
   */
  ComputationCacheResponse queryComputationCaches(ComputationCacheQuery computationCacheQuery);
  
}
