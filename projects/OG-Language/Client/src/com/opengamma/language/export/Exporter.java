/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import java.util.Collection;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.function.FunctionRepository;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.livedata.LiveDataRepository;
import com.opengamma.language.livedata.MetaLiveData;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.ProcedureRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * Base class for constructing an element export from the OG-Language platform. Exports can
 * be used to, for example, generate stub code for a client language or documentation for
 * the client.
 */
public class Exporter implements Runnable {

  private final SessionContext _sessionContext;
  private RepositoryExporter _exporter;

  public Exporter(final SessionContext sessionContext) {
    ArgumentChecker.notNull(sessionContext, "sessionContext");
    _sessionContext = sessionContext;
  }

  protected SessionContext getSessionContext() {
    return _sessionContext;
  }

  protected void setExporter(final RepositoryExporter exporter) {
    _exporter = exporter;
  }

  protected RepositoryExporter getExporter() {
    return _exporter;
  }

  protected void exportFunctions() {
    final FunctionRepository repository = getSessionContext().getFunctionRepository();
    repository.initialize(getSessionContext().getFunctionProvider(), false);
    final Collection<MetaFunction> functions = repository.getAll().values();
    final FunctionDefinitionExporter exporter = getExporter().getFunctionExporter();
    for (MetaFunction function : functions) {
      exporter.exportFunction(function);
    }
  }

  protected void exportLiveData() {
    final LiveDataRepository repository = getSessionContext().getLiveDataRepository();
    repository.initialize(getSessionContext().getLiveDataProvider(), false);
    final Collection<MetaLiveData> liveDatas = repository.getAll().values();
    final LiveDataDefinitionExporter exporter = getExporter().getLiveDataExporter();
    for (MetaLiveData liveData : liveDatas) {
      exporter.exportLiveData(liveData);
    }
  }

  protected void exportProcedures() {
    final ProcedureRepository repository = getSessionContext().getProcedureRepository();
    repository.initialize(getSessionContext().getProcedureProvider(), false);
    final Collection<MetaProcedure> procedures = repository.getAll().values();
    final ProcedureDefinitionExporter exporter = getExporter().getProcedureExporter();
    for (MetaProcedure procedure : procedures) {
      exporter.exportProcedure(procedure);
    }
  }

  // Runnable

  @Override
  public void run() {
    exportFunctions();
    exportLiveData();
    exportProcedures();
  }

}
