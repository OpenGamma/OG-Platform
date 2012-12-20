/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link RepositoryExporter}
 */
public class DefaultRepositoryExporter implements RepositoryExporter {

  private FunctionDefinitionExporter _functionExporter = new DummyFunctionDefinitionExporter();
  private LiveDataDefinitionExporter _liveDataExporter = new DummyLiveDataDefinitionExporter();
  private ProcedureDefinitionExporter _procedureExporter = new DummyProcedureDefinitionExporter();

  public void setFunctionExporter(final FunctionDefinitionExporter functionExporter) {
    ArgumentChecker.notNull(functionExporter, "functionExporter");
    _functionExporter = functionExporter;
  }

  @Override
  public FunctionDefinitionExporter getFunctionExporter() {
    return _functionExporter;
  }

  public void setLiveDataExporter(final LiveDataDefinitionExporter liveDataExporter) {
    ArgumentChecker.notNull(liveDataExporter, "liveDataExporter");
    _liveDataExporter = liveDataExporter;
  }

  @Override
  public LiveDataDefinitionExporter getLiveDataExporter() {
    return _liveDataExporter;
  }

  public void setProcedureExporter(final ProcedureDefinitionExporter procedureExporter) {
    ArgumentChecker.notNull(procedureExporter, "procedureExporter");
    _procedureExporter = procedureExporter;
  }

  @Override
  public ProcedureDefinitionExporter getProcedureExporter() {
    return _procedureExporter;
  }

}
