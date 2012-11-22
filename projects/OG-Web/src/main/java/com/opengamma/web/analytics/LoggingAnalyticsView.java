/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class LoggingAnalyticsView implements AnalyticsView {

  private final AnalyticsView _delegate;
  private final ViewClient _viewClient;
  /**
   * {@link ValueSpecification}s of values for which full logging info is being collected and the count of the
   * number of viewports that are using them. This allows logging to be enabled and disabled for a value as viewports
   * that view its logging output are opened and closed.
   */
  private final Map<ValueSpecification, Integer> _valueRefCount = Maps.newHashMap();

  /* package */ LoggingAnalyticsView(AnalyticsView delegate, ViewClient viewClient) {
    ArgumentChecker.notNull(delegate, "delegate");
    ArgumentChecker.notNull(viewClient, "viewClient");
    _delegate = delegate;
    _viewClient = viewClient;
  }

  @Override
  public List<String> updateStructure(CompiledViewDefinition compiledViewDefinition) {
    return _delegate.updateStructure(compiledViewDefinition);
  }

  @Override
  public List<String> updateResults(ViewResultModel results, ViewCycle viewCycle) {
    return _delegate.updateResults(results, viewCycle);
  }

  @Override
  public GridStructure getGridStructure(GridType gridType) {
    return _delegate.getGridStructure(gridType);
  }

  @Override
  public boolean createViewport(int requestId,
                                GridType gridType,
                                int viewportId,
                                String callbackId,
                                ViewportDefinition viewportDefinition) {
    boolean hasData = _delegate.createViewport(requestId, gridType, viewportId, callbackId, viewportDefinition);
    // TODO update logging config
    if (viewportDefinition.enableLogging()) {
      // TODO enable logging for the cells in the viewport
    }
    return hasData;
  }

  @Override
  public String updateViewport(GridType gridType, int viewportId, ViewportDefinition viewportDefinition) {
    return _delegate.updateViewport(gridType, viewportId, viewportDefinition);
    // TODO update logging config - do I need to know the previous viewport def? UGH
  }

  @Override
  public void deleteViewport(GridType gridType, int viewportId) {
    // TODO update logging config - need the viewport def
    _delegate.deleteViewport(gridType, viewportId);
  }

  @Override
  public ViewportResults getData(GridType gridType, int viewportId) {
    return _delegate.getData(gridType, viewportId);
  }

  @Override
  public void openDependencyGraph(int requestId, GridType gridType, int graphId, String callbackId, int row, int col) {
    _delegate.openDependencyGraph(requestId, gridType, graphId, callbackId, row, col);
  }

  @Override
  public void closeDependencyGraph(GridType gridType, int graphId) {
    _delegate.closeDependencyGraph(gridType, graphId);
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int graphId) {
    return _delegate.getGridStructure(gridType, graphId);
  }

  @Override
  public boolean createViewport(int requestId,
                                GridType gridType,
                                int graphId,
                                int viewportId,
                                String callbackId,
                                ViewportDefinition viewportDefinition) {
    return _delegate.createViewport(requestId, gridType, graphId, viewportId, callbackId, viewportDefinition);
    // TODO update logging config
  }

  @Override
  public String updateViewport(GridType gridType, int graphId, int viewportId, ViewportDefinition viewportDefinition) {
    return _delegate.updateViewport(gridType, graphId, viewportId, viewportDefinition);
    // TODO update logging config
  }

  @Override
  public void deleteViewport(GridType gridType, int graphId, int viewportId) {
    // TODO update logging config
    _delegate.deleteViewport(gridType, graphId, viewportId);
  }

  @Override
  public ViewportResults getData(GridType gridType, int graphId, int viewportId) {
    return _delegate.getData(gridType, graphId, viewportId);
  }
}
