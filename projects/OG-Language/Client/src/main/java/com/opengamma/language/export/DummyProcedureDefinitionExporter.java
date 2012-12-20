/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import com.opengamma.language.procedure.Definition;

/**
 * No-op implementation of {@link ProcedureDefinitionExporter}.
 */
public class DummyProcedureDefinitionExporter implements ProcedureDefinitionExporter {

  @Override
  public void exportProcedure(final Definition definition) {
    // No-op
  }

}
