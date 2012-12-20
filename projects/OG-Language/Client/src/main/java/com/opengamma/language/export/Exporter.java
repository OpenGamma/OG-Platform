/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.export;

import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.function.FunctionDefinitionFilter;
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

  /**
   * Returns the unfiltered definitions.
   * 
   * @param repository the repository for the basic definitions
   * @return the definitions
   */
  protected Collection<MetaFunction> getRawFunctions(final FunctionRepository repository) {
    return repository.getAll().values();
  }

  /**
   * Returns the filtered definitions.
   * 
   * @param repository the repository for the basic definitions
   * @param filter the definition filter
   * @return the filtered definitions
   */
  protected Collection<com.opengamma.language.function.Definition> getFilteredFunctions(final FunctionRepository repository, final FunctionDefinitionFilter filter) {
    final Collection<MetaFunction> raw = getRawFunctions(repository);
    final Collection<com.opengamma.language.function.Definition> functions = new ArrayList<com.opengamma.language.function.Definition>(raw.size());
    for (MetaFunction function : raw) {
      final com.opengamma.language.function.Definition export = filter.createDefinition(function);
      if (export != null) {
        functions.add(export);
      }
    }
    return functions;
  }

  protected void exportFunctions() {
    final FunctionRepository repository = getSessionContext().getFunctionRepository();
    repository.initialize(getSessionContext().getFunctionProvider(), false);
    final FunctionDefinitionExporter exporter = getExporter().getFunctionExporter();
    for (com.opengamma.language.function.Definition export : getFilteredFunctions(repository, getSessionContext().getGlobalContext().getFunctionDefinitionFilter())) {
      exporter.exportFunction(export);
    }
  }

  // TODO: the pattern for functions on live data and procedure

  protected Collection<MetaLiveData> getLiveData(final LiveDataRepository repository) {
    return repository.getAll().values();
  }

  protected void exportLiveData() {
    final LiveDataRepository repository = getSessionContext().getLiveDataRepository();
    repository.initialize(getSessionContext().getLiveDataProvider(), false);
    final Collection<MetaLiveData> liveDatas = getLiveData(repository);
    final LiveDataDefinitionExporter exporter = getExporter().getLiveDataExporter();
    for (MetaLiveData liveData : liveDatas) {
      final com.opengamma.language.livedata.Definition export = getSessionContext().getGlobalContext().getLiveDataDefinitionFilter().createDefinition(liveData);
      if (export != null) {
        exporter.exportLiveData(export);
      }
    }
  }

  protected Collection<MetaProcedure> getProcedures(final ProcedureRepository repository) {
    return repository.getAll().values();
  }

  protected void exportProcedures() {
    final ProcedureRepository repository = getSessionContext().getProcedureRepository();
    repository.initialize(getSessionContext().getProcedureProvider(), false);
    final Collection<MetaProcedure> procedures = getProcedures(repository);
    final ProcedureDefinitionExporter exporter = getExporter().getProcedureExporter();
    for (MetaProcedure procedure : procedures) {
      final com.opengamma.language.procedure.Definition export = getSessionContext().getGlobalContext().getProcedureDefinitionFilter().createDefinition(procedure);
      if (export != null) {
        exporter.exportProcedure(export);
      }
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
