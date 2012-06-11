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

  private MainAnalyticsGrid _portfolioGrid = new MainAnalyticsGrid(GridType.PORTFORLIO);
  private MainAnalyticsGrid _primitivesGrid = new MainAnalyticsGrid(GridType.PRIMITIVES);

  public SimpleAnalyticsView(ViewRequest request) {
    ArgumentChecker.notNull(request, "request");
  }

  @Override
  public void close() {
    // TODO implement close()
  }

  @Override
  public void updateStructure(CompiledViewDefinition compiledViewDefinition) {
    _portfolioGrid.updateStructure(compiledViewDefinition);
    _primitivesGrid.updateStructure(compiledViewDefinition);
    // TODO notify listener
  }

  @Override
  public void updateResults(ViewComputationResultModel fullResult) {
    _portfolioGrid.updateResults(fullResult);
    _primitivesGrid.updateResults(fullResult);
    // TODO notify listener
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
    return getGrid(gridType).getGridStructure();
  }

  @Override
  public String createViewport(GridType gridType, ViewportRequest viewportRequest) {
    String viewportId = getGrid(gridType).createViewport(viewportRequest);
    s_logger.debug("Created viewport ID {} for the {} grid from {}", new Object[]{viewportId, gridType, viewportRequest});
    return viewportId;
  }

  @Override
  public void updateViewport(GridType gridType, String viewportId, ViewportRequest viewportRequest) {
    s_logger.debug("Updating viewport {} for {} grid to {}", new Object[]{viewportId, gridType, viewportRequest});
    getGrid(gridType).updateViewport(viewportId, viewportRequest);
  }

  @Override
  public void deleteViewport(GridType gridType, String viewportId) {
    s_logger.debug("Deleting viewport {} from the {} grid", viewportId, gridType);
    getGrid(gridType).deleteViewport(viewportId);
  }

  @Override
  public AnalyticsResults getData(GridType gridType, String viewportId) {
    s_logger.debug("Getting data for viewport {} of the {} grid", viewportId, gridType);
    return getGrid(gridType).getData(viewportId);
  }

  @Override
  public String openDependencyGraph(GridType gridType, int row, int col) {
    s_logger.debug("Opening dependency graph for cell ({}, {}) of the {} grid", new Object[]{row, col, gridType});
    return getGrid(gridType).openDependencyGraph(row, col);
  }

  @Override
  public void closeDependencyGraph(GridType gridType, String dependencyGraphId) {
    s_logger.debug("Closing dependency graph {} of the {} grid", dependencyGraphId, gridType);
    getGrid(gridType).closeDependencyGraph(dependencyGraphId);
  }

  @Override
  public AnalyticsGridStructure getGridStructure(GridType gridType, String dependencyGraphId) {
    s_logger.debug("Getting grid structure for dependency graph {} of the {} grid", dependencyGraphId, gridType);
    return getGrid(gridType).getGridStructure(dependencyGraphId);
  }

  @Override
  public String createViewport(GridType gridType, String dependencyGraphId, ViewportRequest viewportRequest) {
    String viewportId = getGrid(gridType).createViewport(dependencyGraphId, viewportRequest);
    s_logger.debug("Created viewport ID {} for dependency graph {} of the {} grid using {}", new Object[]{viewportId, dependencyGraphId, gridType, viewportRequest});
    return viewportId;
  }

  @Override
  public void updateViewport(GridType gridType, String dependencyGraphId, String viewportId, ViewportRequest viewportRequest) {
    s_logger.debug("Updating viewport for dependency graph {} of the {} grid using {}", new Object[]{dependencyGraphId, gridType, viewportRequest});
    getGrid(gridType).updateViewport(dependencyGraphId, viewportId, viewportRequest);
  }

  @Override
  public void deleteViewport(GridType gridType, String dependencyGraphId, String viewportId) {
    s_logger.debug("Deleting viewport {} from dependency graph {} of the {} grid", new Object[]{viewportId, dependencyGraphId, gridType});
    getGrid(gridType).deleteViewport(dependencyGraphId, viewportId);
  }

  @Override
  public AnalyticsResults getData(GridType gridType, String dependencyGraphId, String viewportId) {
    s_logger.debug("Getting data for the viewport {} of the dependency graph {} of the {} grid", new Object[]{viewportId, dependencyGraphId, gridType});
    return getGrid(gridType).getData(dependencyGraphId, viewportId);
  }

}
