/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 * TODO should this be split into 2 classes?
 * rows, cols, history and viewports apply to all grids including dependency graphs - AnalyticsGrid
 * depGraphs only apply to the two top level grids - what should the class be called? MainGrid? extends AnalyticsGrid?
 */
/* package */ class AnalyticsGrid {

  private final AnalyticsColumns _cols;
  private final AnalyticsNode _root;
  private final AnalyticsHistory _history = new AnalyticsHistory();
  private final Map<String, AnalyticsViewport> _viewports = new HashMap<String, AnalyticsViewport>();

  private int nextViewportId = 0;
  private ViewComputationResultModel _latestResults = new InMemoryViewComputationResultModel();

  protected AnalyticsGrid(AnalyticsColumns cols, AnalyticsNode root) {
    _cols = cols;
    _root = root;
  }

  /**
   * @return An empty grid structure with no rows or columns
   */
  /* package */ static AnalyticsGrid empty() {
    return new AnalyticsGrid(AnalyticsColumns.empty(), AnalyticsNode.empty());
  }

  /* package */ static AnalyticsGrid create(CompiledViewDefinition compiledViewDef) {
    return new AnalyticsGrid(AnalyticsColumns.create(compiledViewDef), AnalyticsNode.create(compiledViewDef));
  }

  private AnalyticsViewport getViewport(String viewportId) {
    AnalyticsViewport viewport = _viewports.get(viewportId);
    if (viewport == null) {
      throw new DataNotFoundException("No viewport found with ID " + viewportId);
    }
    return viewport;
  }

  /* package */ void updateResults(ViewComputationResultModel fullResult) {
    _latestResults = fullResult;
    _history.addResults(fullResult);
    for (AnalyticsViewport viewport : _viewports.values()) {
      viewport.updateResults(fullResult, _history);
    }
  }

  /* package */ void updateStructure(CompiledViewDefinition compiledViewDef) {
    throw new UnsupportedOperationException("updateStructure not implemented");
  }
  /* package */ AnalyticsGridStructure getGridStructure() {
    return new AnalyticsGridStructure(_root, _cols);
  }

  /* package */ String createViewport(ViewportRequest viewportRequest) {
    // TODO pass this is
    String viewportId = Integer.toString(nextViewportId++);
    _viewports.put(viewportId, new AnalyticsViewport(viewportRequest, _history, _latestResults));
    return viewportId;
  }

  /* package */ void updateViewport(String viewportId, ViewportRequest viewportRequest) {
    getViewport(viewportId).update(viewportRequest);
  }

  /* package */ void deleteViewport(String viewportId) {
    AnalyticsViewport viewport = _viewports.remove(viewportId);
    if (viewport == null) {
      throw new DataNotFoundException("No viewport found with ID " + viewportId);
    }
  }

  /* package */ AnalyticsResults getData(String viewportId) {
    return getViewport(viewportId).getData();
  }
}
