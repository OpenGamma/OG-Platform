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
 * TODO need methods to create and return depgraphs
 */
public class PortfolioGridResource extends AbstractGridResource implements DependencyGraphOwnerResource {

  public PortfolioGridResource(AnalyticsView view) {
    super(view);
  }

  @Override
  public AnalyticsGridStructure getGridStructure() {
    return _view.getPortfolioGridStructure();
  }

  @Override
  public String createViewport(ViewportRequest viewportRequest) {
    return _view.createPortfolioViewport(viewportRequest);
  }

  @Override
  public AbstractViewportResource getViewport(String viewportId) {
    return new PortfolioViewportResource(_view, viewportId);
  }

  @Override
  public Response openDependencyGraph(UriInfo uriInfo, DependencyGraphRequest request) {
    String graphId = _view.openPortfolioDependencyGraph(request.getRow(), request.getColumn());
    return createdResponse(uriInfo, graphId);
  }

  @Override
  public AbstractGridResource getDependencyGraph(String graphId) {
    return new PortfolioDependencyGraphResource(_view, graphId);
  }

  @Override
  public void closeDependencyGraph(String graphId) {
    _view.closePortfolioDependencyGraph(graphId);
  }
}
