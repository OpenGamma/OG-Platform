/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;

/**
 * Invokes a function on behalf of a client.
 */
public interface FunctionInvoker {

  Result invoke(SessionContext sessionContext, List<Data> parameterValue);

  // TODO: note that implementations of a FunctionInvoker will typically take the
  // MetaParameter list and JavaTypeInfo of the return type so that a conversion
  // chain and default parameter supply can be built

}
