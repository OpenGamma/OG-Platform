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

import com.opengamma.web.server.push.analytics.AnalyticsGridStructure;
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
  public AnalyticsGridStructure getGridStructure() {
    return _view.getGridStructure(_gridType);
  }

  @Override
  public void createViewport(String viewportId, ViewportSpecification viewportSpecification) {
    _view.createViewport(_gridType, viewportId, viewportSpecification);
  }

  @Override
  public AbstractViewportResource getViewport(String viewportId) {
    return new MainGridViewportResource(_gridType, _view, viewportId);
  }

  @Override
  public Response openDependencyGraph(UriInfo uriInfo, DependencyGraphRequest request) {
    String nextId = Long.toString(s_nextId.getAndIncrement());
    URI uri = uriInfo.getAbsolutePathBuilder().path(nextId).build();
    String graphId = uri.toString();
    _view.openDependencyGraph(_gridType, graphId, request.getRow(), request.getColumn());
    return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, uri).build();
  }

  @Override
  public AbstractGridResource getDependencyGraph(String graphId) {
    return new DependencyGraphResource(_gridType, _view, graphId);
  }
}
