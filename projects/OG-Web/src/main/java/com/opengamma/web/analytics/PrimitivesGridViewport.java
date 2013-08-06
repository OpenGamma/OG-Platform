/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.view.cycle.ViewCycle;

public class PrimitivesGridViewport extends MainGridViewport {

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes
   * @param viewportDefinition The viewport definition
   * @param cycle The view cycle from the previous calculation cycle
   * @param cache The current results
   */
  PrimitivesGridViewport(MainGridStructure gridStructure,
                         String callbackId,
                         ViewportDefinition viewportDefinition,
                         ViewCycle cycle,
                         ResultsCache cache) {
    super(gridStructure, callbackId, viewportDefinition, cycle, cache);
  }
}
