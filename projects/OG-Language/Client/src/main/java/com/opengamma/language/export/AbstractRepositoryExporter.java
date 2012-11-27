/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of {@link RepositoryExporter}
 */
public class AbstractRepositoryExporter extends DefaultRepositoryExporter implements DefinitionExporter, FunctionDefinitionExporter, LiveDataDefinitionExporter, ProcedureDefinitionExporter {

  private final DefinitionExporter _exporter;

  public AbstractRepositoryExporter(final DefinitionExporter exporter) {
    super();
    ArgumentChecker.notNull(exporter, "exporter");
    setFunctionExporter(this);
    setLiveDataExporter(this);
    setProcedureExporter(this);
    _exporter = exporter;
  }

  private DefinitionExporter getExporter() {
    return _exporter;
  }

  @Override
  public final void export(final com.opengamma.language.definition.Definition definition, final String text) {
    if (text != null) {
      getExporter().export(definition, text);
    }
  }

  protected String getDefinitionText(final com.opengamma.language.definition.Definition definition) {
    // No-op
    return null;
  }

  protected String getFunctionText(final com.opengamma.language.function.Definition definition) {
    return getDefinitionText(definition);
  }

  protected String getLiveDataText(final com.opengamma.language.livedata.Definition definition) {
    return getDefinitionText(definition);
  }

  protected String getProcedureText(final com.opengamma.language.procedure.Definition definition) {
    return getDefinitionText(definition);
  }

  @Override
  public final void exportProcedure(final com.opengamma.language.procedure.Definition definition) {
    export(definition, getProcedureText(definition));
  }

  @Override
  public final void exportLiveData(final com.opengamma.language.livedata.Definition definition) {
    export(definition, getLiveDataText(definition));
  }

  @Override
  public final void exportFunction(final com.opengamma.language.function.Definition definition) {
    export(definition, getFunctionText(definition));
  }

}
