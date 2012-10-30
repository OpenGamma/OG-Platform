/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.MutableUserContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends a context factory to attach a function provider.
 */
public class Loader extends ContextInitializationBean {

  private List<FunctionProvider> _functionProviders;
  private FunctionDefinitionFilter _definitionFilter;

  public void setFunctionProvider(final FunctionProvider functionProvider) {
    ArgumentChecker.notNull(functionProvider, "functionProvider");
    _functionProviders = Collections.singletonList(functionProvider);
  }

  public void setFunctionProviders(final Collection<FunctionProvider> functionProviders) {
    ArgumentChecker.noNulls(functionProviders, "functionProviders");
    ArgumentChecker.isFalse(functionProviders.isEmpty(), "functionProviders");
    _functionProviders = new ArrayList<FunctionProvider>(functionProviders);
  }

  public FunctionProvider getFunctionProvider() {
    if ((_functionProviders == null) || _functionProviders.isEmpty()) {
      return null;
    } else {
      return _functionProviders.get(0);
    }
  }

  public Collection<FunctionProvider> getFunctionProviders() {
    return _functionProviders;
  }

  protected void addProviders(final AggregatingFunctionProvider aggregator) {
    for (FunctionProvider provider : getFunctionProviders()) {
      aggregator.addProvider(provider);
    }
  }

  public FunctionDefinitionFilter getDefinitionFilter() {
    return _definitionFilter;
  }

  public void setDefinitionFilter(final FunctionDefinitionFilter definitionFilter) {
    _definitionFilter = definitionFilter;
  }

  // ContextInitializationBean

  @Override
  protected void initContext(final MutableSessionContext sessionContext) {
    if (getFunctionProviders() != null) {
      addProviders(sessionContext.getFunctionProvider());
    }
  }

  @Override
  protected void initContext(final MutableUserContext userContext) {
    if (getFunctionProviders() != null) {
      addProviders(userContext.getFunctionProvider());
    }
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    if (getFunctionProviders() != null) {
      addProviders(globalContext.getFunctionProvider());
    }
    if (getDefinitionFilter() != null) {
      globalContext.setFunctionDefinitionFilter(getDefinitionFilter());
    }
  }

}
