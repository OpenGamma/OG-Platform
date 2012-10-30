/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Invokes a procedure on behalf of a client.
 */
public interface ProcedureInvoker {

  /**
   * Invokes the procedure.
   * 
   * @param sessionContext the client session context, not null
   * @param parameters parameters as received from the client, may be null or empty
   * @throws AsynchronousExecution if the operation is deferred
   * @return the result message containing any result produced by the procedure 
   */
  Result invoke(SessionContext sessionContext, List<Data> parameters) throws AsynchronousExecution;

}
