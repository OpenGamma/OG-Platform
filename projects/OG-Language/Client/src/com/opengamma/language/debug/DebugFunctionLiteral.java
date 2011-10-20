/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import java.util.Collections;
import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.FunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * Trivial function for debugging. Always returns the same value.
 */
public class DebugFunctionLiteral implements PublishedFunction {

  private final String _name;
  private final Data _literal;

  public DebugFunctionLiteral(final String name, final Data literal) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
    _literal = literal;
  }

  public DebugFunctionLiteral(final String name, final Value literal) {
    this(name, DataUtils.of(literal));
  }

  public DebugFunctionLiteral(final String name, final String literal) {
    this(name, ValueUtils.of(literal));
  }

  public DebugFunctionLiteral(final String name, final int literal) {
    this(name, ValueUtils.of(literal));
  }

  public String getName() {
    return _name;
  }

  public Data getLiteral() {
    return _literal;
  }

  @Override
  public MetaFunction getMetaFunction() {
    final List<MetaParameter> args = Collections.emptyList();
    final FunctionInvoker invoker = new AbstractFunctionInvoker(args) {
      @Override
      public Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
        return getLiteral();
      }
    };
    return new MetaFunction(Categories.DEBUG, getName(), args, invoker);
  }

}
