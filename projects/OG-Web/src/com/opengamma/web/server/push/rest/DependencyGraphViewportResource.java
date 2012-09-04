/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.ViewportResults;
import com.opengamma.web.server.push.analytics.ViewportSpecification;

/**
 *
 */
public class DependencyGraphViewportResource extends AbstractViewportResource {

  private final int _graphId;

  public DependencyGraphViewportResource(AnalyticsView.GridType gridType, AnalyticsView view, int graphId, int viewportId) {
    super(gridType, view, viewportId);
    _graphId = graphId;
  }

  @Override
  public long update(ViewportSpecification viewportSpecification) {
    return _view.updateViewport(_gridType, _graphId, _viewportId, viewportSpecification).getFirst();
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
