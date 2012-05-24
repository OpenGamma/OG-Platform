/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.ws.rs.DELETE;

import com.opengamma.web.server.push.analytics.AnalyticsGridStructure;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.ViewportRequest;

/**
 *
 */
public class DependencyGraphResource extends AbstractGridResource {

  private final String _graphId;

  /**
   * @param gridType
   * @param view The view whose data the grid displays.
   * @param graphId The ID of the dependency graph
   */
  public DependencyGraphResource(AnalyticsView.GridType gridType, AnalyticsView view, String graphId) {
    super(gridType, view);
    _graphId = graphId;
  }

  @Override
  public AnalyticsGridStructure getGridStructure() {
    return _view.getGridStructure(_gridType, _graphId);
  }

  @Override
  public String createViewport(ViewportRequest viewportRequest) {
    return _view.createViewport(_gridType, _graphId, viewportRequest);
  }

  @Override
  public AbstractViewportResource getViewport(String viewportId) {
    return new DependencyGraphViewportResource(_gridType, _view, _graphId, viewportId);
  }

  @DELETE
  public void close() {
    _view.closeDependencyGraph(_gridType, _graphId);
  }
}
