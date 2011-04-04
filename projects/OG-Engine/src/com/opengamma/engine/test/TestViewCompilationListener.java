/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.ViewDefinitionCompilationListener;

/**
 * Test implementation of {@link ViewDefinitionCompilationListener}.
 */
public class TestViewCompilationListener extends AbstractTestResultListener<CompiledViewDefinition> implements ViewDefinitionCompilationListener {

  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition) {
    resultReceived(compiledViewDefinition);
  }

}
