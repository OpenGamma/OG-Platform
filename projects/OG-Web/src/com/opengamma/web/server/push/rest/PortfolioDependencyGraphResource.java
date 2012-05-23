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
public class PortfolioDependencyGraphResource extends AbstractGridResource {

  private final String _graphId;

  public PortfolioDependencyGraphResource(AnalyticsView view, String graphId) {
    super(view);
    ArgumentChecker.notNull(graphId, "graphId");
    _graphId = graphId;
  }

  @Override
  public AnalyticsGridStructure getGridStructure() {
    return _view.getPortfolioGridStructure(_graphId);
  }

  @Override
  public String createViewport(ViewportRequest viewportRequest) {
    return _view.createPortfolioViewport(_graphId, viewportRequest);
  }

  @Override
  public AbstractViewportResource getViewport(@PathParam("viewportId") String viewportId) {
    return new PortfolioDependencyGraphViewportResource(_view, _graphId, viewportId);
  }
}
