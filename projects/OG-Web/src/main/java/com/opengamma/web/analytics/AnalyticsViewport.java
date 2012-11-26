/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.view.calc.ViewCycle;

/**
 * Base class for viewports on grids displaying analytics data. A viewport represents the visible part of a grid.
 * A viewport is defined by collections of row and column indices of the visible cells. These are non-contiguous
 * ordered sets. Row indices can be non-contiguous if the grid rows have a tree structure and parts of the
 * structure are collapsed and therefore not visible. Column indices can be non-contiguous if there is a fixed
 * set of columns and the non-fixed columns have been scrolled. This class isn't thread safe.
 */
/* package */ interface AnalyticsViewport {

  /**
   * @return The current viewport data.
   */
  ViewportResults getData();

  String update(ViewportDefinition viewportDefinition, ViewCycle viewCycle, ResultsCache cache);

  ViewportDefinition getDefinition();
}
