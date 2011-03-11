/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;

/**
 * Invokes a function on behalf of a client.
 */
public interface FunctionInvoker {

  Data invoke(SessionContext sessionContext, Data[] parameters);

}
