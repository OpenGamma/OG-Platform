/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.web.server.push.analytics.AnalyticsGridStructure;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.DependencyGraphRequest;
import com.opengamma.web.server.push.analytics.ViewportRequest;

/**
 * TODO need methods to create and return depgraphs
 */
public class MainGridResource extends AbstractGridResource implements DependencyGraphOwnerResource {

  public MainGridResource(AnalyticsView.GridType gridType, AnalyticsView view) {
    super(gridType, view);
  }

  @Override
  public AnalyticsGridStructure getGridStructure() {
    return _view.getGridStructure(_gridType);
  }

  @Override
  public String createViewport(ViewportRequest viewportRequest) {
    return _view.createViewport(_gridType, viewportRequest);
  }

  @Override
  public AbstractViewportResource getViewport(String viewportId) {
    return new MainGridViewportResource(_gridType, _view, viewportId);
  }

  @Override
  public Response openDependencyGraph(UriInfo uriInfo, DependencyGraphRequest request) {
    String graphId = _view.openDependencyGraph(_gridType, request.getRow(), request.getColumn());
    return createdResponse(uriInfo, graphId);
  }

  @Override
  public AbstractGridResource getDependencyGraph(String graphId) {
    return new DependencyGraphResource(_gridType, _view, graphId);
  }
}
