/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO synchronize every method or decorate with an AnalyticsView impl that does
 */
/* package */ class SimpleAnalyticsView implements AnalyticsView {

  private static final Logger s_logger = LoggerFactory.getLogger(SimpleAnalyticsView.class);

  private final AnalyticsHistory _history = new AnalyticsHistory();

  private MainAnalyticsGrid _portfolioGrid;
  private MainAnalyticsGrid _primitivesGrid;

  public SimpleAnalyticsView(ViewRequest request, String portoflioGridId, String primitivesGridId) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(portoflioGridId, "portoflioGridId");
    ArgumentChecker.notNull(primitivesGridId, "primitivesGridId");
    _portfolioGrid = MainAnalyticsGrid.empty(GridType.PORTFORLIO, portoflioGridId);
    _primitivesGrid = MainAnalyticsGrid.empty(GridType.PRIMITIVES, primitivesGridId);
  }

  @Override
  public void close() {
    // TODO implement close()
  }

  @Override
  public void updateStructure(CompiledViewDefinition compiledViewDefinition) {
    _portfolioGrid = MainAnalyticsGrid.portfolio(compiledViewDefinition, _portfolioGrid.getGridId());
    _primitivesGrid = MainAnalyticsGrid.primitives(compiledViewDefinition, _primitivesGrid.getGridId());
    /*
    TODO
    collect IDs for primitives grid, portfolio grid and all depgraph grids
      portfolio and primitives grids don't have ID ATM, implicit in view URL
      need to pass them in when creating the view?
    publish IDs to listener
    */
  }

  @Override
  public void updateResults(ViewComputationResultModel fullResult) {
    _history.addResults(fullResult);
    _portfolioGrid.updateResults(fullResult, _history);
    _primitivesGrid.updateResults(fullResult, _history);
    /*
    TODO
    collect IDs for all viewports, multiple for each of primitives, portfolio and depgraph grids
    publish IDs to listener
    */
  }

  private MainAnalyticsGrid getGrid(GridType gridType) {
    switch (gridType) {
      case PORTFORLIO:
        return _portfolioGrid;
      case PRIMITIVES:
        return _primitivesGrid;
      default:
        throw new IllegalArgumentException("Unexpected grid type " + gridType);
    }
  }

  @Override
  public AnalyticsGridStructure getGridStructure(GridType gridType) {
    s_logger.debug("Getting grid structure for the {} grid", gridType);
    return getGrid(gridType)._gridStructure;
  }

  @Override
  public void createViewport(GridType gridType, String viewportId, String dataId, ViewportSpecification viewportSpec) {
    getGrid(gridType).createViewport(viewportId, dataId, viewportSpec, _history);
    s_logger.debug("Created viewport ID {} for the {} grid from {}", new Object[]{viewportId, gridType, viewportSpec});
  }

  @Override
  public void updateViewport(GridType gridType, String viewportId, ViewportSpecification viewportSpec) {
    s_logger.debug("Updating viewport {} for {} grid to {}", new Object[]{viewportId, gridType, viewportSpec});
    getGrid(gridType).updateViewport(viewportId, viewportSpec, null);
  }

  @Override
  public void deleteViewport(GridType gridType, String viewportId) {
    s_logger.debug("Deleting viewport {} from the {} grid", viewportId, gridType);
    getGrid(gridType).deleteViewport(viewportId);
  }

  @Override
  public ViewportResults getData(GridType gridType, String viewportId) {
    s_logger.debug("Getting data for viewport {} of the {} grid", viewportId, gridType);
    return getGrid(gridType).getData(viewportId);
  }

  @Override
  public void openDependencyGraph(GridType gridType, String graphId, String gridId, int row, int col) {
    s_logger.debug("Opening dependency graph for cell ({}, {}) of the {} grid", new Object[]{row, col, gridType});
    getGrid(gridType).openDependencyGraph(graphId, gridId,row, col);
  }

  @Override
  public void closeDependencyGraph(GridType gridType, String graphId) {
    s_logger.debug("Closing dependency graph {} of the {} grid", graphId, gridType);
    getGrid(gridType).closeDependencyGraph(graphId);
  }

  @Override
  public AnalyticsGridStructure getGridStructure(GridType gridType, String graphId) {
    s_logger.debug("Getting grid structure for dependency graph {} of the {} grid", graphId, gridType);
    return getGrid(gridType).getGridStructure(graphId);
  }

  @Override
  public void createViewport(GridType gridType, String graphId, String viewportId, String dataId, ViewportSpecification viewportSpec) {
    getGrid(gridType).createViewport(graphId, viewportId, dataId, viewportSpec, _history);
    s_logger.debug("Created viewport ID {} for dependency graph {} of the {} grid using {}", new Object[]{viewportId, graphId, gridType, viewportSpec});
  }

  @Override
  public void updateViewport(GridType gridType, String graphId, String viewportId, ViewportSpecification viewportSpec) {
    s_logger.debug("Updating viewport for dependency graph {} of the {} grid using {}", new Object[]{graphId, gridType, viewportSpec});
    getGrid(gridType).updateViewport(graphId, viewportId, viewportSpec);
  }

  @Override
  public void deleteViewport(GridType gridType, String graphId, String viewportId) {
    s_logger.debug("Deleting viewport {} from dependency graph {} of the {} grid", new Object[]{viewportId, graphId, gridType});
    getGrid(gridType).deleteViewport(graphId, viewportId);
  }

  @Override
  public ViewportResults getData(GridType gridType, String graphId, String viewportId) {
    s_logger.debug("Getting data for the viewport {} of the dependency graph {} of the {} grid", new Object[]{viewportId, graphId, gridType});
    return getGrid(gridType).getData(graphId, viewportId);
  }

}
