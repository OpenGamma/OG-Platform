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
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.value.ValueSpecification;
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
  private final ComputationTargetResolver _targetResolver;

  private ResultsCache _cache = new ResultsCache();
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

  /* package */ static MainAnalyticsGrid emptyPortfolio(String gridId, ComputationTargetResolver targetResolver) {
    return new MainAnalyticsGrid(AnalyticsView.GridType.PORTFORLIO, PortfolioGridStructure.empty(), gridId, targetResolver);
  }

  /* package */ static MainAnalyticsGrid emptyPrimitives(String gridId, ComputationTargetResolver targetResolver) {
    return new MainAnalyticsGrid(AnalyticsView.GridType.PRIMITIVES, PrimitivesGridStructure.empty(), gridId, targetResolver);
  }

  /* package */ static MainAnalyticsGrid portfolio(CompiledViewDefinition compiledViewDef,
                                                   String gridId,
                                                   ComputationTargetResolver targetResolver) {
    MainGridStructure gridStructure = new PortfolioGridStructure(compiledViewDef);
    return new MainAnalyticsGrid(AnalyticsView.GridType.PORTFORLIO, gridStructure, gridId, targetResolver);
  }

  /* package */ static MainAnalyticsGrid primitives(CompiledViewDefinition compiledViewDef,
                                                    String gridId,
                                                    ComputationTargetResolver targetResolver) {
    MainGridStructure gridStructure = new PrimitivesGridStructure(compiledViewDef);
    return new MainAnalyticsGrid(AnalyticsView.GridType.PRIMITIVES, gridStructure, gridId, targetResolver);
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

  /* package */ long updateViewport(String viewportId, ViewportSpecification viewportSpecification) {
    return getViewport(viewportId).update(viewportSpecification, _cache);
  }

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

  /* package */ void closeDependencyGraph(String graphId) {
    AnalyticsGrid grid = _depGraphs.remove(graphId);
    if (grid == null) {
      throw new DataNotFoundException("No dependency graph found with ID " + graphId + " for " + _gridType + " grid");
    }
  }

  /* package */ DependencyGraphGridStructure getGridStructure(String graphId) {
    return getDependencyGraph(graphId).getGridStructure();
  }

  /* package */ long createViewport(String graphId,
                                    String viewportId,
                                    String dataId,
                                    ViewportSpecification viewportSpecification) {
    return getDependencyGraph(graphId).createViewport(viewportId, dataId, viewportSpecification);
  }

  /* package */ long updateViewport(String graphId,
                                    String viewportId,
                                    ViewportSpecification viewportSpec) {
    return getDependencyGraph(graphId).updateViewport(viewportId, viewportSpec, _cycle, _cache);
  }

  /* package */ void deleteViewport(String graphId, String viewportId) {
    getDependencyGraph(graphId).deleteViewport(viewportId);
  }

  /* package */ ViewportResults getData(String graphId, String viewportId) {
    return getDependencyGraph(graphId).getData(viewportId);
  }

  /* package */ DependencyGraphViewport getViewport(String graphId, String viewportId) {
    return getDependencyGraph(graphId).getViewport(viewportId);
  }

  /* package */ String getGridId(String graphId) {
    return getDependencyGraph(graphId).getGridId();
  }

  /* package */ List<String> getDependencyGraphGridIds() {
    List<String> gridIds = new ArrayList<String>();
    for (AnalyticsGrid grid : _depGraphs.values()) {
      gridIds.add(grid.getGridId());
    }
    return gridIds;
  }

  @Override
  public GridStructure getGridStructure() {
    return _gridStructure;
  }

  @Override
  protected MainGridViewport createViewport(ViewportSpecification viewportSpecification, String dataId) {
    return new MainGridViewport(viewportSpecification, _gridStructure, dataId, _cache);
  }
}
