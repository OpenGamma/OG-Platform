/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.ws.rs.DELETE;

import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.GridStructure;
import com.opengamma.web.server.push.analytics.ViewportSpecification;

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
  public GridStructure getGridStructure() {
    return _view.getGridStructure(_gridType, _graphId);
  }

  @Override
  public void createViewport(String viewportId, String dataId, ViewportSpecification viewportSpecification) {
    _view.createViewport(_gridType, _graphId, viewportId, dataId, viewportSpecification);
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
