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
/* package */ class PrimitivesAnalyticsGrid extends MainAnalyticsGrid<MainGridViewport> {

  private final PrimitivesGridStructure _gridStructure;

  /* package */ PrimitivesAnalyticsGrid(CompiledViewDefinition compiledViewDef,
                                        String gridId,
                                        ComputationTargetResolver targetResolver,
                                        ViewportListener viewportListener) {
    this(PrimitivesGridStructure.create(compiledViewDef), gridId, targetResolver, viewportListener);
  }

  /* package */ PrimitivesAnalyticsGrid(PrimitivesGridStructure gridStructure,
                                        String gridId,
                                        ComputationTargetResolver targetResolver,
                                        ViewportListener viewportListener) {
    super(AnalyticsView.GridType.PRIMITIVES, gridId, targetResolver, viewportListener);
    _gridStructure = gridStructure;
  }

  /**
   * @param viewportDefinition Defines the extent and properties of the viewport
   * @param callbackId ID that will be passed to listeners when the grid's data changes
   * @param structureCallbackId ID that will be passed to listeners when the grid's structure changes
   * @param cache The current results
   * @return The viewport
   */
  @Override
  protected MainGridViewport createViewport(ViewportDefinition viewportDefinition,
                                            String callbackId,
                                            String structureCallbackId,
                                            ResultsCache cache) {
    return new PrimitivesGridViewport(getGridStructure(), callbackId, structureCallbackId, viewportDefinition, getViewCycle(), cache);
  }

  @Override
  MainGridStructure getGridStructure() {
    return _gridStructure;
  }

  /**
   * Factory method for creating a primitives grid that doesn't contain any data.
   * @return An empty primitives grid
   */
  /* package */ static PrimitivesAnalyticsGrid empty(String gridId) {
    return new PrimitivesAnalyticsGrid(PrimitivesGridStructure.empty(),
                                       gridId,
                                       new DummyTargetResolver(),
                                       new NoOpViewportListener());
  }

}
