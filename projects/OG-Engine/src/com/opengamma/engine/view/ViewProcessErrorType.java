/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

/**
 * Enumerates the types of errors which can occur in a view process.
 */
public enum ViewProcessErrorType {

  /**
   * The view definition failed to compile. This is normally a fatal error.
   */
  VIEW_DEFINITION_COMPILE_ERROR,
  
  // REVIEW jonathan 2011-04-06 -- I think view cycle errors should be represented in the result model since the
  // execution parameters, such as the valuation time or live data snapshot time, may be relevant to the failure. There
  // would just be an empty set of results and possibly an exception field and/or error flag set.
  /**
   * A view cycle failed to execute. Normally the process will attempt to continue with future cycles.
   */
  EXECUTE_VIEW_CYCLE_ERROR,
  
  /**
   * The view process was terminated, normally by an administrator. No further results should be expected.
   */
  VIEW_PROCESS_TERMINATED
  
}
