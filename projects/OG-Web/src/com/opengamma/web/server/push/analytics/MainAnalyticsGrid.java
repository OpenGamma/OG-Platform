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
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class MainAnalyticsGrid extends AnalyticsGrid {

  private final AnalyticsView.GridType _gridType;
  private final Map<String, AnalyticsGrid> _depGraphs = new HashMap<String, AnalyticsGrid>();
  private final AnalyticsResultsMapper _resultsMapper;

  private ViewComputationResultModel _latestResults = new InMemoryViewComputationResultModel();

  /* package */ MainAnalyticsGrid(AnalyticsView.GridType gridType,
                                  AnalyticsResultsMapper resultsMapper,
                                  AnalyticsGridStructure gridStructure,
                                  String gridId) {
    super(gridStructure, gridId);
    ArgumentChecker.notNull(gridType, "gridType");
    ArgumentChecker.notNull(resultsMapper, "resultsMapper");
    _gridType = gridType;
    _resultsMapper = resultsMapper;
  }

  // TODO does this actually need the grid type parameter? could hard code it as one or other and it probably wouldn't matter
  /* package */ static MainAnalyticsGrid empty(AnalyticsView.GridType gridType, String gridId) {
    return new MainAnalyticsGrid(gridType, AnalyticsResultsMapper.empty(), AnalyticsGridStructure.empty(), gridId);
  }

  /* package */ static MainAnalyticsGrid portfolio(CompiledViewDefinition compiledViewDef, String gridId) {
    AnalyticsResultsMapper resultsMapper = AnalyticsResultsMapper.portfolio(compiledViewDef);
    AnalyticsGridStructure gridStructure = AnalyticsGridStructure.portoflio(compiledViewDef, resultsMapper.getColumnGroups());
    return new MainAnalyticsGrid(AnalyticsView.GridType.PORTFORLIO, resultsMapper,  gridStructure, gridId);
  }

  /* package */ static MainAnalyticsGrid primitives(CompiledViewDefinition compiledViewDef, String gridId) {
    AnalyticsResultsMapper resultsMapper = AnalyticsResultsMapper.primitives(compiledViewDef);
    AnalyticsGridStructure gridStructure = AnalyticsGridStructure.primitives(compiledViewDef, resultsMapper.getColumnGroups());
    return new MainAnalyticsGrid(AnalyticsView.GridType.PRIMITIVES, resultsMapper, gridStructure, gridId);
  }

  // -------- dependency graph grids --------

  private AnalyticsGrid getDependencyGraph(String graphId) {
    AnalyticsGrid grid = _depGraphs.get(graphId);
    if (grid == null) {
      throw new DataNotFoundException("No dependency graph found with ID " + graphId + " for " + _gridType + " grid");
    }
    return grid;
  }

  // TODO a better way to specify which cell we want - target spec? stable row ID generated on the server?
  /* package */ void openDependencyGraph(String graphId, String gridId, int row, int col) {
    if (_depGraphs.containsKey(graphId)) {
      throw new IllegalArgumentException("Dependency graph ID " + graphId + " is already in use");
    }
    //_depGraphs.put(graphId, AnalyticsGrid.dependencyGraph(null/*TODO*/, gridId));
  }

  // TODO this is specific to the main grids
  /* package */ void updateResults(ViewComputationResultModel fullResult, AnalyticsHistory history) {
    _latestResults = fullResult;
    // TODO should the row and cols be looked up here and passed to the viewports?
    // look up col index in _columns
    // iterate over _targets, query results for each target
    for (AnalyticsViewport viewport : _viewports.values()) {
      // TODO don't like the cast, parameterize the grid type with the viewport type?
      MainViewport mainViewport = (MainViewport) viewport;
      mainViewport.updateResults(fullResult, history);
    }
  }

  /* package */ void updateViewport(String viewportId,
                                    ViewportSpecification viewportSpecification,
                                    AnalyticsHistory history) {
    ((MainViewport) getViewport(viewportId)).update(viewportSpecification, _latestResults, history);
  }

  /* package */ void updateResults(ViewComputationResultModel fullResult, AnalyticsHistory history, ViewCycle cycle) {
    updateResults(fullResult, history);

    // TODO update the depgraphs
  }

  /* package */ void closeDependencyGraph(String graphId) {
    AnalyticsGrid grid = _depGraphs.remove(graphId);
    if (grid == null) {
      throw new DataNotFoundException("No dependency graph found with ID " + graphId + " for " + _gridType + " grid");
    }
  }

  /* package */ AnalyticsGridStructure getGridStructure(String graphId) {
    return getDependencyGraph(graphId)._gridStructure;
  }

  /* package */ String createViewport(String graphId,
                                      String viewportId,
                                      String dataId,
                                      ViewportSpecification viewportSpecification,
                                      AnalyticsHistory history) {
    return getDependencyGraph(graphId).createViewport(viewportId, dataId, viewportSpecification, history);
  }

  /* package */ void updateViewport(String graphId,
                                    String viewportId,
                                    ViewportSpecification viewportSpec,
                                    AnalyticsHistory history) {
    // TODO fix this once the depgraph viewport API is done
    //getDependencyGraph(graphId).updateViewport(viewportId, viewportSpec, history);
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
    for (AnalyticsGrid grid : _depGraphs.values()) {
      dataIds.addAll(grid.getViewportDataIds());
    }
    return dataIds;
  }

  /* package */ boolean dependencyGraphVisible() {
    return !_depGraphs.isEmpty();
  }

  @Override
  protected AnalyticsViewport createViewport(AnalyticsGridStructure gridStructure,
                                             ViewportSpecification viewportSpecification,
                                             AnalyticsHistory history,
                                             String dataId) {
    return new MainViewport(viewportSpecification, _gridStructure, _resultsMapper, dataId, _latestResults, history);
  }
}
