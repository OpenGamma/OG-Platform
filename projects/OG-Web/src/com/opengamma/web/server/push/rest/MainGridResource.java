/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import java.net.URI;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.GridStructure;
import com.opengamma.web.server.push.analytics.ViewportDefinition;

/**
 *
 */
public class MainGridResource extends AbstractGridResource implements DependencyGraphOwnerResource {

  public MainGridResource(AnalyticsView.GridType gridType, AnalyticsView view) {
    super(gridType, view);
  }

  @Override
  public GridStructure getGridStructure() {
    return _view.getGridStructure(_gridType);
  }

  @Override
  /* package */ long createViewport(int viewportId, String callbackId, ViewportDefinition viewportDefinition) {
    return _view.createViewport(_gridType, viewportId, callbackId, viewportDefinition).getFirst();
  }

  @Override
  public AbstractViewportResource getViewport(int viewportId) {
    return new MainGridViewportResource(_gridType, _view, viewportId);
  }

  @Override
  public Response openDependencyGraph(UriInfo uriInfo, int row, int col) {
    int graphId = s_nextId.getAndIncrement();
    String graphIdStr = Integer.toString(graphId);
    URI graphUri = uriInfo.getAbsolutePathBuilder().path(graphIdStr).build();
    String callbackId = graphUri.getPath();
    _view.openDependencyGraph(_gridType, graphId, callbackId, row, col);
    return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, graphUri).build();
  }

  @Override
  public AbstractGridResource getDependencyGraph(int graphId) {
    return new DependencyGraphResource(_gridType, _view, graphId);
  }
}
