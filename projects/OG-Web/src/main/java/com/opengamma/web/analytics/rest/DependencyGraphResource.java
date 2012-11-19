/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import javax.ws.rs.DELETE;

import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.GridStructure;
import com.opengamma.web.analytics.ViewportDefinition;

/**
 * REST resource for a grid displaying the dependency graph showing the calculation steps for a cell's value.
 */
public class DependencyGraphResource extends AbstractGridResource {

  private final int _graphId;

  /**
   * @param gridType The type of the main grid associated with this dependency graph
   * @param view The view whose data the grid displays.
   * @param graphId The ID of the dependency graph
   */
  public DependencyGraphResource(AnalyticsView.GridType gridType, AnalyticsView view, int graphId) {
    super(gridType, view);
    _graphId = graphId;
  }

  @Override
  public GridStructure getGridStructure() {
    return _view.getGridStructure(_gridType, _graphId);
  }

  @Override
  /* package */ void createViewport(int requestId, int viewportId, String callbackId, ViewportDefinition viewportDefinition) {
    _view.createViewport(requestId, _gridType, _graphId, viewportId, callbackId, viewportDefinition);
  }

  @Override
  public AbstractViewportResource getViewport(int viewportId) {
    return new DependencyGraphViewportResource(_gridType, _view, _graphId, viewportId);
  }

  /**
   * Closes the grid.
   */
  @DELETE
  public void close() {
    _view.closeDependencyGraph(_gridType, _graphId);
  }
}
