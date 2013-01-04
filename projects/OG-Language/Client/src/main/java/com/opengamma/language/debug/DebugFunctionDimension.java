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
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.FunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Trivial function for debugging. Returns a Data containing a single, linear, or matrix of constants.
 */
public class DebugFunctionDimension implements PublishedFunction {

  private Data execute(final Integer x, final Integer y) {
    int i = 0;
    if (x == null) {
      if (y == null) {
        return DataUtils.of(0);
      } else {
        throw new InvokeInvalidArgumentException(1, "Can't specify Y without X");
      }
    } else {
      if (y == null) {
        final Value[] v = new Value[x];
        for (int j = 0; j < v.length; j++) {
          v[j] = ValueUtils.of(i++);
        }
        return DataUtils.of(v);
      } else {
        final Value[][] v = new Value[y][x];
        for (int j = 0; j < v.length; j++) {
          for (int k = 0; k < v[j].length; k++) {
            v[j][k] = ValueUtils.of(i++);
          }
        }
        return DataUtils.of(v);
      }
    }
  }

  @Override
  public MetaFunction getMetaFunction() {
    final List<MetaParameter> args = Arrays.asList(new MetaParameter("x", JavaTypeInfo.builder(Integer.class).defaultValue(null).get()), new MetaParameter("y", JavaTypeInfo.builder(Integer.class)
        .defaultValue(null).get()));
    final FunctionInvoker invoker = new AbstractFunctionInvoker(args) {
      @Override
      public Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
        return execute((Integer) parameters[0], (Integer) parameters[1]);
      }
    };
    return new MetaFunction(Categories.DEBUG, "DebugFunctionDimension", args, invoker);
  }
}
