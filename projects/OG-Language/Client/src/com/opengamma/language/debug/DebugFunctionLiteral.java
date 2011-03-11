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
    this(name, DataUtil.of(literal));
  }

  public DebugFunctionLiteral(final String name, final String literal) {
    this(name, ValueUtil.of(literal));
  }

  public DebugFunctionLiteral(final String name, final int literal) {
    this(name, ValueUtil.of(literal));
  }

  public String getName() {
    return _name;
  }

  public Data getLiteral() {
    return _literal;
  }

  @Override
  public MetaFunction getMetaFunction() {
    final MetaFunction metaFunction = new MetaFunction(getName(), new FunctionInvoker() {
      @Override
      public Data invoke(SessionContext sessionContext, Data[] parameters) {
        return getLiteral();
      }
    });
    return metaFunction;
  }

}
