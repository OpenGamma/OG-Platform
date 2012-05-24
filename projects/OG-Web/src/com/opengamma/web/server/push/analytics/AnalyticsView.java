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

  public enum GridType {
    PORTFORLIO,
    PRIMITIVES
  }
  
  void close();

  void updateStructure(CompiledViewDefinition compiledViewDefinition);

  void updateResults(ViewComputationResultModel fullResult);

  // -------- portfolio grid --------

  AnalyticsGridStructure getGridStructure(GridType gridType);

  String createViewport(GridType gridType, ViewportRequest request);

  void updateViewport(GridType gridType, String viewportId, ViewportRequest request);

  void deleteViewport(GridType gridType, String viewportId);

  AnalyticsResults getData(GridType gridType, String viewportId);

  // -------- portfolio dependency graph grids --------

  String openDependencyGraph(GridType gridType, int row, int col);

  void closeDependencyGraph(GridType gridType, String dependencyGraphId);

  AnalyticsGridStructure getGridStructure(GridType gridType, String dependencyGraphId);

  String createViewport(GridType gridType, String dependencyGraphId, ViewportRequest request);

  void updateViewport(GridType gridType, String dependencyGraphId, String viewportId, ViewportRequest request);

  void deleteViewport(GridType gridType, String dependencyGraphId, String viewportId);

  AnalyticsResults getData(GridType gridType, String dependencyGraphId, String viewportId);
}
