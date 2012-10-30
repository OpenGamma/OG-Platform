/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Handler for asynchronous values from a live data component.
 */
public interface LiveDataDispatcher {

  /**
   * Causes a value to eventually be dispatched to the client or notification of the end
   * of the result stream.
   * 
   * @param context the session context
   * @param identifier the identifier of the connection producing the value
   * @param value the value produced, or null if the connection is canceled
   */
  void dispatchValue(SessionContext context, int identifier, Data value);

  /**
   * Creates the result for the initial connect message. This hook is to allow the dispatcher
   * to block the caller for a brief period or perhaps until the first result is available.
   * 
   * @param context the session context
   * @param identifier the identifier of the connection
   * @param result the initial value (may be null if there is no initial result)
   * @return the result message
   */
  Result createResult(SessionContext context, int identifier, Data result) throws AsynchronousExecution;

}
