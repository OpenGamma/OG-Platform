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
 * TODO this subclass doesn't have much reason to exist any more
 */
/* package */ class PortfolioAnalyticsGrid extends MainAnalyticsGrid {

  /**
   * @param compiledViewDef The view definition whose results the grid will display
   * @param gridId The grid ID, sent to listeners when the grid structure changes
   * @param targetResolver For looking up calculation targets using their specification
   * @param valueMappings Mappings between the requirements and outputs of the view
   * @param viewportListener Receives notification when any viewport changes
   */
  /* package */ PortfolioAnalyticsGrid(CompiledViewDefinition compiledViewDef,
                                       String gridId,
                                       ComputationTargetResolver targetResolver,
                                       ValueMappings valueMappings,
                                       ViewportListener viewportListener) {
    this(PortfolioGridStructure.forAnalytics(compiledViewDef, valueMappings), gridId, targetResolver, viewportListener);
  }

  /* package */ PortfolioAnalyticsGrid(PortfolioGridStructure gridStructure,
                                       String gridId,
                                       ComputationTargetResolver targetResolver,
                                       ViewportListener viewportListener) {
    super(AnalyticsView.GridType.PORTFORLIO, gridStructure, gridId, targetResolver, viewportListener);
  }

  /**
   * Factory method for creating a portfolio grid that doesn't contain any data.
   *
   * @return An empty portfolio grid
   */
  /* package */
  static PortfolioAnalyticsGrid empty(String gridId) {
    return new PortfolioAnalyticsGrid(PortfolioGridStructure.empty(),
                                      gridId,
                                      new DummyTargetResolver(),
                                      new NoOpViewportListener());
  }
}
