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
public class PrimitivesDependencyGraphViewportResource extends AbstractViewportResource {

  private final String _graphId;

  public PrimitivesDependencyGraphViewportResource(AnalyticsView view, String graphId, String viewportId) {
    super(view, viewportId);
    _graphId = graphId;
  }

  @Override
  public void update(ViewportRequest viewportRequest) {
    _view.updatePrimitivesViewport(_graphId, _viewportId, viewportRequest);
  }

  @Override
  public void delete(String viewportId) {
    _view.deletePrimitivesViewport(_graphId, viewportId);
  }

  @Override
  public AnalyticsResults getData() {
    return _view.getPrimitivesData(_graphId, _viewportId);
  }
}
