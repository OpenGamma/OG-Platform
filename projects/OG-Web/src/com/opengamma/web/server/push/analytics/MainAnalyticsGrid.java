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

  /* package */ MainAnalyticsGrid(AnalyticsView.GridType gridType) {
    super(AnalyticsColumns.empty(), AnalyticsNode.empty());
    ArgumentChecker.notNull(gridType, "gridType");
    _gridType = gridType;
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

  @Override
  void updateStructure(CompiledViewDefinition compiledViewDef) {
    super.updateStructure(compiledViewDef);
    // TODO dep graph grids. what should be done? are they still valid after a structure change?
    // the row and col that defined them might not point to the same cell any more
    // should they be keyed on the position / node (row) and value spec / req (column)? so they can possibly be rebuilt
  }

  /* package */ AnalyticsGridStructure getGridStructure(String dependencyGraphId) {
    return getDependencyGraph(dependencyGraphId).getGridStructure();
  }

  /* package */ String createViewport(String dependencyGraphId, ViewportRequest viewportRequest) {
    return getDependencyGraph(dependencyGraphId).createViewport(viewportRequest);
  }

  /* package */ void updateViewport(String dependencyGraphId, String viewportId, ViewportRequest viewportRequest) {
    getDependencyGraph(dependencyGraphId).updateViewport(viewportId, viewportRequest);
  }

  /* package */ void deleteViewport(String dependencyGraphId, String viewportId) {
    getDependencyGraph(dependencyGraphId).deleteViewport(viewportId);
  }

  /* package */ AnalyticsResults getData(String dependencyGraphId, String viewportId) {
    return getDependencyGraph(dependencyGraphId).getData(viewportId);
  }
}
