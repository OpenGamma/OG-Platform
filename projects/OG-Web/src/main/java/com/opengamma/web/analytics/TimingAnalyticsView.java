/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;

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
  public List<String> updateStructure(CompiledViewDefinition compiledViewDefinition) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.updateStructure");
    List<String> retVal = _delegate.updateStructure(compiledViewDefinition);
    s_logger.trace("Method updateStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<String> updateResults(ViewResultModel results,
                                    ViewCycle viewCycle) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.updateResults");
    List<String> retVal = _delegate.updateResults(results, viewCycle);
    s_logger.trace("Method updateResults completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public GridStructure getGridStructure(GridType gridType) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getGridStructure");
    GridStructure retVal = _delegate.getGridStructure(gridType);
    s_logger.trace("Method getGridStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public boolean createViewport(int requestId,
                                GridType gridType,
                                int viewportId,
                                String callbackId,
                                ViewportDefinition viewportDefinition) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.createViewport");
    boolean retVal = _delegate.createViewport(requestId, gridType, viewportId, callbackId, viewportDefinition);
    s_logger.trace("Method createViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public String updateViewport(GridType gridType,
                               int viewportId,
                               ViewportDefinition viewportDefinition) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.updateViewport");
    String retVal = _delegate.updateViewport(gridType, viewportId, viewportDefinition);
    s_logger.trace("Method updateViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void deleteViewport(GridType gridType, int viewportId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.deleteViewport");
    _delegate.deleteViewport(gridType, viewportId);
    s_logger.trace("Method deleteViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public ViewportResults getData(GridType gridType, int viewportId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getData");
    ViewportResults retVal = _delegate.getData(gridType, viewportId);
    s_logger.trace("Method getData completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void openDependencyGraph(int requestId,
                                  GridType gridType,
                                  int graphId, String callbackId, int row, int col) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.openDependencyGraph");
    _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, row, col);
    s_logger.trace("Method openDependencyGraph completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public void closeDependencyGraph(GridType gridType, int graphId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.closeDependencyGraph");
    _delegate.closeDependencyGraph(gridType, graphId);
    s_logger.trace("Method closeDependencyGraph completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int graphId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getGridStructure");
    GridStructure retVal = _delegate.getGridStructure(gridType, graphId);
    s_logger.trace("Method getGridStructure completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public boolean createViewport(int requestId,
                                GridType gridType,
                                int graphId,
                                int viewportId,
                                String callbackId,
                                ViewportDefinition viewportDefinition) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.createViewport");
    boolean retVal = _delegate.createViewport(requestId,
                                              gridType,
                                              graphId,
                                              viewportId,
                                              callbackId,
                                              viewportDefinition);
    s_logger.trace("Method createViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public String updateViewport(GridType gridType,
                               int graphId,
                               int viewportId, ViewportDefinition viewportDefinition) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.updateViewport");
    String retVal = _delegate.updateViewport(gridType, graphId, viewportId, viewportDefinition);
    s_logger.trace("Method updateViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public void deleteViewport(GridType gridType,
                             int graphId,
                             int viewportId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.deleteViewport");
    _delegate.deleteViewport(gridType, graphId, viewportId);
    s_logger.trace("Method deleteViewport completed in " + (System.currentTimeMillis() - startTime) + "ms");
  }

  @Override
  public ViewportResults getData(GridType gridType,
                                 int graphId,
                                 int viewportId) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.getData");
    ViewportResults retVal = _delegate.getData(gridType, graphId, viewportId);
    s_logger.trace("Method getData completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<String> entityChanged(MasterChangeNotification<?> notification) {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.entityChanged");
    List<String> retVal = _delegate.entityChanged(notification);
    s_logger.trace("Method entityChanged completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }

  @Override
  public List<String> portfolioChanged() {
    long startTime = System.currentTimeMillis();
    s_logger.trace("Executing AnalyticsView.entityChanged");
    List<String> retVal = _delegate.portfolioChanged();
    s_logger.trace("Method portfolioChanged completed in " + (System.currentTimeMillis() - startTime) + "ms");
    return retVal;
  }
}
