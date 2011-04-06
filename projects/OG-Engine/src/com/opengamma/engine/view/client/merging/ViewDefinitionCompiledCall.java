/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import com.google.common.base.Function;
import com.opengamma.engine.view.ViewProcessListener;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 * Represents a call to {@link ViewProcessListener#viewDefinitionCompiled(CompiledViewDefinition)}.
 */
public class ViewDefinitionCompiledCall implements Function<ViewProcessListener, Object> {

  private final CompiledViewDefinition _compiledViewDefinition;
  
  public ViewDefinitionCompiledCall(CompiledViewDefinition compiledViewDefinition) {
    _compiledViewDefinition = compiledViewDefinition;
  }
  
  @Override
  public Object apply(ViewProcessListener listener) {
    listener.viewDefinitionCompiled(_compiledViewDefinition);
    return null;
  }
  
}
