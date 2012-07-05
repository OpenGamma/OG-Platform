/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
/* package */ class MainAnalyticsGrid extends AnalyticsGrid<MainGridViewport> {

  private final AnalyticsView.GridType _gridType;
  private final Map<String, DependencyGraphGrid> _depGraphs = new HashMap<String, DependencyGraphGrid>();
  private final MainGridStructure _gridStructure;

  private ViewComputationResultModel _latestResults = new InMemoryViewComputationResultModel();
  private AnalyticsHistory _history = new AnalyticsHistory();
  private ViewCycle _cycle = EmptyViewCycle.INSTANCE;


  /* package */ MainAnalyticsGrid(AnalyticsView.GridType gridType, MainGridStructure gridStructure, String gridId) {
    super(gridId);
    ArgumentChecker.notNull(gridType, "gridType");
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    _gridType = gridType;
    _gridStructure = gridStructure;
  }

  /* package */ static MainAnalyticsGrid emptyPortfolio(String gridId) {
    return new MainAnalyticsGrid(AnalyticsView.GridType.PORTFORLIO, PortfolioGridStructure.empty(), gridId);
  }

  /* package */ static MainAnalyticsGrid emptyPrimitives(String gridId) {
    return new MainAnalyticsGrid(AnalyticsView.GridType.PRIMITIVES, PrimitivesGridStructure.empty(), gridId);
  }

  /* package */ static MainAnalyticsGrid portfolio(CompiledViewDefinition compiledViewDef, String gridId) {
    MainGridStructure gridStructure = new PortfolioGridStructure(compiledViewDef);
    return new MainAnalyticsGrid(AnalyticsView.GridType.PORTFORLIO, gridStructure, gridId);
  }

  /* package */ static MainAnalyticsGrid primitives(CompiledViewDefinition compiledViewDef, String gridId) {
    MainGridStructure gridStructure = new PrimitivesGridStructure(compiledViewDef);
    return new MainAnalyticsGrid(AnalyticsView.GridType.PRIMITIVES, gridStructure, gridId);
  }

  // -------- dependency graph grids --------

  private DependencyGraphGrid getDependencyGraph(String graphId) {
    DependencyGraphGrid grid = _depGraphs.get(graphId);
    if (grid == null) {
      throw new DataNotFoundException("No dependency graph found with ID " + graphId + " for " + _gridType + " grid");
    }
    return grid;
  }

  // TODO a better way to specify which cell we want - target spec? stable row ID generated on the server?
  /* package */ void openDependencyGraph(String graphId,
                                         String gridId,
                                         int row,
                                         int col,
                                         CompiledViewDefinition compiledViewDef) {
    if (_depGraphs.containsKey(graphId)) {
      throw new IllegalArgumentException("Dependency graph ID " + graphId + " is already in use");
    }
    Pair<ValueSpecification, String> targetForCell = _gridStructure.getTargetForCell(row, col);
    if (targetForCell == null) {
      throw new DataNotFoundException("No dependency graph is available for row " + row + ", col " + col);
    }
    ValueSpecification valueSpec = targetForCell.getFirst();
    String calcConfigName = targetForCell.getSecond();
    DependencyGraphGrid grid =
        DependencyGraphGrid.create(compiledViewDef, valueSpec, calcConfigName, _cycle, _history, gridId);
    _depGraphs.put(graphId, grid);
  }

  /* package */ void updateViewport(String viewportId, ViewportSpecification viewportSpecification) {
    getViewport(viewportId).update(viewportSpecification, _latestResults, _history);
  }

  /* package */ void updateResults(ViewComputationResultModel fullResult, AnalyticsHistory history, ViewCycle cycle) {
    _latestResults = fullResult;
    _history = history;
    _cycle = cycle;
    // TODO should the row and cols be looked up here and passed to the viewports?
    // look up col index in _columns
    // iterate over _targets, query results for each target
    for (MainGridViewport viewport : _viewports.values()) {
      viewport.updateResults(fullResult, history);
    }
    for (DependencyGraphGrid grid : _depGraphs.values()) {
      grid.updateResults(cycle, history);
    }
  }

  /* package */ void closeDependencyGraph(String graphId) {
    AnalyticsGrid grid = _depGraphs.remove(graphId);
    if (grid == null) {
      throw new DataNotFoundException("No dependency graph found with ID " + graphId + " for " + _gridType + " grid");
    }
  }

  /* package */ DependencyGraphGridStructure getGridStructure(String graphId) {
    return getDependencyGraph(graphId).getGridStructure();
  }

  /* package */ void createViewport(String graphId,
                                    String viewportId,
                                    String dataId,
                                    ViewportSpecification viewportSpecification) {
    getDependencyGraph(graphId).createViewport(viewportId, dataId, viewportSpecification);
  }

  /* package */ void updateViewport(String graphId,
                                    String viewportId,
                                    ViewportSpecification viewportSpec) {
    getDependencyGraph(graphId).updateViewport(viewportId, viewportSpec, _cycle, _history);
  }

  /* package */ void deleteViewport(String graphId, String viewportId) {
    getDependencyGraph(graphId).deleteViewport(viewportId);
  }

  /* package */ ViewportResults getData(String graphId, String viewportId) {
    return getDependencyGraph(graphId).getData(viewportId);
  }

  /* package */ List<String> getDependencyGraphGridIds() {
    List<String> gridIds = new ArrayList<String>();
    for (AnalyticsGrid grid : _depGraphs.values()) {
      gridIds.add(grid.getGridId());
    }
    return gridIds;
  }

  /* package */ List<String> getDependencyGraphViewportDataIds() {
    List<String> dataIds = new ArrayList<String>();
    for (DependencyGraphGrid grid : _depGraphs.values()) {
      dataIds.addAll(grid.getViewportDataIds());
    }
    return dataIds;
  }

  @Override
  public Object getGridStructure() {
    return _gridStructure;
  }

  @Override
  protected MainGridViewport createViewport(ViewportSpecification viewportSpecification, String dataId) {
    return new MainGridViewport(viewportSpecification, _gridStructure, dataId, _latestResults, _history);
  }
}
