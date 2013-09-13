/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter.Format;

/**
 * {@link AnalyticsView} that decorates another view and logs the time taken to execute every method call. Intended
 * to help track down a performance regression.
 */
/* package */ class TimingAnalyticsView implements AnalyticsView {

  private static final Logger s_logger = LoggerFactory.getLogger(TimingAnalyticsView.class);

  private final AnalyticsView _delegate;

  /* package */ TimingAnalyticsView(AnalyticsView delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public List<String> updateStructure(CompiledViewDefinition compiledViewDefinition, Portfolio portfolio) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.updateStructure");
    List<String> retVal = _delegate.updateStructure(compiledViewDefinition, portfolio);
    s_logger.trace("updateStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public String viewCompilationFailed(Throwable t) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.viewCompilationFailed");
    String retVal = _delegate.viewCompilationFailed(t);
    s_logger.trace("viewCompilationFailed completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<String> updateResults(ViewResultModel results,
                                    ViewCycle viewCycle) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.updateResults");
    List<String> retVal = _delegate.updateResults(results, viewCycle);
    s_logger.trace("updateResults completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int viewportId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getGridStructure");
    GridStructure retVal = _delegate.getGridStructure(gridType, viewportId);
    s_logger.trace("getGridStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public GridStructure getInitialGridStructure(GridType gridType) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getGridStructure");
    GridStructure retVal = _delegate.getInitialGridStructure(gridType);
    s_logger.trace("getGridStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public boolean createViewport(int requestId,
                                GridType gridType,
                                int viewportId,
                                String callbackId,
                                String structureCallbackId,
                                ViewportDefinition viewportDefinition) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.createViewport");
    boolean retVal = _delegate.createViewport(requestId, gridType, viewportId, callbackId, structureCallbackId, viewportDefinition);
    s_logger.trace("createViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public String updateViewport(GridType gridType,
                               int viewportId,
                               ViewportDefinition viewportDefinition) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.updateViewport");
    String retVal = _delegate.updateViewport(gridType, viewportId, viewportDefinition);
    s_logger.trace("updateViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void deleteViewport(GridType gridType, int viewportId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.deleteViewport");
    _delegate.deleteViewport(gridType, viewportId);
    s_logger.trace("deleteViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public ViewportResults getData(GridType gridType, int viewportId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getData");
    ViewportResults retVal = _delegate.getData(gridType, viewportId);
    s_logger.trace("getData completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void openDependencyGraph(int requestId,
                                  GridType gridType,
                                  int graphId, String callbackId, int row, int col) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.openDependencyGraph");
    _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, row, col);
    s_logger.trace("openDependencyGraph completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public void openDependencyGraph(int requestId,
                                  GridType gridType,
                                  int graphId,
                                  String callbackId,
                                  String calcConfigName,
                                  ValueRequirement valueRequirement) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.openDependencyGraph");
    _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, calcConfigName, valueRequirement);
    s_logger.trace("openDependencyGraph completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public void closeDependencyGraph(GridType gridType, int graphId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.closeDependencyGraph");
    _delegate.closeDependencyGraph(gridType, graphId);
    s_logger.trace("closeDependencyGraph completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int graphId, int viewportId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getGridStructure");
    GridStructure retVal = _delegate.getGridStructure(gridType, graphId, viewportId);
    s_logger.trace("getGridStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public GridStructure getInitialGridStructure(GridType gridType, int graphId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getGridStructure");
    GridStructure retVal = _delegate.getInitialGridStructure(gridType, graphId);
    s_logger.trace("getGridStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public boolean createViewport(int requestId,
                                GridType gridType,
                                int graphId,
                                int viewportId,
                                String callbackId,
                                String structureCallbackId,
                                ViewportDefinition viewportDefinition) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.createViewport");
    boolean retVal = _delegate.createViewport(requestId,
                                              gridType,
                                              graphId,
                                              viewportId,
                                              callbackId,
                                              structureCallbackId,
                                              viewportDefinition);
    s_logger.trace("createViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public String updateViewport(GridType gridType,
                               int graphId,
                               int viewportId, ViewportDefinition viewportDefinition) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.updateViewport");
    String retVal = _delegate.updateViewport(gridType, graphId, viewportId, viewportDefinition);
    s_logger.trace("updateViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void deleteViewport(GridType gridType,
                             int graphId,
                             int viewportId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.deleteViewport");
    _delegate.deleteViewport(gridType, graphId, viewportId);
    s_logger.trace("deleteViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public ViewportResults getData(GridType gridType,
                                 int graphId,
                                 int viewportId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getData");
    ViewportResults retVal = _delegate.getData(gridType, graphId, viewportId);
    s_logger.trace("getData completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<String> entityChanged(MasterChangeNotification<?> notification) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.entityChanged");
    List<String> retVal = _delegate.entityChanged(notification);
    s_logger.trace("entityChanged completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<String> portfolioChanged() {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.entityChanged");
    List<String> retVal = _delegate.portfolioChanged();
    s_logger.trace("portfolioChanged completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public ViewportResults getAllGridData(GridType gridType, Format format) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getAllGridData");
    ViewportResults retVal = _delegate.getAllGridData(gridType, format);
    s_logger.trace("getAllGridData completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public UniqueId getViewDefinitionId() {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getViewDefinitionId");
    UniqueId retVal = _delegate.getViewDefinitionId();
    s_logger.trace("getViewDefinitionId completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<ErrorInfo> getErrors() {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getError");
    List<ErrorInfo> retVal = _delegate.getErrors();
    s_logger.trace("getError completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void deleteError(long id) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.deleteError");
    _delegate.deleteError(id);
    s_logger.trace("deleteError completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }
}
