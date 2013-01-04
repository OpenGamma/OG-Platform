/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;


/**
 * A grid for displaying primitives analytics data.
 */
/* package */ class PrimitivesAnalyticsGrid extends MainAnalyticsGrid<PrimitivesGridViewport> {

  /* package */ PrimitivesAnalyticsGrid(CompiledViewDefinition compiledViewDef,
                                        String gridId,
                                        ComputationTargetResolver targetResolver,
                                        ValueMappings valueMappings,
                                        ViewportListener viewportListener) {
    this(new PrimitivesGridStructure(compiledViewDef, valueMappings), gridId, targetResolver, viewportListener);
  }

  /* package */ PrimitivesAnalyticsGrid(MainGridStructure gridStructure,
                                        String gridId,
                                        ComputationTargetResolver targetResolver,
                                        ViewportListener viewportListener) {
    super(AnalyticsView.GridType.PRIMITIVES, gridStructure, gridId, targetResolver, viewportListener);
  }

  /**
   *
   * @param viewportDefinition Defines the extent and properties of the viewport
   * @param callbackId ID that will be passed to listeners when the grid's data changes
   * @return The viewport
   */
  @Override
  protected PrimitivesGridViewport createViewport(ViewportDefinition viewportDefinition, String callbackId) {
    return new PrimitivesGridViewport(_gridStructure, callbackId, viewportDefinition, _cycle, _cache);
  }

  /**
   * Factory method for creating a primitives grid that doesn't contain any data.
   * @return An empty primitives grid
   */
  /* package */ static PrimitivesAnalyticsGrid empty(String gridId) {
    return new PrimitivesAnalyticsGrid(PrimitivesGridStructure.empty(), gridId, new DummyTargetResolver(), new NoOpViewportListener());
  }
}
