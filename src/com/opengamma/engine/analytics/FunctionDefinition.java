/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import com.opengamma.engine.ComputationTargetType;


/**
 * A single unit of work capable of operating on inputs to produce results. 
 *
 * @author kirk
 */
public interface FunctionDefinition {
  
  /**
   * The unique identifier for an {@code AnalyticFunction} is the handle
   * through which its {@link FunctionInvoker} can be identified
   * from the {@link AnalyticFunctionRepository} which sourced the function.
   * In general, functions will not specify this themselves, but the repository
   * will provide a unique identifier for them.
   * 
   * @return The unique identifier for this function.
   */
  String getUniqueIdentifier();
  
  String getShortName();
  
  boolean buildsOwnSubGraph();
  
  /**
   * While this can be determined by the subgraph, it is provided at this
   * level for ease of programming.
   *  
   * @return
   */
  ComputationTargetType getTargetType();
  

  
}
