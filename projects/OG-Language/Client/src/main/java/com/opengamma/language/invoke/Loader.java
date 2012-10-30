/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends a context factory to attach a type converter provider.
 */
public class Loader extends ContextInitializationBean {

  private List<TypeConverterProvider> _typeConverterProviders;

  public void setTypeConverterProvider(final TypeConverterProvider typeConverterProvider) {
    ArgumentChecker.notNull(typeConverterProvider, "typeConverterProvider");
    _typeConverterProviders = Collections.singletonList(typeConverterProvider);
  }

  public void setTypeConverterProviders(final Collection<TypeConverterProvider> typeConverterProviders) {
    ArgumentChecker.noNulls(typeConverterProviders, "typeConverterProviders");
    ArgumentChecker.isFalse(typeConverterProviders.isEmpty(), "typeConverterProviders");
    _typeConverterProviders = new ArrayList<TypeConverterProvider>(typeConverterProviders);
  }

  public TypeConverterProvider getTypeConverterProvider() {
    if ((_typeConverterProviders == null) || _typeConverterProviders.isEmpty()) {
      return null;
    } else {
      return _typeConverterProviders.get(0);
    }
  }

  public Collection<TypeConverterProvider> getTypeConverterProviders() {
    return _typeConverterProviders;
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getTypeConverterProviders(), "typeConverterProviders");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    for (TypeConverterProvider typeConverterProvider : getTypeConverterProviders()) {
      globalContext.getTypeConverterProvider().addTypeConverterProvider(typeConverterProvider);
    }
  }

}
