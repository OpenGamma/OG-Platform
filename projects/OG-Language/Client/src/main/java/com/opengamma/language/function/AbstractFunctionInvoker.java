/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.util.Collections;
import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.Constants;
import com.opengamma.language.invoke.AbstractInvoker;
import com.opengamma.language.invoke.ParameterConverter;
import com.opengamma.language.invoke.ResultConverter;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.async.AsynchronousOperation;
import com.opengamma.util.async.AsynchronousResult;
import com.opengamma.util.async.ResultCallback;
import com.opengamma.util.async.ResultListener;

/**
 * Partial implementation of a {@link FunctionInvoker} that converts the parameters and results using
 * the converters bound to the invoking session context.
 */
public abstract class AbstractFunctionInvoker extends AbstractInvoker implements FunctionInvoker {

  protected AbstractFunctionInvoker(final List<MetaParameter> parameters) {
    super(parameters);
  }

  @Override
  protected List<MetaParameter> getParameters() {
    return super.getParameters();
  }

  protected abstract Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) throws AsynchronousExecution;

  private Result invokeResult(final SessionContext sessionContext, final Object resultObject) {
    if (resultObject == null) {
      return new Result(Collections.singleton(new Data()));
    }
    final Data resultData = convertResult(sessionContext, resultObject);
    if (resultData == null) {
      // This is a fault - non-null should not have been converted to null
      final Value err = ValueUtils.ofError(Constants.ERROR_INTERNAL);
      err.setStringValue("Conversion of non-null '" + resultObject.toString() + "' should not have given null");
      return new Result(Collections.singleton(DataUtils.of(err)));
    }
    return new Result(Collections.singleton(resultData));
  }

  // AbstractInvoker

  @Override
  protected ResultConverter getResultConverter(final GlobalContext globalContext) {
    return globalContext.getFunctionResultConverter();
  }

  @Override
  protected ParameterConverter getParameterConverter(final GlobalContext globalContext) {
    return globalContext.getFunctionParameterConverter();
  }

  // FunctionInvoker

  @Override
  public final Result invoke(final SessionContext sessionContext, final List<Data> parameterValue) throws AsynchronousExecution {
    final Object[] parameters = convertParameters(sessionContext, parameterValue);
    final Object resultObject;
    try {
      resultObject = invokeImpl(sessionContext, parameters);
    } catch (final AsynchronousExecution e) {
      final AsynchronousOperation<Result> async = AsynchronousOperation.create(Result.class);
      final ResultCallback<Result> asyncResult = async.getCallback();
      e.setResultListener(new ResultListener<Object>() {
        @Override
        public void operationComplete(final AsynchronousResult<Object> result) {
          try {
            final Object resultObject = result.getResult();
            asyncResult.setResult(invokeResult(sessionContext, resultObject));
          } catch (final RuntimeException e) {
            asyncResult.setException(e);
          }
        }
      });
      return async.getResult();
    }
    return invokeResult(sessionContext, resultObject);
  }

}
