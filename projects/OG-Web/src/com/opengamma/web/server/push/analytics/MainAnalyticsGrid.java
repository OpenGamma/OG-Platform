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

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Grid for displaying analytics data for a portfolio or for calculated values that aren't associated with the
 * portfolio (primitives). This class isn't thread safe.
 */
/* package */ class MainAnalyticsGrid extends AnalyticsGrid<MainGridViewport> {

  /** Type of data in the grid, portfolio or primitives. */
  private final AnalyticsView.GridType _gridType;
  /** Dependency graph grids for cells in this grid, keyed by grid ID. */
  private final Map<String, DependencyGraphGrid> _depGraphs = new HashMap<String, DependencyGraphGrid>();
  /** Row and column structure of the grid. */
  private final MainGridStructure _gridStructure;
  /** For looking up calculation targets using their specifications. */
  private final ComputationTargetResolver _targetResolver;

  /** Cache of results. */
  private ResultsCache _cache = new ResultsCache();
  /** The calculation cycle used to calculate the most recent set of results. */
  private ViewCycle _cycle = EmptyViewCycle.INSTANCE;

  /* package */ MainAnalyticsGrid(AnalyticsView.GridType gridType,
                                  MainGridStructure gridStructure,
                                  String gridId,
                                  ComputationTargetResolver targetResolver) {
    super(gridId);
    ArgumentChecker.notNull(gridType, "gridType");
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    _gridType = gridType;
    _gridStructure = gridStructure;
    _targetResolver = targetResolver;
  }

  /**
   * Factory method for creating a portfolio grid that doesn't contain any data.
   * @return An empty portoflio grid
   */
  /* package */ static MainAnalyticsGrid emptyPortfolio() {
    return new MainAnalyticsGrid(AnalyticsView.GridType.PORTFORLIO, PortfolioGridStructure.empty(), "", new DummyTargetResolver());
  }

  /**
   * Factory method for creating a primitives grid that doesn't contain any data.
   * @return An empty primitives grid
   */
  /* package */ static MainAnalyticsGrid emptyPrimitives() {
    return new MainAnalyticsGrid(AnalyticsView.GridType.PRIMITIVES, PrimitivesGridStructure.empty(), "", new DummyTargetResolver());
  }

  /**
   * Factory method for creating a grid to display portfolio data.
   * @param compiledViewDef The view definition whose results the grid will display
   * @param gridId The grid ID, sent to listeners when the grid structure changes
   * @param targetResolver For looking up calculation targets using their specification
   * @return The new grid
   */
  /* package */ static MainAnalyticsGrid portfolio(CompiledViewDefinition compiledViewDef,
                                                 String gridId,
                                                 ComputationTargetResolver targetResolver) {
    MainGridStructure gridStructure = new PortfolioGridStructure(compiledViewDef);
    return new MainAnalyticsGrid(AnalyticsView.GridType.PORTFORLIO, gridStructure, gridId, targetResolver);
  }

  /**
   * Factory method for creating a grid to display primitives data.
   * @param compiledViewDef The view definition whose results the grid will display
   * @param gridId The grid ID, sent to listeners when the grid structure changes
   * @param targetResolver For looking up calculation targets using their specification
   * @return The new grid
   */
  /* package */ static MainAnalyticsGrid primitives(CompiledViewDefinition compiledViewDef,
                                                    String gridId,
                                                    ComputationTargetResolver targetResolver) {
    MainGridStructure gridStructure = new PrimitivesGridStructure(compiledViewDef);
    return new MainAnalyticsGrid(AnalyticsView.GridType.PRIMITIVES, gridStructure, gridId, targetResolver);
  }

  /**
   * Updates the data in the viewports of the main grid and all dependency graph grids when new results arrive
   * from the calculation engine.
   * @param cache Cache of calculation results
   * @param cycle Calculation cycle that calculated the latest results
   * @return List of IDs specifiying the viewports whose data has changed as a result of the new update
   */
  /* package */ List<String> updateResults(ResultsCache cache, ViewCycle cycle) {
    _cache = cache;
    _cycle = cycle;
    List<String> updatedIds = Lists.newArrayList();
    for (MainGridViewport viewport : _viewports.values()) {
      CollectionUtils.addIgnoreNull(updatedIds, viewport.updateResults(cache));
    }
    for (DependencyGraphGrid grid : _depGraphs.values()) {
      updatedIds.addAll(grid.updateResults(cycle, cache));
    }
    return updatedIds;
  }

  /**
   * Updates a viewport on the main grid, e.g. in response the the user scrolling the grid.
   * @param viewportId ID of the viewport
   * @param viewportSpecification Definition of the updated viewport
   * @return Version number of the updated viewport, allows the client to check that any data it receives corresponds
   * to the current state of the viewport
   * @throws DataNotFoundException If no viewport exists with the specified ID
   */
  /* package */ long updateViewport(String viewportId, ViewportSpecification viewportSpecification) {
    return getViewport(viewportId).update(viewportSpecification, _cache);
  }

  // -------- dependency graph grids --------

  /**
   * Opens a depdency graph grid showing the steps used to calculate a cell's value.
   * @param graphId ID of the dependency graph, this can be any unique value
   * @param gridId ID passed to listeners when the grid's row and column structure changes, this can be any unique value
   * @param row Row index of the cell whose dependency graph is required
   * @param col Column index of the cell whose dependency graph is required
   * @param compiledViewDef Compiled view definition containing the full dependency graph
   * TODO a better way to specify which cell we want - target spec? stable row ID generated on the server?
   * one of these will be needed when dynamic view aggregation is implemented
   */
  /* package */ void openDependencyGraph(String graphId,
                                         String gridId,
                                         int row,
                                         int col,
                                         CompiledViewDefinition compiledViewDef) {
    if (_depGraphs.containsKey(graphId)) {
      throw new IllegalArgumentException("Dependency graph ID " + graphId + " is already in use");
    }
    Pair<String, ValueSpecification> targetForCell = _gridStructure.getTargetForCell(row, col);
    if (targetForCell == null) {
      throw new DataNotFoundException("No dependency graph is available for row " + row + ", col " + col);
    }
    String calcConfigName = targetForCell.getFirst();
    ValueSpecification valueSpec = targetForCell.getSecond();
    DependencyGraphGrid grid =
        DependencyGraphGrid.create(compiledViewDef, valueSpec, calcConfigName, _cycle, _cache, gridId, _targetResolver);
    _depGraphs.put(graphId, grid);
  }

  /**
   * Returns an existing dependency graph grid.
   * @param graphId ID of the dependency graph
   * @return The dependency graph grid
   * @throws DataNotFoundException If no dependency graph exists with the specified ID
   */
  private DependencyGraphGrid getDependencyGraph(String graphId) {
    DependencyGraphGrid grid = _depGraphs.get(graphId);
    if (grid == null) {
      throw new DataNotFoundException("No dependency graph found with ID " + graphId + " for " + _gridType + " grid");
    }
    return grid;
  }

  /**
   * Closes an existing dependency graph grid.
   * @param graphId ID of the dependency graph
   * @throws DataNotFoundException If no dependency graph exists with the specified ID
   */
  /* package */ void closeDependencyGraph(String graphId) {
    AnalyticsGrid grid = _depGraphs.remove(graphId);
    if (grid == null) {
      throw new DataNotFoundException("No dependency graph found with ID " + graphId + " for " + _gridType + " grid");
    }
  }

  /**
   * Returns the grid structure for a dependency graph.
   * @param graphId ID of the dependency graph
   * @return The grid structure of the specified dependency graph
   * @throws DataNotFoundException If no dependency graph exists with the specified ID
   */
  /* package */ DependencyGraphGridStructure getGridStructure(String graphId) {
    return getDependencyGraph(graphId).getGridStructure();
  }

  /**
   * Creates a viewport on a dependency graph grid.
   * @param graphId ID of the dependency graph
   * @param viewportId ID of the viewport, can be any unique value
   * @param dataId ID passed to listeners when the viewport's data changes, can be any unique value
   * @param viewportSpec Definition of the viewport
   * @return Version number of the viewport, allows clients to ensure any data they receive for a viewport matches
   * the current viewport state
   */
  /* package */ long createViewport(String graphId,
                                    String viewportId,
                                    String dataId,
                                    ViewportSpecification viewportSpec) {
    return getDependencyGraph(graphId).createViewport(viewportId, dataId, viewportSpec);
  }

  /**
   * Updates an existing viewport on a dependency graph grid
   * @param graphId ID of the dependency graph
   * @param viewportId ID of the viewport, can be any unique value
   * @param viewportSpec Definition of the viewport
   * @return Version number of the viewport, allows clients to ensure any data they receive for a viewport matches
   * the current viewport state
   * @throws DataNotFoundException If no dependency graph exists with the specified ID
   */
  /* package */ long updateViewport(String graphId,
                                    String viewportId,
                                    ViewportSpecification viewportSpec) {
    return getDependencyGraph(graphId).updateViewport(viewportId, viewportSpec, _cycle, _cache);
  }

  /**
   * Deletes an existing viewport on a dependency graph grid.
   * @param graphId ID of the dependency graph
   * @param viewportId ID of the viewport, can be any unique value
   * @throws DataNotFoundException If no dependency graph exists with the specified ID
   */
  /* package */ void deleteViewport(String graphId, String viewportId) {
    getDependencyGraph(graphId).deleteViewport(viewportId);
  }

  /**
   * Returns the data for a viewport on a dependency graph grid.
   * @param graphId ID of the dependency graph
   * @param viewportId ID of the viewport, can be any unique value
   * @return The current data for the viewport
   * @throws DataNotFoundException If no dependency graph exists with the specified ID
   */
  /* package */ ViewportResults getData(String graphId, String viewportId) {
    return getDependencyGraph(graphId).getData(viewportId);
  }

  /**
   * Returns the ID sent to listeners when the row and column structure changes of a dependency graph grid.
   * @param graphId ID of the dependency graph
   * @return The ID
   * @throws DataNotFoundException If no dependency graph exists with the specified ID
   */
  /* package */ String getGridId(String graphId) {
    return getDependencyGraph(graphId).getGridId();
  }

  /**
   * Returns the ID sent to listeners when the data changes in a dependency graph grid viewport.
   * @param graphId ID of the dependency graph
   * @param viewportId ID of the viewport
   * @return The ID
   * @throws DataNotFoundException If no dependency graph and viewport exist with the specified IDs
   */
  /* package */ String getDataId(String graphId, String viewportId) {
    return getDependencyGraph(graphId).getViewport(viewportId).getDataId();
  }

  /**
   * @return The IDs for all depdendency graph grids that are sent to listeners when the grid structure changes
   */
  /* package */ List<String> getDependencyGraphGridIds() {
    List<String> gridIds = new ArrayList<String>();
    for (AnalyticsGrid grid : _depGraphs.values()) {
      gridIds.add(grid.getGridId());
    }
    return gridIds;
  }

  /**
   * @return The row and column structure of the main grid
   */
  @Override
  public GridStructure getGridStructure() {
    return _gridStructure;
  }

  @Override
  protected MainGridViewport createViewport(ViewportSpecification viewportSpecification, String dataId) {
    return new MainGridViewport(viewportSpecification, _gridStructure, dataId, _cache);
  }

  /**
   * Resolver that doesn't resolve anything, used for grids that will always be empty.
   */
  private static class DummyTargetResolver implements ComputationTargetResolver {

    @Override
    public ComputationTarget resolve(ComputationTargetSpecification specification) {
      return null;
    }

    @Override
    public SecuritySource getSecuritySource() {
      return null;
    }

    @Override
    public PositionSource getPositionSource() {
      return null;
    }
  }
}
