/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 *
 */
public interface AnalyticsView {

  void close();

  void updateStructure(CompiledViewDefinition compiledViewDefinition);

  void updateResults(ViewComputationResultModel fullResult);

  // -------- portfolio grid --------

  AnalyticsGridStructure getPortfolioGridStructure();

  String createPortfolioViewport(ViewportRequest request);

  void updatePortfolioViewport(String viewportId, ViewportRequest request);

  void deletePortfolioViewport(String viewportId);

  AnalyticsResults getPortfolioData(String viewportId);

  // -------- portfolio dependency graph grids --------

  String openPortfolioDependencyGraph(int row, int col);

  void closePortfolioDependencyGraph(String dependencyGraphId);

  AnalyticsGridStructure getPortfolioGridStructure(String dependencyGraphId);

  String createPortfolioViewport(String dependencyGraphId, ViewportRequest request);

  void updatePortfolioViewport(String dependencyGraphId, String viewportId, ViewportRequest request);

  void deletePortfolioViewport(String dependencyGraphId, String viewportId);

  AnalyticsResults getPortfolioData(String dependencyGraphId, String viewportId);

  // -------- primitives grid --------

  AnalyticsGridStructure getPrimitivesGridStructure();

  String createPrimitivesViewport(ViewportRequest request);

  void updatePrimitivesViewport(String viewportId, ViewportRequest request);

  void deletePrimitivesViewport(String viewportId);

  AnalyticsResults getPrimitivesData(String viewportId);

  // -------- primitives dependency graph grids --------

  AnalyticsGridStructure getPrimitivesGridStructure(String dependencyGraphId);

  String openPrimitivesDependencyGraph(int row, int col);

  void closePrimitivesDependencyGraph(String dependencyGraphId);

  String createPrimitivesViewport(String dependencyGraphId, ViewportRequest request);

  void updatePrimitivesViewport(String dependencyGraphId, String viewportId, ViewportRequest request);

  void deletePrimitivesViewport(String dependencyGraphId, String viewportId);

  AnalyticsResults getPrimitivesData(String dependencyGraphId, String viewportId);
}
