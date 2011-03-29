/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collection;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;

/**
 * 
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
   * @param calcConfigName  the name of the calculation configuration to query, not null
   * @param specifications  a collection of value specifications describing the values required, not null
   * @return  a collection of pairs associating the requested value specifications with their respective values, not
   *          null
   */
  Collection<Pair<ValueSpecification, Object>> query(String calcConfigName, Collection<ValueSpecification> specifications);
  
}
