/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class DependencyGraphViewport extends AnalyticsViewport {

  DependencyGraphViewport(AnalyticsGridStructure gridStructure,
                          ViewportSpecification viewportSpec,
                          ViewCycle cycle,
                          AnalyticsHistory history,
                          String dataId) {
    super(gridStructure, viewportSpec, history, dataId);
    // TODO also need target?
  }

  /* package */ void updateResults(ViewCycle viewCycle, AnalyticsHistory history) {

  }

  /* package */ void update(ViewportSpecification viewportSpec, ViewCycle viewCycle, AnalyticsHistory history) {
    ArgumentChecker.notNull(viewportSpec, "viewportSpec");
    ArgumentChecker.notNull(viewCycle, "viewCycle");
    ArgumentChecker.notNull(history, "history");
    if (!viewportSpec.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportSpec + ", grid: " + _gridStructure);
    }
    _viewportSpec = viewportSpec;
    updateResults(viewCycle, history);
  }
}
