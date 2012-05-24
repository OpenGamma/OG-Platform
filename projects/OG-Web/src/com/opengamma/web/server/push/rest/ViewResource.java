/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.AnalyticsViewManager;

/**
 *
 */
public class ViewResource {

  private final AnalyticsView _view;
  private final AnalyticsViewManager _viewManager;

  public ViewResource(AnalyticsView view, AnalyticsViewManager viewManager) {
    ArgumentChecker.notNull(viewManager, "viewManager");
    ArgumentChecker.notNull(view, "view");
    _viewManager = viewManager;
    _view = view;
  }

  @Path("portfolio")
  public PortfolioGridResource getPortfolioGrid() {
    return new PortfolioGridResource(_view);
  }

  @Path("primitives")
  public PrimitivesGridResource getPrimitivesGrid() {
    return new PrimitivesGridResource(_view);
  }

  @DELETE
  public void deleteView(@PathParam("viewId") String viewId) {
    _viewManager.deleteView(viewId);
  }
}
