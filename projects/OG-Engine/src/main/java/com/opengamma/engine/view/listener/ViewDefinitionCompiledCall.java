/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 * Represents a call to {@link ViewResultListener#viewDefinitionCompiled(CompiledViewDefinition)}.
 */
public class ViewDefinitionCompiledCall implements Function<ViewResultListener, Object> {

  private final CompiledViewDefinition _compiledViewDefinition;
  private final boolean _hasMarketDataPermissions;
  
  public ViewDefinitionCompiledCall(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    _compiledViewDefinition = compiledViewDefinition;
    _hasMarketDataPermissions = hasMarketDataPermissions;
  }
  
  public CompiledViewDefinition getCompiledViewDefinition() {
    return _compiledViewDefinition;
  }
  
  public boolean hasMarketDataPermissions() {
    return _hasMarketDataPermissions;
  }
  
  @Override
  public Object apply(ViewResultListener listener) {
    listener.viewDefinitionCompiled(getCompiledViewDefinition(), hasMarketDataPermissions());
    return null;
  }
  
}
