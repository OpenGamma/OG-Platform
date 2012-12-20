/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import java.util.Arrays;
import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.FunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Trivial function for debugging. Takes a single value and returns the incremented value.
 */
public class DebugFunctionIncrement implements PublishedFunction {

  private Value execute(final Value parameter) {
    if (parameter.getDoubleValue() != null) {
      return ValueUtils.of(parameter.getDoubleValue() + 1);
    } else if (parameter.getIntValue() != null) {
      return ValueUtils.of(parameter.getIntValue() + 1);
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
      return DataUtils.of(execute(parameter.getSingle()));
    } else if (parameter.getLinear() != null) {
      return DataUtils.of(execute(parameter.getLinear()));
    } else if (parameter.getMatrix() != null) {
      return DataUtils.of(execute(parameter.getMatrix()));
    } else {
      throw new IllegalArgumentException("Expected single, linear or matrix");
    }
  }

  @Override
  public MetaFunction getMetaFunction() {
    final List<MetaParameter> args = Arrays.asList(new MetaParameter("x", JavaTypeInfo.builder(Data.class).get()));
    final FunctionInvoker invoker = new AbstractFunctionInvoker(args) {
      @Override
      public Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
        if (parameters.length != 1) {
          throw new IllegalArgumentException("Wrong number of parameters");
        }
        return execute((Data) parameters[0]);
      }
    };
    return new MetaFunction(Categories.DEBUG, "DebugFunctionIncrement", args, invoker);
  }

}
