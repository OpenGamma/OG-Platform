/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.ws.rs.PathParam;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsGridStructure;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.ViewportRequest;

/**
 *
 */
public class PrimitivesDependencyGraphResource extends DependencyGraphResource {

  public PrimitivesDependencyGraphResource(AnalyticsView view, String graphId) {
    super(view, graphId);
  }

  @Override
  public AnalyticsGridStructure getGridStructure() {
    return _view.getPrimitivesGridStructure(_graphId);
  }

  @Override
  public String createViewport(ViewportRequest viewportRequest) {
    return _view.createPrimitivesViewport(_graphId, viewportRequest);
  }

  @Override
  public AbstractViewportResource getViewport(@PathParam("viewportId") String viewportId) {
    return new PrimitivesDependencyGraphViewportResource(_view, _graphId, viewportId);
  }

  @Override
  public void close() {
    _view.closePrimitivesDependencyGraph(_graphId);
  }
}
