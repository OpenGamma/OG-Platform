/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class AnalyticsViewport {

  private final AnalyticsGridStructure _gridStructure;
  private final ViewportSpecification _viewportSpec;

  private AnalyticsResults _latestResults;

  /* package */ AnalyticsViewport(AnalyticsGridStructure gridStructure,
                                  ViewportSpecification viewportSpec,
                                  ViewComputationResultModel latestResults,
                                  AnalyticsHistory history) {
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(viewportSpec, "viewportSpec");
    ArgumentChecker.notNull(latestResults, "latestResults");
    ArgumentChecker.notNull(history, "history");
    _gridStructure = gridStructure;
    _viewportSpec = viewportSpec;
    updateResults(latestResults, history);
  }

  /**
   * @return An empty viewport with no rows or columns
   */
  /* package */ static AnalyticsViewport empty() {
    return new AnalyticsViewport(AnalyticsGridStructure.empty(),
                                 ViewportSpecification.empty(),
                                 new InMemoryViewComputationResultModel(),
                                 new AnalyticsHistory());
  }

  /* package */ AnalyticsViewport updateResults(ViewComputationResultModel results, AnalyticsHistory history) {
    /*
    get the target for each row in the viewport from the grid structure
    query the results for the results for the target
    for each value get the column index from the grid structure
    if the column is in the viewport update the results
    */
    throw new UnsupportedOperationException("updateResults not implemented");
  }

  /* package */ AnalyticsResults getData() {
    return _latestResults;
  }

  public void update(ViewportSpecification viewportSpec, ViewComputationResultModel results) {
    // TODO implement AnalyticsViewport.update()
    throw new UnsupportedOperationException("update not implemented");
  }
}
