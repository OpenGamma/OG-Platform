/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import com.opengamma.engine.view.compilation.ViewDefinitionCompilationListener;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;

/**
 * Test implementation of {@link ViewDefinitionCompilationListener}.
 */
public class TestViewCompilationListener extends AbstractTestResultListener<CompiledViewDefinitionImpl> implements ViewDefinitionCompilationListener {

  @Override
  public void viewDefinitionCompiled(CompiledViewDefinitionImpl compiledViewDefinition) {
    resultReceived(compiledViewDefinition);
  }

}
