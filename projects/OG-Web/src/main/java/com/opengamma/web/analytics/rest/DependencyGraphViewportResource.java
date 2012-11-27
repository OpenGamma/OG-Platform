/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.ViewportDefinition;
import com.opengamma.web.analytics.ViewportResults;

/**
 * REST resource for a viewport on a dependency graph grid. The viewport represents the visible part of the grid.
 */
public class DependencyGraphViewportResource extends AbstractViewportResource {

  private final int _graphId;

  public DependencyGraphViewportResource(AnalyticsView.GridType gridType, AnalyticsView view, int graphId, int viewportId) {
    super(gridType, view, viewportId);
    _graphId = graphId;
  }

  @Override
  public void update(ViewportDefinition viewportDefinition) {
    _view.updateViewport(_gridType, _graphId, _viewportId, viewportDefinition);
  }

  @Override
  public void delete() {
    _view.deleteViewport(_gridType, _graphId, _viewportId);
  }

  @Override
  public ViewportResults getData() {
    return _view.getData(_gridType, _graphId, _viewportId);
  }
}
