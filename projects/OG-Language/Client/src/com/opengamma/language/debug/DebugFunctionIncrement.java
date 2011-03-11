/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtil;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.function.FunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Trivial function for debugging. Takes a single value and returns the incremented value.
 */
public class DebugFunctionIncrement implements PublishedFunction {

  private Value execute(final Value parameter) {
    if (parameter.getDoubleValue() != null) {
      return ValueUtil.of(parameter.getDoubleValue() + 1);
    } else if (parameter.getIntValue() != null) {
      return ValueUtil.of(parameter.getIntValue() + 1);
    } else {
      throw new IllegalArgumentException("invalid parameter " + parameter);
    }
  }

  private Value[] execute(final Value[] parameter) {
    for (int i = 0; i < parameter.length; i++) {
      parameter[i] = execute(parameter[i]);
    }
    return parameter;
  }

  private Value[][] execute(final Value[][] parameter) {
    for (int i = 0; i < parameter.length; i++) {
      parameter[i] = execute(parameter[i]);
    }
    return parameter;
  }

  private Data execute(final Data parameter) {
    if (parameter.getSingle() != null) {
      return DataUtil.of(execute(parameter.getSingle()));
    } else if (parameter.getLinear() != null) {
      return DataUtil.of(execute(parameter.getLinear()));
    } else if (parameter.getMatrix() != null) {
      return DataUtil.of(execute(parameter.getMatrix()));
    } else {
      throw new IllegalArgumentException("Expected single, linear or matrix");
    }
  }

  @Override
  public MetaFunction getMetaFunction() {
    final MetaFunction metaFunction = new MetaFunction("DebugFunctionIncrement", new FunctionInvoker() {

      @Override
      public Data invoke(SessionContext sessionContext, Data[] parameters) {
        if (parameters.length != 1) {
          throw new IllegalArgumentException("Wrong number of parameters");
        }
        return execute(parameters[0]);
      }

    });
    // TODO: change to a MetaFunctionBuilder (like in Excel)
    return metaFunction;
  }

}
