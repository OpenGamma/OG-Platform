/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsResults;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.ViewportRequest;

/**
 *
 */
public class DependencyGraphViewportResource extends AbstractViewportResource {

  private final String _graphId;

  public DependencyGraphViewportResource(AnalyticsView.GridType gridType, AnalyticsView view, String graphId, String viewportId) {
    super(gridType,  view, viewportId);
    ArgumentChecker.notNull(graphId, "graphId");
    _graphId = graphId;
  }

  @Override
  public void update(ViewportRequest viewportRequest) {
    _view.updateViewport(_gridType, _graphId, _viewportId, viewportRequest);
  }

  @Override
  public void delete() {
    _view.deleteViewport(_gridType, _graphId, _viewportId);
  }

  @Override
  public AnalyticsResults getData() {
    return _view.getData(_gridType, _graphId, _viewportId);
  }
}
