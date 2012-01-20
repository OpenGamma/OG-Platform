/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import com.opengamma.language.function.Definition;

/**
 * No-op implementation of {@link FunctionDefinitionExporter}.
 */
public class DummyFunctionDefinitionExporter implements FunctionDefinitionExporter {

  @Override
  public void exportFunction(final Definition definition) {
    // No-op
  }

}
