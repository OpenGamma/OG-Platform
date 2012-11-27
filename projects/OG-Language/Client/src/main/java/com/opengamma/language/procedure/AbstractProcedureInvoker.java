/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.procedure;

import java.util.Collections;
import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.AbstractException;
import com.opengamma.language.error.Constants;
import com.opengamma.language.invoke.AbstractInvoker;
import com.opengamma.language.invoke.ParameterConverter;
import com.opengamma.language.invoke.ResultConverter;
import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of a {@link ProcedureInvoker} that converts the parameters using the converters bound to the invoking
 * session context.
 */
public abstract class AbstractProcedureInvoker extends AbstractInvoker implements ProcedureInvoker {

  /**
   * Implementation for procedures that do not return a result.
   */
  public abstract static class NoResult extends AbstractProcedureInvoker {

    protected NoResult(final List<MetaParameter> parameters) {
      super(parameters);
    }

    protected abstract void invokeImpl(SessionContext sessionContext, Object[] parameters);

    // AbstractProcedureInvoker

    @Override
    protected void invoke(final Result result, final SessionContext sessionContext, final Object[] parameters) {
      invokeImpl(sessionContext, parameters);
    }

  }

  /**
   * Implementation for procedures that return a single result.
   */
  public abstract static class SingleResult extends AbstractProcedureInvoker {

    protected SingleResult(final List<MetaParameter> parameters) {
      super(parameters);
    }

    protected abstract Object invokeImpl(SessionContext sessionContext, Object[] parameters);

    // AbstractProcedureInvoker

    @Override
    protected void invoke(final Result result, final SessionContext sessionContext, final Object[] parameters) {
      result.addResult(convertResult(sessionContext, invokeImpl(sessionContext, parameters)));
    }

  }

  /**
   * Implementation for procedures that return multiple results.
   */
  public abstract static class MultipleResult extends AbstractProcedureInvoker {

    private final int _resultCount;

    protected MultipleResult(final List<MetaParameter> parameters, final int resultCount) {
      super(parameters);
      ArgumentChecker.notNegative(resultCount, "resultCount");
      _resultCount = resultCount;
    }

    protected abstract Object[] invokeImpl(SessionContext sessionContext, Object[] parameters);

    // AbstractProcedureInvoker

    @Override
    protected void invoke(final Result result, final SessionContext sessionContext, final Object[] parameters) {
      final Object[] results = invokeImpl(sessionContext, parameters);
      final int resultsLength = (results != null) ? results.length : 0;
      if (resultsLength != _resultCount) {
        final Value err = ValueUtils.ofError(Constants.ERROR_INTERNAL);
        err.setStringValue("Invalid number of results - expected " + _resultCount + ", got " + resultsLength);
        result.addResult(DataUtils.of(err));
      } else {
        if (resultsLength > 0) {
          for (Object resultValue : results) {
            result.addResult(convertResult(sessionContext, resultValue));
          }
        }
      }
    }

  }

  protected AbstractProcedureInvoker(final List<MetaParameter> parameters) {
    super(parameters);
  }

  @Override
  protected List<MetaParameter> getParameters() {
    return super.getParameters();
  }

  protected abstract void invoke(Result result, SessionContext sessionContext, Object[] parameters);

  // AbstractInvoker

  @Override
  protected ResultConverter getResultConverter(final GlobalContext globalContext) {
    return globalContext.getFunctionResultConverter();
  }

  @Override
  protected ParameterConverter getParameterConverter(final GlobalContext globalContext) {
    return globalContext.getProcedureParameterConverter();
  }

  // FunctionInvoker

  @Override
  public final Result invoke(final SessionContext sessionContext, final List<Data> parameterValue) {
    try {
      final Object[] parameters = convertParameters(sessionContext, parameterValue);
      final Result result = new Result();
      invoke(result, sessionContext, parameters);
      return result;
    } catch (AbstractException e) {
      return new Result(Collections.singleton(DataUtils.of(e.getValue())));
    }
  }

}
