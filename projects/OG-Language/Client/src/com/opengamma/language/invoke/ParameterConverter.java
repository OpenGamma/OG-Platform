/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;

/**
 * Converts a set of parameters received from a client into the correct types for
 * an invocation target.
 */
public interface ParameterConverter {

  Object[] convertParameters(SessionContext sessionContext, List<Data> clientParameters,
      List<MetaParameter> targetParameters);

}
