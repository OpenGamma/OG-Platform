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
  
  public ViewDefinitionCompiledCall(CompiledViewDefinition compiledViewDefinition) {
    _compiledViewDefinition = compiledViewDefinition;
  }
  
  public CompiledViewDefinition getCompiledViewDefinition() {
    return _compiledViewDefinition;
  }
  
  @Override
  public Object apply(ViewResultListener listener) {
    listener.viewDefinitionCompiled(getCompiledViewDefinition());
    return null;
  }
  
}
