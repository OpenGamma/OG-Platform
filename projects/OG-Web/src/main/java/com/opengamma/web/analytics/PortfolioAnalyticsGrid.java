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
   * @param valueMappings Mappings between the requirements and outputs of the view
   * @param viewportListener Receives notification when any viewport changes
   */
  /* package */ PortfolioAnalyticsGrid(CompiledViewDefinition compiledViewDef,
                                       String gridId,
                                       ComputationTargetResolver targetResolver,
                                       ValueMappings valueMappings,
                                       ViewportListener viewportListener) {
    this(new PortfolioGridStructure(compiledViewDef, valueMappings), gridId, targetResolver, viewportListener);
  }

  /* package */ PortfolioAnalyticsGrid(PortfolioGridStructure gridStructure,
                                       String gridId,
                                       ComputationTargetResolver targetResolver,
                                       ViewportListener viewportListener) {
    super(AnalyticsView.GridType.PORTFORLIO, gridStructure, gridId, targetResolver, viewportListener);
  }

  /**
   * @param viewportDefinition Defines the extent and properties of the viewport
   * @param callbackId ID that will be passed to listeners when the grid's data changes
   * @return The viewport
   */
  @Override
  protected PortfolioGridViewport createViewport(ViewportDefinition viewportDefinition, String callbackId) {
    return new PortfolioGridViewport(_gridStructure, callbackId, viewportDefinition, _cycle, _cache);
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
