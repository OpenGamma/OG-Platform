/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.push.analytics.AnalyticsResults;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.ViewportRequest;

/**
 *
 */
public class MainGridViewportResource extends AbstractViewportResource {

  public MainGridViewportResource(AnalyticsView.GridType gridType, AnalyticsView view, String viewportId) {
    super(gridType, view, viewportId);
  }

  @Override
  public void update(ViewportRequest viewportRequest) {
    _view.updateViewport(_gridType, _viewportId, viewportRequest);
  }

  @Override
  public void delete() {
    _view.deleteViewport(_gridType, _viewportId);
  }

  @Override
  public AnalyticsResults getData() {
    return _view.getData(_gridType, _viewportId);
  }
}
