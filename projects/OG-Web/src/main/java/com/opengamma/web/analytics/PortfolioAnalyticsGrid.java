/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.tuple.Pair;

/**
 * A grid for displaying portfolio analytics data.
 */
/* package */ class PortfolioAnalyticsGrid extends MainAnalyticsGrid<PortfolioGridViewport> {

  /**
   * @param compiledViewDef The view definition whose results the grid will display
   * @param gridId The grid ID, sent to listeners when the grid structure changes
   * @param targetResolver For looking up calculation targets using their specification
   * @param valueMappings Mappings between the requirements and outputs of the view
   */
  /* package */ PortfolioAnalyticsGrid(CompiledViewDefinition compiledViewDef,
                                       String gridId,
                                       ComputationTargetResolver targetResolver,
                                       ValueMappings valueMappings) {
    this(new PortfolioGridStructure(compiledViewDef, valueMappings), gridId, targetResolver);
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
  protected Pair<PortfolioGridViewport, Boolean> createViewport(ViewportDefinition viewportDefinition, String callbackId) {
    PortfolioGridViewport viewport = new PortfolioGridViewport(_gridStructure, callbackId);
    String updatedCallbackId = viewport.update(viewportDefinition, _cache);
    boolean hasData = (updatedCallbackId != null);
    return Pair.of(viewport, hasData);
  }

  /**
   * Factory method for creating a portfolio grid that doesn't contain any data.
   * @return An empty portfolio grid
   */
  /* package */ static PortfolioAnalyticsGrid empty(String gridId) {
    return new PortfolioAnalyticsGrid(PortfolioGridStructure.empty(), gridId, new DummyTargetResolver());
  }
}
