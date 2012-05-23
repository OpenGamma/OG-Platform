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

  @Override
  public AnalyticsGridStructure getPortfolioGridStructure() {
    return _portfolioGrid.getGridStructure();
  }

  @Override
  public String createPortfolioViewport(ViewportRequest request) {
    return _portfolioGrid.createViewport(request);
  }

  @Override
  public void updatePortfolioViewport(String viewportId, ViewportRequest request) {
    _portfolioGrid.updateViewport(viewportId, request);
  }

  @Override
  public void deletePortfolioViewport(String viewportId) {
    _portfolioGrid.deleteViewport(viewportId);
  }

  @Override
  public AnalyticsResults getPortfolioData(String viewportId) {
    return _portfolioGrid.getData(viewportId);
  }

  @Override
  public String openPortfolioDependencyGraph(int row, int col) {
    return _portfolioGrid.openDependencyGraph(row, col);
  }

  @Override
  public void closePortfolioDependencyGraph(String dependencyGraphId) {
    _portfolioGrid.closeDependencyGraph(dependencyGraphId);
  }

  @Override
  public AnalyticsGridStructure getPortfolioGridStructure(String dependencyGraphId) {
    return _portfolioGrid.getGridStructure(dependencyGraphId);
  }

  @Override
  public String createPortfolioViewport(String dependencyGraphId, ViewportRequest request) {
    return _portfolioGrid.createViewport(dependencyGraphId, request);
  }

  @Override
  public void updatePortfolioViewport(String dependencyGraphId, String viewportId, ViewportRequest request) {
    _portfolioGrid.updateViewport(dependencyGraphId, viewportId, request);
  }

  @Override
  public void deletePortfolioViewport(String dependencyGraphId, String viewportId) {
    _portfolioGrid.deleteViewport(dependencyGraphId, viewportId);
  }

  @Override
  public AnalyticsResults getPortfolioData(String dependencyGraphId, String viewportId) {
    return _portfolioGrid.getData(dependencyGraphId, viewportId);
  }

  @Override
  public AnalyticsGridStructure getPrimitivesGridStructure() {
    return _primitivesGrid.getGridStructure();
  }

  @Override
  public String createPrimitivesViewport(ViewportRequest request) {
    return _primitivesGrid.createViewport(request);
  }

  @Override
  public void updatePrimitivesViewport(String viewportId, ViewportRequest request) {
    _primitivesGrid.updateViewport(viewportId, request);
  }

  @Override
  public void deletePrimitivesViewport(String viewportId) {
    _primitivesGrid.deleteViewport(viewportId);
  }

  @Override
  public AnalyticsResults getPrimitivesData(String viewportId) {
    return _primitivesGrid.getData(viewportId);
  }

  @Override
  public AnalyticsGridStructure getPrimitivesGridStructure(String dependencyGraphId) {
    return _primitivesGrid.getGridStructure(dependencyGraphId);
  }

  @Override
  public String openPrimitivesDependencyGraph(int row, int col) {
    return _primitivesGrid.openDependencyGraph(row, col);
  }

  @Override
  public void closePrimitivesDependencyGraph(String dependencyGraphId) {
    _primitivesGrid.closeDependencyGraph(dependencyGraphId);
  }

  @Override
  public String createPrimitivesViewport(String dependencyGraphId, ViewportRequest request) {
    return _primitivesGrid.createViewport(dependencyGraphId, request);
  }

  @Override
  public void updatePrimitivesViewport(String dependencyGraphId, String viewportId, ViewportRequest request) {
    _primitivesGrid.updateViewport(dependencyGraphId, viewportId, request);
  }

  @Override
  public void deletePrimitivesViewport(String dependencyGraphId, String viewportId) {
    _primitivesGrid.deleteViewport(dependencyGraphId, viewportId);
  }

  @Override
  public AnalyticsResults getPrimitivesData(String dependencyGraphId, String viewportId) {
    return _primitivesGrid.getData(dependencyGraphId, viewportId);
  }
}
