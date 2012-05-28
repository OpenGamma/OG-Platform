/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.web.server.push.analytics.AnalyticsGridStructure;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.DependencyGraphRequest;
import com.opengamma.web.server.push.analytics.ViewportRequest;

/**
 *
 */
public class PrimitivesGridResource extends AbstractGridResource implements DependencyGraphOwnerResource {

  public PrimitivesGridResource(AnalyticsView view) {
    super(view);
  }

  @Override
  public AnalyticsGridStructure getGridStructure() {
    return _view.getPrimitivesGridStructure();
  }

  @Override
  public String createViewport(ViewportRequest viewportRequest) {
    return _view.createPrimitivesViewport(viewportRequest);
  }

  @Override
  public AbstractViewportResource getViewport(String viewportId) {
    return new PrimitivesViewportResource(_view, viewportId);
  }

  @Override
  public Response openDependencyGraph(UriInfo uriInfo, DependencyGraphRequest request) {
    String graphId = _view.openPrimitivesDependencyGraph(request.getRow(), request.getColumn());
    return RestUtils.createdResponse(uriInfo, graphId);
  }

  @Override
  public AbstractGridResource getDependencyGraph(String graphId) {
    return new PrimitivesDependencyGraphResource(_view, graphId);
  }

  @Override
  public void closeDependencyGraph(String graphId) {
    _view.closePrimitivesDependencyGraph(graphId);
  }
}
