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
 * A grid for displaying primitives analytics data.
 */
/* package */ class PrimitivesAnalyticsGrid extends MainAnalyticsGrid<PrimitivesGridViewport> {

  /* package */ PrimitivesAnalyticsGrid(CompiledViewDefinition compiledViewDef,
                                        String gridId,
                                        ComputationTargetResolver targetResolver,
                                        ValueMappings valueMappings) {
    this(new PrimitivesGridStructure(compiledViewDef, valueMappings), gridId, targetResolver);
  }

  /* package */ PrimitivesAnalyticsGrid(MainGridStructure gridStructure,
                                        String gridId,
                                        ComputationTargetResolver targetResolver) {
    super(AnalyticsView.GridType.PRIMITIVES, gridStructure, gridId, targetResolver);
  }

  /**
   *
   * @param viewportDefinition Defines the extent and properties of the viewport
   * @param callbackId ID that will be passed to listeners when the grid's data changes
   * @return The viewport
   */
  @Override
  protected Pair<PrimitivesGridViewport, Boolean> createViewport(ViewportDefinition viewportDefinition, String callbackId) {
    PrimitivesGridViewport viewport = new PrimitivesGridViewport(_gridStructure, callbackId);
    String updatedCallbackId = viewport.update(viewportDefinition, _cache);
    boolean hasData = (updatedCallbackId != null);
    return Pair.of(viewport, hasData);
  }

  /**
   * Factory method for creating a primitives grid that doesn't contain any data.
   * @return An empty primitives grid
   */
  /* package */ static PrimitivesAnalyticsGrid empty(String gridId) {
    return new PrimitivesAnalyticsGrid(PrimitivesGridStructure.empty(), gridId, new DummyTargetResolver());
  }
}
