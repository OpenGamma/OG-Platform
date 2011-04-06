/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends a context factory to attach a type converter provider.
 */
public class Loader extends ContextInitializationBean {

  private TypeConverterProvider _typeConverters;

  public void setTypeConverters(final TypeConverterProvider typeConverters) {
    _typeConverters = typeConverters;
  }

  public TypeConverterProvider getTypeConverters() {
    return _typeConverters;
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getTypeConverters(), "typeConverters");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    globalContext.getTypeConverterProvider().addTypeConverterProvider(getTypeConverters());
  }

}
