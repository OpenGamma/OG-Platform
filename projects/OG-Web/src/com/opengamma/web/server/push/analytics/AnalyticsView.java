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

  // -------- main grid --------

  AnalyticsGridStructure getGridStructure(GridType gridType);

  String createViewport(GridType gridType, ViewportRequest request);

  void updateViewport(GridType gridType, String viewportId, ViewportRequest request);

  void deleteViewport(GridType gridType, String viewportId);

  AnalyticsResults getData(GridType gridType, String viewportId);

  // -------- dependency graph grids --------

  // TODO specifying by row and col is a problem for two reasons
  // 1) if the structure changes we don't know if the cell has moved and where to
  // 2) there's a race condition if the structure changes as the client requests a depgraph - they might not get what they want
  // would be better to specify the row and col in a way that persists across recompilation of the view def
  // i.e. specify the target spec
  // for now send a version ID to the client so it can tell the data is stale? or have the client supply the ID of the
  // structure and perform that logic on the server?
  String openDependencyGraph(GridType gridType, int row, int col);

  void closeDependencyGraph(GridType gridType, String dependencyGraphId);

  AnalyticsGridStructure getGridStructure(GridType gridType, String dependencyGraphId);

  String createViewport(GridType gridType, String dependencyGraphId, ViewportRequest request);

  void updateViewport(GridType gridType, String dependencyGraphId, String viewportId, ViewportRequest request);

  void deleteViewport(GridType gridType, String dependencyGraphId, String viewportId);

  AnalyticsResults getData(GridType gridType, String dependencyGraphId, String viewportId);
}
