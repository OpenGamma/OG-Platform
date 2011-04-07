/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.util.Collections;
import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.AbstractException;
import com.opengamma.language.invoke.AbstractInvoker;
import com.opengamma.language.invoke.ParameterConverter;
import com.opengamma.language.invoke.ResultConverter;

/**
 * Partial implementation of a {@link FunctionInvoker} that converts the parameters and results using
 * the converters bound to the invoking session context.
 */
public abstract class AbstractFunctionInvoker extends AbstractInvoker implements FunctionInvoker {

  protected AbstractFunctionInvoker(final List<MetaParameter> parameters) {
    super(parameters);
  }

  protected abstract Object invokeImpl(final SessionContext sessionContext, final Object[] parameters);

  // AbstractInvoker

  @Override
  protected ResultConverter getResultConverter(final GlobalContext globalContext) {
    return globalContext.getFunctionResultConverter();
  }

  @Override
  protected ParameterConverter getParameterConverter(final GlobalContext globalContext) {
    return globalContext.getParameterConverter();
  }

  // FunctionInvoker

  @Override
  public final Result invoke(final SessionContext sessionContext, final List<Data> parameterValue) {
    try {
      final Object[] parameters = convertParameters(sessionContext, parameterValue);
      final Object resultObject = invokeImpl(sessionContext, parameters);
      if (resultObject == null) {
        return null;
      }
      final Data resultData = convertResult(sessionContext, resultObject);
      if (resultData == null) {
        return null;
      }
      return new Result(Collections.singleton(resultData));
    } catch (AbstractException e) {
      return new Result(Collections.singleton(DataUtil.of(e.getValue())));
    }
  }

}
