/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 * TODO should this be split into 2 classes?
 * rows, cols, history and viewports apply to all grids including dependency graphs - AnalyticsGrid
 * depGraphs only apply to the two top level grids - what should the class be called?
 */
/* package */ class AnalyticsGrid {

  private final AnalyticsColumns _cols;
  private final AnalyticsRows _rows;
  private final AnalyticsHistory _history = new AnalyticsHistory();
  private final Map<String, AnalyticsViewport> _viewports = new HashMap<String, AnalyticsViewport>();
  private final Map<String, DependencyGraphGrid> _depGraphs = new HashMap<String, DependencyGraphGrid>();

  private AnalyticsGrid(AnalyticsColumns cols, AnalyticsRows rows) {
    _cols = cols;
    _rows = rows;
  }

  /**
   * @return An empty grid structure with no rows or columns
   */
  /* package */ static AnalyticsGrid empty() {
    return new AnalyticsGrid(AnalyticsColumns.empty(), AnalyticsRows.empty());
  }

  /* package */ static AnalyticsGrid create(CompiledViewDefinition compiledViewDef) {
    return new AnalyticsGrid(AnalyticsColumns.create(compiledViewDef), AnalyticsRows.create(compiledViewDef));
  }

  /* package */ void updateResults(ViewComputationResultModel fullResult) {
    _history.addResults(fullResult);
    for (AnalyticsViewport viewport : _viewports.values()) {
      viewport.updateResults(fullResult, _history);
    }
  }

  // -------- main grid --------

  /* package */ void updateStructure(CompiledViewDefinition compiledViewDef) {
    throw new UnsupportedOperationException("updateStructure not implemented");

  }
  /* package */ AnalyticsGridStructure getGridStructure() {
    throw new UnsupportedOperationException("getGridStructure() not implemented");
  }

  /* package */ String createViewport(ViewportRequest request) {
    throw new UnsupportedOperationException("createViewport not implemented");
  }

  /* package */ void updateViewport(String viewportId, ViewportRequest request) {
    throw new UnsupportedOperationException("updateViewport not implemented");

  }

  /* package */ void deleteViewport(String viewportId) {
    throw new UnsupportedOperationException("deleteViewport not implemented");

  }

  /* package */ AnalyticsResults getData(String viewportId) {

    throw new UnsupportedOperationException("getData not implemented");
  }

  // -------- dependency graph grids --------

  /* package */ String openDependencyGraph(int row, int col) {

    throw new UnsupportedOperationException("openDependencyGraph not implemented");
  }

  /* package */ void closeDependencyGraph(String graphId) {
    throw new UnsupportedOperationException("closeDependencyGraph not implemented");
  }

  /* package */ AnalyticsGridStructure getGridStructure(String dependencyGraphId) {

    throw new UnsupportedOperationException("getGridStructure not implemented");
  }

  /* package */ String createViewport(String dependencyGraphId, ViewportRequest request) {

    throw new UnsupportedOperationException("createViewport not implemented");
  }

  /* package */ void updateViewport(String dependencyGraphId, String viewportId, ViewportRequest request) {
    throw new UnsupportedOperationException("updateViewport not implemented");
  }

  /* package */ void deleteViewport(String dependencyGraphId, String viewportId) {
    throw new UnsupportedOperationException("deleteViewport not implemented");
  }

  /* package */ AnalyticsResults getData(String dependencyGraphId, String viewportId) {

    throw new UnsupportedOperationException("getData not implemented");
  }
}
