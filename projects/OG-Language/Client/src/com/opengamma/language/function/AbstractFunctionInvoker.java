/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.Value;
import com.opengamma.language.context.SessionContext;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Partial implementation of a {@link FunctionInvoker}.
 */
public abstract class AbstractFunctionInvoker implements FunctionInvoker {

  protected abstract Object invokeImpl(final SessionContext sessionContext, final Object[] parameters);

  protected Object convertParameterImpl(final SessionContext sessionContext, final Data parameter) {
    return parameter;
  }

  protected Object convertParameterImpl(final SessionContext sessionContext, final int index, final Data parameter) {
    return convertParameterImpl(sessionContext, parameter);
  }

  protected Data convertResultImpl(final SessionContext sessionContext, final Object result) {
    if (result == null) {
      return null;
    } else if (result instanceof Data) {
      return (Data) result;
    } else if (result instanceof Value) {
      return DataUtil.of((Value) result);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @SuppressWarnings("unchecked")
  protected Result convertResult(final SessionContext sessionContext, final Object result) {
    final Data dataResult = convertResultImpl(sessionContext, result);
    return new Result(Collections.singleton(dataResult));
  }

  protected Object[] convertParameters(final SessionContext sessionContext, final List<Data> parameters) {
    final Object[] result = new Object[parameters.size()];
    for (int i = 0; i < parameters.size(); i++) {
      result[i] = convertParameterImpl(sessionContext, i, parameters.get(i));
    }
    return result;
  }

  // TODO: abstract parameter and result conversion logics into external interfaces as they will need to be shared
  // with procedure and live data invocations. The instances for convertParameter and convertResult will be pulled
  // from the session context

  // TODO: i.e. ParameterConverter (take MetaParameter list)
  // TODO: i.e. ResultConverter (take JavaTypeInfo for result)
  // TODO: i.e. fundamental DataToObject converter (extensible/replaceable)
  // TODO: i.e. fundamental ObjectToData converter (extensible/replaceable)

  // FunctionInvoker

  @Override
  public final Result invoke(final SessionContext sessionContext, final List<Data> parameterValue) {
    final Object[] parameters = convertParameters(sessionContext, parameterValue);
    final Object result = invokeImpl(sessionContext, parameters);
    return (result != null) ? convertResult(sessionContext, result) : null;
  }

}
