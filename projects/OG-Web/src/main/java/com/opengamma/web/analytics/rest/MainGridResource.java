/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.GridStructure;
import com.opengamma.web.analytics.ViewportDefinition;

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
  /* package */ void createViewport(int requestId, int viewportId, String callbackId, ViewportDefinition viewportDefinition) {
    _view.createViewport(requestId, _gridType, viewportId, callbackId, viewportDefinition);
  }

  @Override
  public AbstractViewportResource getViewport(int viewportId) {
    return new MainGridViewportResource(_gridType, _view, viewportId);
  }

  @Override
  public Response openDependencyGraph(UriInfo uriInfo, int requestId, int row, int col) {
    int graphId = s_nextId.getAndIncrement();
    String graphIdStr = Integer.toString(graphId);
    URI graphUri = uriInfo.getAbsolutePathBuilder().path(graphIdStr).build();
    String callbackId = graphUri.getPath();
    _view.openDependencyGraph(requestId, _gridType, graphId, callbackId, row, col);
    return Response.status(Response.Status.CREATED).build();
  }

  @Override
  public AbstractGridResource getDependencyGraph(int graphId) {
    return new DependencyGraphResource(_gridType, _view, graphId);
  }
}
