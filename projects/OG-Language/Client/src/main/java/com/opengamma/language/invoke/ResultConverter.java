/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;

/**
 * Converts an invocation result back to a transport type for the client.
 */
public interface ResultConverter {

  Data convertResult(SessionContext sessionContext, Object result);

}
