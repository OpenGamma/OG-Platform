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

import com.opengamma.web.server.push.analytics.GridBounds;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.DependencyGraphRequest;
import com.opengamma.web.server.push.analytics.ViewportSpecification;

/**
 *
 */
public class MainGridResource extends AbstractGridResource implements DependencyGraphOwnerResource {

  public MainGridResource(AnalyticsView.GridType gridType, AnalyticsView view) {
    super(gridType, view);
  }

  @Override
  public Object getGridStructure() {
    return _view.getGridStructure(_gridType);
  }

  @Override
  public void createViewport(String viewportId, String dataId, ViewportSpecification viewportSpecification) {
    _view.createViewport(_gridType, viewportId, dataId, viewportSpecification);
  }

  @Override
  public AbstractViewportResource getViewport(String viewportId) {
    return new MainGridViewportResource(_gridType, _view, viewportId);
  }

  @Override
  public Response openDependencyGraph(UriInfo uriInfo, DependencyGraphRequest request) {
    String graphId = Long.toString(s_nextId.getAndIncrement());
    URI graphUri = uriInfo.getAbsolutePathBuilder().path(graphId).build();
    URI gridUri = uriInfo.getAbsolutePathBuilder().path(graphId).path(AbstractGridResource.class, "getGridStructure").build();
    String gridId = gridUri.toString();
    _view.openDependencyGraph(_gridType, graphId, gridId, request.getRow(), request.getColumn());
    return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, graphUri).build();
  }

  @Override
  public AbstractGridResource getDependencyGraph(String graphId) {
    return new DependencyGraphResource(_gridType, _view, graphId);
  }
}
