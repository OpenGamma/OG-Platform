/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO synchronize every method or decorate with an AnalyticsView impl that does
 */
/* package */ class SimpleAnalyticsView implements AnalyticsView {

  private volatile AnalyticsGrid _portfolioGrid = AnalyticsGrid.empty();
  private volatile AnalyticsGrid _primitivesGrid = AnalyticsGrid.empty();

  public SimpleAnalyticsView(ViewRequest request) {
    ArgumentChecker.notNull(request, "request");
  }

  @Override
  public void close() {
    // TODO implement close()
    throw new UnsupportedOperationException("close not implemented");
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

  private AnalyticsGrid getGrid(GridType gridType) {
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
    return getGrid(gridType).getGridStructure();
  }

  @Override
  public String createViewport(GridType gridType, ViewportRequest request) {
    return getGrid(gridType).createViewport(request);
  }

  @Override
  public void updateViewport(GridType gridType, String viewportId, ViewportRequest viewportRequest) {
    getGrid(gridType).updateViewport(viewportId, viewportRequest);
  }

  @Override
  public void deleteViewport(GridType gridType, String viewportId) {
    getGrid(gridType).deleteViewport(viewportId);
  }

  @Override
  public AnalyticsResults getData(GridType gridType, String viewportId) {
    return getGrid(gridType).getData(viewportId);
  }

  @Override
  public String openDependencyGraph(GridType gridType, int row, int col) {
    return getGrid(gridType).openDependencyGraph(row, col);
  }

  @Override
  public void closeDependencyGraph(GridType gridType, String dependencyGraphId) {
    getGrid(gridType).closeDependencyGraph(dependencyGraphId);
  }

  @Override
  public AnalyticsGridStructure getGridStructure(GridType gridType, String dependencyGraphId) {
    return getGrid(gridType).getGridStructure(dependencyGraphId);
  }

  @Override
  public String createViewport(GridType gridType, String dependencyGraphId, ViewportRequest request) {
    return getGrid(gridType).createViewport(dependencyGraphId, request);
  }

  @Override
  public void updateViewport(GridType gridType, String dependencyGraphId, String viewportId, ViewportRequest viewportRequest) {
    getGrid(gridType).updateViewport(dependencyGraphId, viewportId, viewportRequest);
  }

  @Override
  public void deleteViewport(GridType gridType, String dependencyGraphId, String viewportId) {
    getGrid(gridType).deleteViewport(dependencyGraphId, viewportId);
  }

  @Override
  public AnalyticsResults getData(GridType gridType, String dependencyGraphId, String viewportId) {
    return getGrid(gridType).getData(dependencyGraphId, viewportId);
  }

}
