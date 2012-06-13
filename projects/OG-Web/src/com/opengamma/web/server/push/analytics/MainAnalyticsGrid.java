/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class MainAnalyticsGrid extends AnalyticsGrid {

  private final AnalyticsView.GridType _gridType;
  private final Map<String, AnalyticsGrid> _depGraphs = new HashMap<String, AnalyticsGrid>();

  private int nextDependencyGraphId = 0;

  /* package */ MainAnalyticsGrid(AnalyticsView.GridType gridType,
                                  AnalyticsGridStructure gridStructure) {
    super(gridStructure);
    ArgumentChecker.notNull(gridType, "gridType");
    _gridType = gridType;
  }

  // TODO does this actually need the grid type parameter? could hard code it as one or other and it probably wouldn't matter
  /* package */ static MainAnalyticsGrid empty(AnalyticsView.GridType gridType) {
    return new MainAnalyticsGrid(gridType, AnalyticsGridStructure.empty());
  }

  /* package */ static MainAnalyticsGrid portfolio(CompiledViewDefinition compiledViewDef) {
    return new MainAnalyticsGrid(AnalyticsView.GridType.PORTFORLIO, AnalyticsGridStructure.portoflio(compiledViewDef));
  }

  /* package */ static MainAnalyticsGrid primitives(CompiledViewDefinition compiledViewDef) {
    return new MainAnalyticsGrid(AnalyticsView.GridType.PRIMITIVES, AnalyticsGridStructure.primitives(compiledViewDef));
  }

  // -------- dependency graph grids --------

  private AnalyticsGrid getDependencyGraph(String dependencyGraphId) {
    AnalyticsGrid grid = _depGraphs.get(dependencyGraphId);
    if (grid == null) {
      throw new DataNotFoundException("No dependency graph found with ID " + dependencyGraphId + " for " + _gridType + " grid");
    }
    return grid;
  }

  // TODO a better way to specify which cell we want - target spec? stable row ID generated on the server?
  /* package */ String openDependencyGraph(int row, int col) {
    // TODO this should be passed in
    String dependencyGraphId = Integer.toString(nextDependencyGraphId++);
    _depGraphs.put(dependencyGraphId, AnalyticsGrid.empty());
    return dependencyGraphId;
  }

  /* package */ void closeDependencyGraph(String dependencyGraphId) {
    AnalyticsGrid grid = _depGraphs.remove(dependencyGraphId);
    if (grid == null) {
      throw new DataNotFoundException("No dependency graph found with ID " + dependencyGraphId + " for " + _gridType + " grid");
    }
  }

  /* package */ AnalyticsGridStructure getGridStructure(String dependencyGraphId) {
    return getDependencyGraph(dependencyGraphId)._gridStructure;
  }

  /* package */ String createViewport(String dependencyGraphId, ViewportSpecification viewportSpecification, AnalyticsHistory history) {
    return getDependencyGraph(dependencyGraphId).createViewport(viewportSpecification, history);
  }

  /* package */ void updateViewport(String dependencyGraphId, String viewportId, ViewportSpecification viewportSpec) {
    getDependencyGraph(dependencyGraphId).updateViewport(viewportId, viewportSpec, null);
  }

  /* package */ void deleteViewport(String dependencyGraphId, String viewportId) {
    getDependencyGraph(dependencyGraphId).deleteViewport(viewportId);
  }

  /* package */ ViewportResults getData(String dependencyGraphId, String viewportId) {
    return getDependencyGraph(dependencyGraphId).getData(viewportId);
  }
}
