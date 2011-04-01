/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;

/**
 * Base implementation for {@link AbstractFunctionInvoker}, {@link AbstractLiveDataInvoker} and {@link AbstractProcedureInvoker}.
 */
public abstract class AbstractInvoker {

  private final List<MetaParameter> _parameters;

  /**
   * Constructs the invoker.
   * 
   * @param parameters the invocation parameters, or {@code null} for no parameters
   */
  protected AbstractInvoker(final List<MetaParameter> parameters) {
    if ((parameters == null) || parameters.isEmpty()) {
      _parameters = Collections.emptyList();
    } else {
      _parameters = new ArrayList<MetaParameter>(parameters);
    }
  }

  protected abstract ResultConverter getResultConverter(final GlobalContext globalContext);

  protected Data convertResult(final SessionContext sessionContext, final Object result) {
    final ResultConverter converter = getResultConverter(sessionContext.getGlobalContext());
    return converter.convertResult(sessionContext, result);
  }

  protected Data[] convertResults(final SessionContext sessionContext, final Object[] results) {
    final ResultConverter converter = getResultConverter(sessionContext.getGlobalContext());
    final Data[] result = new Data[results.length];
    for (int i = 0; i < results.length; i++) {
      result[i] = converter.convertResult(sessionContext, results[i]);
    }
    return result;
  }

  protected abstract ParameterConverter getParameterConverter(final GlobalContext globalContext);

  protected Object[] convertParameters(final SessionContext sessionContext, final List<Data> parameters) {
    final ParameterConverter converter = getParameterConverter(sessionContext.getGlobalContext());
    return converter.convertParameters(sessionContext, parameters, _parameters);
  }

}
