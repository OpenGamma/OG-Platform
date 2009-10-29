/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;


// REVIEW kirk 2009-09-22 -- This is getting REALLY large and unwieldy. We need to
// segregate this out into various facets for different types of functions I think.

/**
 * A single unit of work capable of operating on inputs to produce results. 
 *
 * @author kirk
 */
public interface AnalyticFunctionDefinition {
  
  /**
   * The unique identifier for an {@code AnalyticFunction} is the handle
   * through which its {@link AnalyticFunctionInvoker} can be identified
   * from the {@link AnalyticFunctionRepository} which sourced the function.
   * In general, functions will not specify this themselves, but the repository
   * will provide a unique identifier for them.
   * 
   * @return The unique identifier for this function.
   */
  String getUniqueIdentifier();
  
  String getShortName();
  
  boolean buildsOwnSubGraph();
  

  
}
