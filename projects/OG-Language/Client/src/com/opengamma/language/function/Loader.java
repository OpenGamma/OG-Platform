/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends a context factory to attach a function provider.
 */
public class Loader extends ContextInitializationBean {

  private FunctionProvider _functions;

  public void setFunctions(final FunctionProvider functions) {
    ArgumentChecker.notNull(functions, "functions");
    _functions = functions;
  }

  public FunctionProvider getFunctions() {
    return _functions;
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getFunctions(), "functions");
  }

  @Override
  protected void initContext(final MutableSessionContext sessionContext) {
    sessionContext.getFunctionProvider().addProvider(getFunctions());
  }

  @Override
  protected void initContext(final MutableUserContext userContext) {
    userContext.getFunctionProvider().addProvider(getFunctions());
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    globalContext.getFunctionProvider().addProvider(getFunctions());
  }

}
