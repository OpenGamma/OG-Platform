/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 * A grid for displaying portfolio analytics data.
 */
/* package */ class PortfolioAnalyticsGrid extends MainAnalyticsGrid<PortfolioGridViewport> {

  /**
   * @param compiledViewDef The view definition whose results the grid will display
   * @param gridId The grid ID, sent to listeners when the grid structure changes
   * @param targetResolver For looking up calculation targets using their specification
   */
  /* package */ PortfolioAnalyticsGrid(CompiledViewDefinition compiledViewDef,
                                       String gridId,
                                       ComputationTargetResolver targetResolver) {
    this(new PortfolioGridStructure(compiledViewDef), gridId, targetResolver);
  }

  /* package */ PortfolioAnalyticsGrid(PortfolioGridStructure gridStructure,
                                       String gridId,
                                       ComputationTargetResolver targetResolver) {
    super(AnalyticsView.GridType.PORTFORLIO, gridStructure, gridId, targetResolver);
  }

  /**
   *
   * @param viewportDefinition Defines the extent and properties of the viewport
   * @param callbackId ID that will be passed to listeners when the grid's data changes
   * @return The viewport
   */
  @Override
  protected PortfolioGridViewport createViewport(ViewportDefinition viewportDefinition, String callbackId) {
    return new PortfolioGridViewport(viewportDefinition, _gridStructure, callbackId, _cache);
  }

  /**
   * Factory method for creating a portfolio grid that doesn't contain any data.
   * @return An empty portfolio grid
   */
  /* package */ static PortfolioAnalyticsGrid empty(String gridId) {
    return new PortfolioAnalyticsGrid(PortfolioGridStructure.empty(), gridId, new DummyTargetResolver());
  }
}
