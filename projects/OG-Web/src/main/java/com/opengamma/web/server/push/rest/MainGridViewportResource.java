/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.ViewportDefinition;
import com.opengamma.web.server.push.analytics.ViewportResults;

/**
 * REST resource for a viewport on one of the main grids displaying analytics data. The viewport represents the
 * visible part of the grid.
 */
public class MainGridViewportResource extends AbstractViewportResource {

  public MainGridViewportResource(AnalyticsView.GridType gridType, AnalyticsView view, int viewportId) {
    super(gridType, view, viewportId);
  }

  @Override
  public long update(ViewportDefinition viewportSpec) {
    return _view.updateViewport(_gridType, _viewportId, viewportSpec).getFirst();
  }

  @Override
  public void delete() {
    _view.deleteViewport(_gridType, _viewportId);
  }

  @Override
  public ViewportResults getData() {
    return _view.getData(_gridType, _viewportId);
  }
}
