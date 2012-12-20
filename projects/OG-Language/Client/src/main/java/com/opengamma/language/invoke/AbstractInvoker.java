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
import com.opengamma.language.error.InvokeResultConversionException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.livedata.AbstractLiveDataConnector;
import com.opengamma.language.procedure.AbstractProcedureInvoker;

/**
 * Base implementation for {@link AbstractFunctionInvoker}, {@link AbstractLiveDataConnector} and {@link AbstractProcedureInvoker}.
 */
public abstract class AbstractInvoker {

  private static final Object[] NO_PARAMETERS = new Object[0];

  private final List<MetaParameter> _parameters;

  /**
   * Constructs the invoker.
   * 
   * @param parameters  the invocation parameters, or null for no parameters
   */
  protected AbstractInvoker(final List<MetaParameter> parameters) {
    if ((parameters == null) || parameters.isEmpty()) {
      _parameters = Collections.emptyList();
    } else {
      _parameters = new ArrayList<MetaParameter>(parameters);
    }
  }

  protected List<MetaParameter> getParameters() {
    return _parameters;
  }

  protected abstract ResultConverter getResultConverter(final GlobalContext globalContext);

  protected Data convertResult(final SessionContext sessionContext, final Object result) {
    final ResultConverter converter = getResultConverter(sessionContext.getGlobalContext());
    try {
      return converter.convertResult(sessionContext, result);
    } catch (InvalidConversionException e) {
      throw new InvokeResultConversionException(e.getClientMessage());
    }
  }

  protected Data[] convertResults(final SessionContext sessionContext, final Object[] results) {
    final ResultConverter converter = getResultConverter(sessionContext.getGlobalContext());
    final Data[] result = new Data[results.length];
    for (int i = 0; i < results.length; i++) {
      try {
        result[i] = converter.convertResult(sessionContext, results[i]);
      } catch (InvalidConversionException e) {
        throw new InvokeResultConversionException(i, e.getClientMessage());
      }
    }
    return result;
  }

  protected abstract ParameterConverter getParameterConverter(final GlobalContext globalContext);

  protected Object[] convertParameters(final SessionContext sessionContext, final List<Data> parameters) {
    if ((parameters != null) && !parameters.isEmpty()) {
      final ParameterConverter converter = getParameterConverter(sessionContext.getGlobalContext());
      return converter.convertParameters(sessionContext, parameters, getParameters());
    } else {
      return NO_PARAMETERS;
    }
  }

}
